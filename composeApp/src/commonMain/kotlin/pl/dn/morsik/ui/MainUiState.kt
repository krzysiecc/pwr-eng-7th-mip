package pl.dn.morsik.ui

/** One letter of the message with its Morse code written as dots and dashes, e.g. "O" to "---". */
data class LetterUi(val symbol: String, val code: String)

enum class Signal { Dot, Dash }

/** Position of the transmission: which letter, which of its symbols, and whether the signal is on right now. */
data class TransmissionUi(
    val activeLetter: Int,
    val activeSymbol: Int,
    val signalOn: Boolean,
)

data class ChannelsUi(
    val flashlight: Boolean = true,
    val vibration: Boolean = true,
    val sound: Boolean = false,
)

data class MainUiState(
    val message: String = "",
    val letters: List<LetterUi> = emptyList(),
    val transmission: TransmissionUi? = null,
    val channels: ChannelsUi = ChannelsUi(),
    val unitMs: Int = 250,
) {
    val activeLetter: LetterUi? get() = transmission?.let { letters.getOrNull(it.activeLetter) }

    val activeSignal: Signal?
        get() {
            val t = transmission ?: return null
            return when (activeLetter?.code?.getOrNull(t.activeSymbol)) {
                '.' -> Signal.Dot
                '-' -> Signal.Dash
                else -> null
            }
        }

    companion object {
        const val SlowestUnitMs = 400
        const val FastestUnitMs = 150

        /** Hard-coded state matching the mockup, until the encoder and transmitter are wired in. */
        val Demo = MainUiState(
            message = "SOS",
            letters = listOf(
                LetterUi("S", "..."),
                LetterUi("O", "---"),
                LetterUi("S", "..."),
            ),
            transmission = TransmissionUi(activeLetter = 1, activeSymbol = 1, signalOn = true),
        )
    }
}
