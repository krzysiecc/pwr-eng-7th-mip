package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HelpUiStateTest {

    @Test
    fun groupsAreInTableOrder() {
        val titles = alphabetGroups().map { it.title }
        assertEquals(
            listOf(Strings.GroupLetters, Strings.GroupPolish, Strings.GroupDigits, Strings.GroupPunctuation),
            titles,
        )
    }

    @Test
    fun everyCharacterIsShownExactlyOnce() {
        val shown = alphabetGroups().flatMap { it.characters }.map { it.symbol }
        assertEquals(Morse.alphabet.keys.map { it.toString() }, shown)
    }

    @Test
    fun polishLettersHaveTheirOwnGroup() {
        val polish = alphabetGroups().first { it.title == Strings.GroupPolish }.characters.map { it.symbol }
        assertEquals(listOf("Ą", "Ć", "Ę", "Ł", "Ń", "Ó", "Ś", "Ź", "Ż"), polish)
    }

    @Test
    fun nothingIsActiveAtStart() {
        assertFalse(HelpUiState().isTransmitting)
        assertTrue(HelpUiState(activeSymbol = "A").isTransmitting)
    }

    @Test
    fun punctuationIsReadByName() {
        assertEquals("znak zapytania: kropka kropka kreska kreska kropka kropka", spokenLetter("?", "..--.."))
        // Każdy znak interpunkcyjny ma nazwę, a nie tylko sam symbol.
        val punctuation = alphabetGroups().first { it.title == Strings.GroupPunctuation }.characters
        for (letter in punctuation) assertNotEquals(letter.symbol, Strings.characterName(letter.symbol))
    }

    @Test
    fun boldMarkupBoldsOnlyTextBetweenStars() {
        val text = boldMarkup("Wpisz **wiadomość** i dotknij **Nadaj**.")
        assertEquals("Wpisz wiadomość i dotknij Nadaj.", text.text)
        assertEquals(listOf("wiadomość", "Nadaj"), text.spanStyles.map { text.text.substring(it.start, it.end) })
    }
}
