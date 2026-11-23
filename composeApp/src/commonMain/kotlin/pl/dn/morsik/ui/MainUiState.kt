package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse

/**
 * One letter of the message with its Morse code written as dots and dashes, e.g. "O" to "---".
 * [startsWord] marks the first letter of every word after the first one, so the preview can show the word gap.
 */
data class LetterUi(val symbol: String, val code: String, val startsWord: Boolean = false)

/**
 * Letters to show under the message field, in transmission order.
 * Characters without a Morse code (e.g. emoji, `#`) are left out, because they are not transmitted.
 */
fun previewLetters(message: String): List<LetterUi> =
    Morse.encode(message)
        .filter { it.isTransmittable }
        .flatMapIndexed { wordIndex, word ->
            word.letters
                .mapNotNull { letter -> letter.code?.let { code -> letter.text.uppercase() to code } }
                .mapIndexed { index, (symbol, code) ->
                    LetterUi(symbol, code, startsWord = wordIndex > 0 && index == 0)
                }
        }

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

    /** New message typed by the user, with the letter preview recomputed from it. */
    fun withMessage(message: String): MainUiState = copy(message = message, letters = previewLetters(message))

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
