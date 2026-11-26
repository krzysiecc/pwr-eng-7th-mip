package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse
import kotlin.math.roundToInt

// `startsWord` jest false dla pierwszego słowa.
data class LetterUi(val symbol: String, val code: String, val startsWord: Boolean = false)

fun previewLetters(message: String): List<LetterUi> {
    val result = mutableListOf<LetterUi>()
    for (word in Morse.encode(message)) {
        var firstInWord = true
        for (letter in word.letters) {
            val code = letter.code ?: continue
            result += LetterUi(
                symbol = letter.text.uppercase(),
                code = code,
                startsWord = firstInWord && result.isNotEmpty(),
            )
            firstInWord = false
        }
    }
    return result
}

fun displayCode(code: String): String =
    code.map { if (it == Morse.DOT) "·" else "–" }.joinToString(" ")

fun spokenLetter(symbol: String, code: String): String =
    Strings.characterName(symbol) + ": " + code.map { if (it == Morse.DOT) Strings.SignalDot else Strings.SignalDash }
        .joinToString(" ") { it.lowercase() }

enum class Signal { Dot, Dash }

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
    val unitMs: Int = DefaultUnitMs,
    val menuOpen: Boolean = false,
) {
    val isTransmitting: Boolean get() = transmission != null

    val canStart: Boolean get() = !isTransmitting && letters.isNotEmpty()

    val activeLetter: LetterUi? get() = transmission?.let { letters.getOrNull(it.activeLetter) }

    val activeSignal: Signal?
        get() {
            val symbolIndex = transmission?.activeSymbol ?: return null
            return when (activeLetter?.code?.getOrNull(symbolIndex)) {
                Morse.DOT -> Signal.Dot
                Morse.DASH -> Signal.Dash
                else -> null
            }
        }

    fun withMessage(message: String): MainUiState = copy(message = message, letters = previewLetters(message))

    companion object {
        const val SlowestUnitMs = 350

        const val FastestUnitMs = 50

        // WCAG 2.3.1: max 3 błyski/s; poniżej 170 ms pokazujemy ostrzeżenie.
        const val SafeUnitMs = 170
        const val DefaultUnitMs = 250

        fun isBeyondSafeSpeed(unitMs: Int): Boolean = unitMs < SafeUnitMs

        // Lustrzane odbicie: prawa strona suwaka = szybciej = mniej ms.
        fun sliderPosition(unitMs: Int): Float = (SlowestUnitMs + FastestUnitMs - unitMs).toFloat()

        fun unitMsAt(sliderPosition: Float): Int = SlowestUnitMs + FastestUnitMs - sliderPosition.roundToInt()

        // Pozycja (0..1), od której zaczyna się strefa ostrzeżenia.
        val SafeZoneEnd = (SlowestUnitMs - SafeUnitMs).toFloat() / (SlowestUnitMs - FastestUnitMs)

        val Demo = MainUiState(
            message = "SOS",
            letters = previewLetters("SOS"),
            transmission = TransmissionUi(activeLetter = 1, activeSymbol = 1, signalOn = true),
        )
    }
}
