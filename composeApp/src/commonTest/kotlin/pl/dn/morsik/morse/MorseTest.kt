package pl.dn.morsik.morse

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Testy jednostkowe kodera Morse'a (Morse.kt).
// Uruchamianie: w Android Studio zielona strzałka obok nazwy klasy/testu,
// albo w terminalu: ./gradlew :composeApp:testAndroidHostTest
//
// Każda funkcja z @Test to osobny test. assertEquals(oczekiwane, faktyczne) - test nie przechodzi,
// gdy wartości są różne. Ostatni (opcjonalny) argument to komunikat pokazywany przy błędzie.

class MorseTest {

    // --- codeOf: kod pojedynczego znaku ---

    @Test
    fun codeOfReturnsCodeForKnownCharacter() {
        assertEquals("...", Morse.codeOf('S'))
        assertEquals("-----", Morse.codeOf('0'))
        assertEquals(".-.-.-", Morse.codeOf('.'))
    }

    @Test
    fun codeOfIgnoresLetterCase() {
        assertEquals(Morse.codeOf('S'), Morse.codeOf('s'))
        assertEquals(Morse.codeOf('Ż'), Morse.codeOf('ż'))
    }

    @Test
    fun codeOfReturnsNullForUnknownCharacter() {
        assertNull(Morse.codeOf('#'))
        assertNull(Morse.codeOf(' '))
        assertNull(Morse.codeOf('é')) // litera spoza polskiego alfabetu
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

    // --- encodeToString: cała wiadomość jako kropki i kreski ---

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
    fun skipsUnknownCharactersWhenTransmitting() {
        assertEquals(".- -...", Morse.encodeToString("A#B"))
    }

    @Test
    fun dropsWordsWithNothingToTransmit() {
        val words = Morse.encode("A ### B")

        assertEquals(3, words.size) // encode zostawia słowo "###"...
        assertFalse(words[1].isTransmittable)
        assertEquals(".- / -...", Morse.encodeToString("A ### B")) // ...ale encodeToString je pomija
    }

    // --- encode: podział na słowa i znaki ---

    @Test
    fun treatsAnyWhitespaceRunAsSingleWordGap() {
        assertEquals("... --- ... / ... --- ...", Morse.encodeToString("  SOS \t\n  SOS  "))
        assertEquals(2, Morse.encode("SOS   SOS").size)
    }

    @Test
    fun keepsOriginalTextOfEveryCharacter() {
        val letters = Morse.encode("ała").single().letters

        // Tekst zostaje taki, jak wpisany (małe litery), kod jest ten sam co dla wielkich.
        assertEquals(listOf("a", "ł", "a"), letters.map { it.text })
        assertEquals(listOf(".-", ".-..-", ".-"), letters.map { it.code })
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
    fun emojiHasNoCodeAndIsNotTransmitted() {
        val letters = Morse.encode("A😀").single().letters

        assertEquals(".-", letters.first().code)
        // Emoji w Kotlinie to dwa znaki Char (tzw. para surogatów) - żaden nie ma kodu.
        assertTrue(letters.drop(1).none { it.isKnown })
        assertEquals(".-", Morse.encodeToString("A😀"))
    }

    // --- isKnown / isTransmittable ---

    @Test
    fun wordIsTransmittableWhenAnyLetterHasCode() {
        assertTrue(MorseWord(listOf(MorseLetter("#", null), MorseLetter("A", ".-"))).isTransmittable)
        assertFalse(MorseWord(listOf(MorseLetter("#", null))).isTransmittable)
        assertFalse(MorseWord(emptyList()).isTransmittable)
    }

    // --- sam alfabet ---

    @Test
    fun alphabetContainsAllRequiredCharacters() {
        val required = ('A'..'Z') + "ĄĆĘŁŃÓŚŹŻ".toList() + ('0'..'9') + ".,?!".toList()
        val missing = required.filter { it !in Morse.alphabet }
        assertTrue(missing.isEmpty(), "brakuje znaków: $missing")
    }

    @Test
    fun alphabetKeepsDisplayOrder() {
        // Ekran Pomocy pokaże znaki w kolejności z mapy - zaczynamy od A, B, C.
        assertEquals(listOf('A', 'B', 'C'), Morse.alphabet.keys.take(3))
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
        // groupBy grupuje znaki po kodzie; grupa z więcej niż jednym znakiem = duplikat.
        val duplicates = Morse.alphabet.entries
            .groupBy({ it.value }, { it.key })
            .filterValues { it.size > 1 }
        assertTrue(duplicates.isEmpty(), "ten sam kod dla kilku znaków: $duplicates")
    }
}
