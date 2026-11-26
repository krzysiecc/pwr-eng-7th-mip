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

enum class Screen { Main, Help, Accessibility, About }

// Przeżywa obrót ekranu, więc `createChannels` nie może trzymać aktywności.
class MorsikViewModel(
    private val createChannels: (ChannelsUi) -> List<SignalChannel> = { emptyList() },
    private val transmitter: Transmitter = Transmitter(),
) : ViewModel() {

    private val _main = MutableStateFlow(MainUiState())
    val main: StateFlow<MainUiState> = _main.asStateFlow()

    private val _help = MutableStateFlow(HelpUiState())
    val help: StateFlow<HelpUiState> = _help.asStateFlow()

    private val _screen = MutableStateFlow(Screen.Main)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private var transmission: Job? = null

    private var liveChannels: LiveChannels? = null

    fun onMessageChange(message: String) = _main.update { it.withMessage(message) }

    // W trakcie nadawania wynik dyktowania jest pomijany.
    fun onDictated(text: String) {
        if (!_main.value.isTransmitting) onMessageChange(text)
    }

    fun onChannelsChange(channels: ChannelsUi) {
        _main.update { it.copy(channels = channels) }
        liveChannels?.select(channels)
    }

    fun onUnitMsChange(unitMs: Int) = _main.update {
        it.copy(unitMs = unitMs.coerceIn(MainUiState.FastestUnitMs, MainUiState.SlowestUnitMs))
    }

    fun startMessage() {
        val state = _main.value
        if (transmission?.isActive == true || !state.canStart) return
        launchTransmission(
            letters = state.letters,
            onProgress = { progress -> _main.update { it.copy(transmission = progress) } },
            onEnd = { _main.update { it.copy(transmission = null) } },
        )
    }

    fun sendCharacter(letter: LetterUi) {
        launchTransmission(
            letters = listOf(letter),
            onProgress = { _help.update { it.copy(activeSymbol = letter.symbol) } },
            onEnd = { _help.update { it.copy(activeSymbol = null) } },
        )
    }

    fun stop() {
        transmission?.cancel()
    }

    fun open(screen: Screen) {
        stop()
        _screen.value = screen
    }

    fun setMenuOpen(open: Boolean) = _main.update { it.copy(menuOpen = open) }

    // `menuOpen` zostaje bez zmian, więc wracamy do otwartego menu.
    fun back() = open(Screen.Main)

    private fun launchTransmission(
        letters: List<LetterUi>,
        onProgress: (TransmissionUi) -> Unit,
        onEnd: () -> Unit,
    ) {
        val previous = transmission
        previous?.cancel()
        transmission = viewModelScope.launch {
            // Inaczej `finally` poprzedniego nadawania zgasiłoby kanały nowego.
            previous?.join()
            val channels = LiveChannels(createChannels, _main.value.channels)
            liveChannels = channels
            try {
                transmitter.transmit(
                    steps = Timeline.build(letters),
                    channels = listOf(channels),
                    unitMs = { _main.value.unitMs },
                    onProgress = onProgress,
                )
            } finally {
                channels.release()
                if (liveChannels === channels) liveChannels = null
                onEnd()
            }
        }
    }
}

// Pozwala zmieniać kanały w trakcie nadawania.
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

    private fun dispose() {
        channels.forEach {
            runCatching { it.setOn(false) }
            runCatching { it.release() }
        }
    }
}
