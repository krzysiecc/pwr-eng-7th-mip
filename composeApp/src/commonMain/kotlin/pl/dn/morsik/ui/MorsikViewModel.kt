package pl.dn.morsik.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.dn.morsik.transmit.SignalChannel
import pl.dn.morsik.transmit.Timeline
import pl.dn.morsik.transmit.Transmitter

// =============================================================================================
// ViewModel, czyli "M" i "VM" ze wzorca MVVM:
//
// * Model      - czysta logika: Morse.kt, Timeline.kt, Transmitter.kt.
// * View       - ekrany @Composable (MainScreen, HelpScreen...). Tylko rysują stan i zgłaszają kliknięcia.
// * ViewModel  - ta klasa. Trzyma stan ekranów i decyduje, co zrobić po kliknięciu.
//
// Po co osobna klasa zamiast `remember` w App.kt? ViewModel PRZEŻYWA obrót ekranu: Android niszczy
// i tworzy aktywność od nowa, ale ten sam obiekt ViewModel zostaje, a z nim wpisany tekst i trwające
// nadawanie. Znika dopiero, gdy użytkownik naprawdę zamknie aplikację.
//
// Stan wystawiamy jako StateFlow: strumień, który zawsze ma aktualną wartość. Ekran go "obserwuje"
// (collectAsState w App.kt) i przerysowuje się przy każdej zmianie. Z zewnątrz da się go tylko czytać,
// zmienia go wyłącznie ViewModel przez prywatny MutableStateFlow.
// =============================================================================================

/** Ekrany aplikacji. Wszystkie poza głównym otwiera się z menu i wraca z nich przyciskiem Wstecz. */
enum class Screen { Main, Help, Accessibility, About }

/**
 * Stan i akcje całej aplikacji.
 *
 * @param createChannels tworzy kanały (latarka, wibracja, dźwięk) dla wybranych ustawień.
 *   Dostarcza je MainActivity, bo wymagają Androida. Uwaga: ViewModel żyje dłużej niż aktywność,
 *   więc ta funkcja nie może trzymać aktywności (stąd applicationContext w MainActivity).
 * @param transmitter silnik nadawania; w testach dostaje wirtualny zegar
 */
class MorsikViewModel(
    private val createChannels: (ChannelsUi) -> List<SignalChannel> = { emptyList() },
    private val transmitter: Transmitter = Transmitter(),
) : ViewModel() {

    // Konwencja: prywatne `_main` można zmieniać, publiczne `main` tylko czytać.
    private val _main = MutableStateFlow(MainUiState())
    val main: StateFlow<MainUiState> = _main.asStateFlow()

    private val _help = MutableStateFlow(HelpUiState())
    val help: StateFlow<HelpUiState> = _help.asStateFlow()

    private val _screen = MutableStateFlow(Screen.Main)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    /** Trwające nadawanie (wiadomości albo jednego znaku z Pomocy) albo null. Anulowanie = Stop. */
    private var transmission: Job? = null

    /** Kanały trwającego nadawania (do zmiany w locie przez [onChannelsChange]) albo null. */
    private var liveChannels: LiveChannels? = null

    // ---- ekran główny ----

    fun onMessageChange(message: String) = _main.update { it.withMessage(message) }

    /** Tekst z dyktowania. W trakcie nadawania pole jest tylko do odczytu, więc wynik pomijamy. */
    fun onDictated(text: String) {
        if (!_main.value.isTransmitting) onMessageChange(text)
    }

    /** Zmiana kanałów działa też w trakcie nadawania: odznaczony milknie od razu, zaznaczony dołącza. */
    fun onChannelsChange(channels: ChannelsUi) {
        _main.update { it.copy(channels = channels) }
        liveChannels?.select(channels)
    }

    /** Nowa szybkość z suwaka. coerceIn = przytnij do zakresu, gdyby przyszła wartość spoza niego. */
    fun onUnitMsChange(unitMs: Int) = _main.update {
        it.copy(unitMs = unitMs.coerceIn(MainUiState.FastestUnitMs, MainUiState.SlowestUnitMs))
    }

    /** Nadaj wpisaną wiadomość (przycisk Nadaj albo Wyślij na klawiaturze). */
    fun startMessage() {
        val state = _main.value
        if (transmission?.isActive == true || !state.canStart) return
        launchTransmission(
            letters = state.letters,
            onProgress = { progress -> _main.update { it.copy(transmission = progress) } },
            onEnd = { _main.update { it.copy(transmission = null) } },
        )
    }

    // ---- ekran Pomocy ----

    /** Nadaj jeden znak z tabeli. Jeśli coś się już nadaje, przerywamy to i nadajemy nowy znak. */
    fun sendCharacter(letter: LetterUi) {
        launchTransmission(
            letters = listOf(letter),
            onProgress = { _help.update { it.copy(activeSymbol = letter.symbol) } },
            onEnd = { _help.update { it.copy(activeSymbol = null) } },
        )
    }

    // ---- wspólne ----

    /** Stop: przerywa nadawanie. Kanały gasną w silniku, stan czyści się w `finally` nadawania. */
    fun stop() {
        transmission?.cancel()
    }

    /** Przejście do innego ekranu. Nadawanie się zatrzymuje, żeby nic nie migało "za plecami" użytkownika. */
    fun open(screen: Screen) {
        stop()
        _screen.value = screen
    }

    /** Otwiera albo zamyka menu na ekranie głównym. */
    fun setMenuOpen(open: Boolean) = _main.update { it.copy(menuOpen = open) }

    /**
     * Przycisk Wstecz na ekranach z menu: wraca do ekranu głównego z otwartym menu, z którego tam weszliśmy.
     * (menuOpen zostaje `true` przez cały pobyt w Pomocy itd., a zamyka je dopiero strzałka w menu.)
     */
    fun back() = open(Screen.Main)

    /**
     * Uruchamia nadawanie w viewModelScope: korutyny w nim są anulowane dopiero przy zamknięciu
     * aplikacji, nie przy obrocie ekranu. Poprzednie nadawanie najpierw się kończy (`join`):
     * inaczej jego `finally` zgasiłoby latarkę już w trakcie pierwszej kropki nowego.
     */
    private fun launchTransmission(
        letters: List<LetterUi>,
        onProgress: (TransmissionUi) -> Unit,
        onEnd: () -> Unit,
    ) {
        val previous = transmission
        previous?.cancel()
        transmission = viewModelScope.launch {
            previous?.join()
            val channels = LiveChannels(createChannels, _main.value.channels)
            liveChannels = channels
            try {
                transmitter.transmit(
                    steps = Timeline.build(letters),
                    channels = listOf(channels),
                    unitMs = { _main.value.unitMs }, // suwak działa na żywo
                    onProgress = onProgress,
                )
            } finally {
                // Po końcu albo po Stop: zwolnij kanały i wyczyść stan nadawania.
                // (Silnik sam już zgasił kanały, release() tylko zwalnia zasoby.)
                channels.release()
                if (liveChannels === channels) liveChannels = null
                onEnd()
            }
        }
    }
}

/**
 * Kanały jednego nadawania, które można przestawić w locie. Silnik widzi jeden [SignalChannel],
 * a my pod spodem podmieniamy prawdziwe kanały, gdy użytkownik zmieni zaznaczenie:
 * stare gasną i są zwalniane, nowe powstają i od razu dostają bieżący stan sygnału (włączony/wyłączony).
 */
private class LiveChannels(
    private val create: (ChannelsUi) -> List<SignalChannel>,
    private var selected: ChannelsUi,
) : SignalChannel {
    private var channels = create(selected)
    private var on = false

    fun select(new: ChannelsUi) {
        if (new == selected) return
        dispose()
        selected = new
        channels = create(new)
        if (on) channels.forEach { runCatching { it.setOn(true) } }
    }

    override fun setOn(on: Boolean) {
        this.on = on
        channels.forEach { runCatching { it.setOn(on) } }
    }

    override fun release() = dispose()

    /** Gasi i zwalnia obecne kanały. Błąd jednego nie może zatrzymać pozostałych. */
    private fun dispose() {
        channels.forEach {
            runCatching { it.setOn(false) }
            runCatching { it.release() }
        }
    }
}
