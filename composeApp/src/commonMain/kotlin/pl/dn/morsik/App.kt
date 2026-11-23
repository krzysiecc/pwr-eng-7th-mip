package pl.dn.morsik

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.dn.morsik.transmit.SignalChannel
import pl.dn.morsik.transmit.Timeline
import pl.dn.morsik.transmit.Transmitter
import pl.dn.morsik.ui.ChannelsUi
import pl.dn.morsik.ui.MainScreen
import pl.dn.morsik.ui.MainUiState
import pl.dn.morsik.ui.MorsikTheme

/**
 * Korzeń interfejsu - to wywołuje MainActivity.
 *
 * Tu mieszka stan ekranu głównego i tu decydujemy, co się dzieje po kliknięciach.
 * Sam MainScreen tylko rysuje to, co dostanie, i zgłasza zdarzenia przez funkcje onXxx.
 *
 * Rzeczy zależne od Androida wchodzą z zewnątrz (dzięki temu ten plik nie zna Androida):
 * @param createChannels tworzy kanały (latarka, wibracja, dźwięk) dla wybranych ustawień
 * @param startDictation otwiera dyktowanie i po rozpoznaniu woła podaną funkcję z tekstem
 */
@Composable
fun App(
    createChannels: (ChannelsUi) -> List<SignalChannel> = { emptyList() },
    startDictation: (onResult: (String) -> Unit) -> Unit = {},
) {
    // mutableStateOf = wartość, której zmiana każe Compose przerysować ekran.
    // remember = zachowaj tę wartość między kolejnymi wywołaniami App() (inaczej co klatkę byłby nowy, pusty stan).
    // `by` + getValue/setValue = można pisać `state` zamiast `state.value`.
    var state by remember { mutableStateOf(MainUiState()) }

    // Korutyny: scope to "właściciel" zadań w tle; znika razem z ekranem i anuluje wtedy nadawanie.
    val scope = rememberCoroutineScope()
    // Trwające nadawanie albo null. Anulowanie tego zadania = Stop.
    var transmission by remember { mutableStateOf<Job?>(null) }

    /** Rozpoczyna nadawanie bieżącej wiadomości (jeśli jest co nadać i nic się już nie nadaje). */
    fun start() {
        if (transmission != null || !state.canStart) return
        val steps = Timeline.build(state.letters) // lista kroków "sygnał / przerwa"
        val channels = createChannels(state.channels)
        transmission = scope.launch {
            try {
                Transmitter().transmit(
                    steps = steps,
                    channels = channels,
                    unitMs = { state.unitMs }, // suwak działa na żywo
                    onProgress = { state = state.copy(transmission = it) },
                )
            } finally {
                // Po końcu albo po Stop: zwolnij kanały i wyczyść stan nadawania.
                // (Silnik sam już zgasił kanały, release() tylko zwalnia zasoby.)
                channels.forEach { it.release() }
                state = state.copy(transmission = null)
                transmission = null
            }
        }
    }

    MorsikTheme {
        // Surface = białe tło na cały ekran w kolorze z motywu.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MainScreen(
                state = state,
                // `it` = domyślna nazwa jedynego parametru lambdy (tu: nowy tekst / nowe kanały / nowe ms).
                onMessageChange = { state = state.withMessage(it) },
                onDictate = { if (!state.isTransmitting) startDictation { text -> state = state.withMessage(text) } },
                onStart = ::start,
                onMenu = {}, // TODO zadanie 12: menu i ekran Pomocy
                onChannelsChange = { state = state.copy(channels = it) },
                onUnitMsChange = { state = state.copy(unitMs = it) },
                onStop = { transmission?.cancel() },
            )
        }
    }
}
