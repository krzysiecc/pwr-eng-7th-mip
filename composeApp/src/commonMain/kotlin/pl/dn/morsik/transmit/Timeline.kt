package pl.dn.morsik.transmit

import pl.dn.morsik.morse.Morse
import pl.dn.morsik.ui.LetterUi

// Przerwa należy do znaku, po którym następuje.
data class SignalStep(val on: Boolean, val units: Int, val letter: Int, val symbol: Int)

// Czasy w jednostkach; 1 jednostka = długość kropki.
object Timeline {
    const val DOT_UNITS = 1
    const val DASH_UNITS = 3
    const val SYMBOL_GAP_UNITS = 1
    const val LETTER_GAP_UNITS = 3
    const val WORD_GAP_UNITS = 7

    fun build(letters: List<LetterUi>): List<SignalStep> {
        val steps = mutableListOf<SignalStep>()
        letters.forEachIndexed { letterIndex, letter ->
            letter.code.forEachIndexed { symbolIndex, symbol ->
                val units = if (symbol == Morse.DASH) DASH_UNITS else DOT_UNITS
                steps += SignalStep(on = true, units = units, letter = letterIndex, symbol = symbolIndex)

                val gap = when {
                    symbolIndex < letter.code.lastIndex -> SYMBOL_GAP_UNITS
                    letterIndex == letters.lastIndex -> 0
                    letters[letterIndex + 1].startsWord -> WORD_GAP_UNITS
                    else -> LETTER_GAP_UNITS
                }
                if (gap > 0) steps += SignalStep(on = false, units = gap, letter = letterIndex, symbol = symbolIndex)
            }
        }
        return steps
    }

    fun totalUnits(steps: List<SignalStep>): Int = steps.sumOf { it.units }
}
