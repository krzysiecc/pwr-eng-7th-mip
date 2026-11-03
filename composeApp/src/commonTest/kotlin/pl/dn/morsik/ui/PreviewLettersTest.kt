package pl.dn.morsik.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        assertEquals(listOf(false, true), previewLetters("A 😀 B").map { it.startsWord })
    }

    @Test
    fun emptyMessageHasNoLetters() {
        assertTrue(previewLetters("").isEmpty())
        assertTrue(previewLetters("   ").isEmpty())
    }

    @Test
    fun typingUpdatesMessageAndLettersWithoutStartingTransmission() {
        val state = MainUiState().withMessage("ok")

        assertEquals("ok", state.message)
        assertEquals(listOf(LetterUi("O", "---"), LetterUi("K", "-.-")), state.letters)
        assertNull(state.transmission)
        assertNull(state.activeLetter)
    }
}
