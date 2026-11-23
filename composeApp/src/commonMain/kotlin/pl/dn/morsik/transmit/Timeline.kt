package pl.dn.morsik.transmit

import pl.dn.morsik.morse.Morse
import pl.dn.morsik.ui.LetterUi

// Oś czasu sygnałów (zadanie 6). Czysta logika, bez Androida i bez czasu rzeczywistego:
// zamienia litery na listę kroków "włącz na X jednostek" / "przerwa X jednostek".
// Jednostka to najkrótszy odstęp (długość kropki); ile to milisekund, ustala suwak (170-400 ms).

/**
 * Jeden krok nadawania: sygnał włączony ([on] = true) albo przerwa, na [units] jednostek.
 *
 * [letter] i [symbol] mówią, do którego znaku (indeks litery w podglądzie, indeks w jej kodzie)
 * krok należy. Przerwa po znaku należy do tego znaku, więc ekran w przerwie nadal pokazuje
 * "bieżący znak", tylko blady (to właśnie opisuje TransmissionUi.signalOn).
 */
data class SignalStep(val on: Boolean, val units: Int, val letter: Int, val symbol: Int)

object Timeline {
    // Długości z alfabetu Morse'a, w jednostkach.
    const val DOT_UNITS = 1
    const val DASH_UNITS = 3
    const val SYMBOL_GAP_UNITS = 1 // przerwa między kropkami/kreskami w jednej literze
    const val LETTER_GAP_UNITS = 3 // przerwa między literami
    const val WORD_GAP_UNITS = 7 // przerwa między słowami

    /**
     * Buduje kroki dla całej wiadomości. Po ostatnim znaku nie ma przerwy (nie ma po co czekać).
     *
     * Przykład: "E E" (litera E = jedna kropka) daje: sygnał 1, przerwa 7, sygnał 1.
     */
    fun build(letters: List<LetterUi>): List<SignalStep> {
        val steps = mutableListOf<SignalStep>()
        letters.forEachIndexed { letterIndex, letter ->
            letter.code.forEachIndexed { symbolIndex, symbol ->
                val units = if (symbol == Morse.DASH) DASH_UNITS else DOT_UNITS
                steps += SignalStep(on = true, units = units, letter = letterIndex, symbol = symbolIndex)

                // Jaka przerwa następuje po tym znaku?
                val gap = when {
                    symbolIndex < letter.code.lastIndex -> SYMBOL_GAP_UNITS // dalej ta sama litera
                    letterIndex == letters.lastIndex -> 0 // koniec wiadomości
                    letters[letterIndex + 1].startsWord -> WORD_GAP_UNITS // następna litera zaczyna słowo
                    else -> LETTER_GAP_UNITS
                }
                if (gap > 0) steps += SignalStep(on = false, units = gap, letter = letterIndex, symbol = symbolIndex)
            }
        }
        return steps
    }

    /** Łączny czas nadawania w jednostkach, np. SOS = 27. Pomnóż przez długość jednostki, by dostać ms. */
    fun totalUnits(steps: List<SignalStep>): Int = steps.sumOf { it.units }
}
