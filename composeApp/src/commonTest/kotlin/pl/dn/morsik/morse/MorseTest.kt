package pl.dn.morsik.morse

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MorseTest {

    @Test
    fun encodesSos() {
        assertEquals("... --- ...", Morse.encodeToString("SOS"))
    }

    @Test
    fun ignoresLetterCase() {
        assertEquals(Morse.encodeToString("SOS"), Morse.encodeToString("sos"))
        assertEquals(Morse.encodeToString("ŻÓŁW"), Morse.encodeToString("żółw"))
    }

    @Test
    fun encodesPolishLetters() {
        val expected = mapOf(
            'Ą' to ".-.-",
            'Ć' to "-.-..",
            'Ę' to "..-..",
            'Ł' to ".-..-",
            'Ń' to "--.--",
            'Ó' to "---.",
            'Ś' to "...-...",
            'Ź' to "--..-.",
            'Ż' to "--..-",
        )
        for ((letter, code) in expected) {
            assertEquals(code, Morse.codeOf(letter), "wielka litera $letter")
            assertEquals(code, Morse.codeOf(letter.lowercaseChar()), "mała litera ${letter.lowercaseChar()}")
        }
    }

    @Test
    fun encodesPolishSentence() {
        assertEquals(
            "--..- --- .-..- ---. .-- / --. ..-.. ...-... .-.. .-.-",
            Morse.encodeToString("żołów gęślą"),
        )
    }

    @Test
    fun encodesDigitsAndPunctuation() {
        assertEquals("--... ...-- -.-.--", Morse.encodeToString("73!"))
        assertEquals(".---- .-.-.- / ..--..", Morse.encodeToString("1. ?"))
    }

    @Test
    fun separatesWordsWithSlash() {
        assertEquals("... --- ... / ... --- ...", Morse.encodeToString("SOS SOS"))
    }

    @Test
    fun treatsAnyWhitespaceRunAsSingleWordGap() {
        assertEquals("... --- ... / ... --- ...", Morse.encodeToString("  SOS \t\n  SOS  "))
        assertEquals(2, Morse.encode("SOS   SOS").size)
    }

    @Test
    fun emptyOrBlankTextGivesNothing() {
        assertTrue(Morse.encode("").isEmpty())
        assertTrue(Morse.encode(" \n\t ").isEmpty())
        assertEquals("", Morse.encodeToString(""))
        assertEquals("", Morse.encodeToString("   "))
    }

    @Test
    fun keepsUnknownCharactersWithoutCode() {
        val letters = Morse.encode("A#B").single().letters

        assertEquals(listOf("A", "#", "B"), letters.map { it.text })
        assertEquals(".-", letters[0].code)
        assertNull(letters[1].code)
        assertFalse(letters[1].isKnown)
        assertEquals("-...", letters[2].code)
    }

    @Test
    fun skipsUnknownCharactersWhenTransmitting() {
        assertEquals(".- -...", Morse.encodeToString("A#B"))
    }

    @Test
    fun treatsEmojiAsSingleUnknownCharacter() {
        val letters = Morse.encode("A😀").single().letters

        assertEquals(listOf("A", "😀"), letters.map { it.text })
        assertNull(letters[1].code)
    }

    @Test
    fun dropsWordsWithNothingToTransmit() {
        val words = Morse.encode("A ### B")

        assertEquals(3, words.size)
        assertFalse(words[1].isTransmittable)
        assertEquals(".- / -...", Morse.encodeToString("A ### B"))
    }

    @Test
    fun alphabetContainsAllRequiredCharacters() {
        val required = ('A'..'Z') + "ĄĆĘŁŃÓŚŹŻ".toList() + ('0'..'9') + ".,?!".toList()
        val missing = required.filter { it !in Morse.alphabet }
        assertTrue(missing.isEmpty(), "brakuje znaków: $missing")
    }

    @Test
    fun codesUseOnlyDotsAndDashes() {
        for ((char, code) in Morse.alphabet) {
            assertTrue(code.isNotEmpty(), "pusty kod dla $char")
            assertTrue(code.all { it == Morse.DOT || it == Morse.DASH }, "zły kod dla $char: $code")
        }
    }

    @Test
    fun codesAreUnique() {
        val duplicates = Morse.alphabet.entries
            .groupBy({ it.value }, { it.key })
            .filterValues { it.size > 1 }
        assertTrue(duplicates.isEmpty(), "ten sam kod dla kilku znaków: $duplicates")
    }
}
