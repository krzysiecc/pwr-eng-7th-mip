package pl.dn.morsik.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Testy funkcji previewLetters (MainUiState.kt), czyli tego, jakie kafelki pokaże podgląd pod polem tekstu.

class PreviewLettersTest {

    @Test
    fun showsEveryLetterWithItsCode() {
        assertEquals(
            listOf(LetterUi("S", "..."), LetterUi("O", "---"), LetterUi("S", "...")),
            previewLetters("SOS"),
        )
    }

    @Test
    fun showsLowercaseAndPolishLettersInUppercase() {
        assertEquals(listOf("Ż", "Ó", "Ł", "W"), previewLetters("żółw").map { it.symbol })
    }

    @Test
    fun marksFirstLetterOfEveryNextWord() {
        val letters = previewLetters("ala  ma")

        assertEquals(listOf("A", "L", "A", "M", "A"), letters.map { it.symbol })
        assertEquals(listOf(false, false, false, true, false), letters.map { it.startsWord })
    }

    @Test
    fun leavesOutCharactersThatAreNotTransmitted() {
        assertEquals(listOf("A", "B"), previewLetters("A#B").map { it.symbol })
        assertEquals(listOf("A", "B"), previewLetters("A 😀 B").map { it.symbol })
        // Słowo z samego emoji znika, więc B jest początkiem drugiego (a nie trzeciego) słowa.
        assertEquals(listOf(false, true), previewLetters("A 😀 B").map { it.startsWord })
    }

    @Test
    fun firstVisibleLetterNeverStartsWord() {
        // Nawet gdy pierwsze słowo nie ma nic do nadania, przed pierwszym kafelkiem nie ma odstępu.
        assertEquals(listOf(false), previewLetters("### A").map { it.startsWord })
        // Znak bez kodu na początku słowa nie psuje oznaczenia: początkiem słowa jest "B".
        assertEquals(listOf(false, true), previewLetters("A #B").map { it.startsWord })
    }

    @Test
    fun emptyMessageHasNoLetters() {
        assertTrue(previewLetters("").isEmpty())
        assertTrue(previewLetters("   ").isEmpty())
    }
}
