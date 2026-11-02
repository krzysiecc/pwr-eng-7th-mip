package pl.dn.morsik.morse

/**
 * Jeden znak wiadomości razem z kodem Morse'a.
 *
 * [code] to ciąg kropek i kresek, np. `".-"` dla A, albo `null`, gdy znaku nie ma w alfabecie
 * (taki znak pokazujemy w podglądzie, ale nie nadajemy).
 */
data class MorseLetter(val text: String, val code: String?) {
    val isKnown: Boolean get() = code != null
}

/** Słowo wiadomości, czyli znaki między białymi znakami. */
data class MorseWord(val letters: List<MorseLetter>) {
    /** `true`, gdy w słowie jest co nadać (przynajmniej jeden znak ma kod). */
    val isTransmittable: Boolean get() = letters.any { it.isKnown }
}

object Morse {
    const val DOT = '.'
    const val DASH = '-'

    /** Alfabet w kolejności do wyświetlenia: litery, polskie litery, cyfry, interpunkcja. */
    val alphabet: Map<Char, String> = linkedMapOf(
        'A' to ".-",
        'B' to "-...",
        'C' to "-.-.",
        'D' to "-..",
        'E' to ".",
        'F' to "..-.",
        'G' to "--.",
        'H' to "....",
        'I' to "..",
        'J' to ".---",
        'K' to "-.-",
        'L' to ".-..",
        'M' to "--",
        'N' to "-.",
        'O' to "---",
        'P' to ".--.",
        'Q' to "--.-",
        'R' to ".-.",
        'S' to "...",
        'T' to "-",
        'U' to "..-",
        'V' to "...-",
        'W' to ".--",
        'X' to "-..-",
        'Y' to "-.--",
        'Z' to "--..",

        'Ą' to ".-.-",
        'Ć' to "-.-..",
        'Ę' to "..-..",
        'Ł' to ".-..-",
        'Ń' to "--.--",
        'Ó' to "---.",
        'Ś' to "...-...",
        'Ź' to "--..-.",
        'Ż' to "--..-",

        '0' to "-----",
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----.",

        '.' to ".-.-.-",
        ',' to "--..--",
        '?' to "..--..",
        '!' to "-.-.--",
        '\'' to ".----.",
        '"' to ".-..-.",
        '/' to "-..-.",
        '(' to "-.--.",
        ')' to "-.--.-",
        '&' to ".-...",
        ':' to "---...",
        ';' to "-.-.-.",
        '=' to "-...-",
        '+' to ".-.-.",
        '-' to "-....-",
        '_' to "..--.-",
        '$' to "...-..-",
        '@' to ".--.-.",
    )

    /** Kod znaku bez względu na wielkość liter albo `null`, gdy znaku nie ma w alfabecie. */
    fun codeOf(char: Char): String? = alphabet[char.uppercaseChar()]

    /**
     * Dzieli tekst na słowa i znaki z kodem Morse'a.
     *
     * Słowa rozdziela dowolny ciąg białych znaków (spacje, tabulatory, nowe linie).
     * Znaki spoza alfabetu zostają w wyniku z `code == null`, a emoji zapisane jako para
     * surogatów to jeden znak, nie dwa.
     */
    fun encode(text: String): List<MorseWord> {
        val words = mutableListOf<MorseWord>()
        var letters = mutableListOf<MorseLetter>()
        var i = 0
        while (i < text.length) {
            val char = text[i]
            when {
                char.isWhitespace() -> {
                    if (letters.isNotEmpty()) {
                        words += MorseWord(letters)
                        letters = mutableListOf()
                    }
                    i += 1
                }
                char.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate() -> {
                    letters += MorseLetter(text.substring(i, i + 2), code = null)
                    i += 2
                }
                else -> {
                    letters += MorseLetter(char.toString(), codeOf(char))
                    i += 1
                }
            }
        }
        if (letters.isNotEmpty()) words += MorseWord(letters)
        return words
    }

    /**
     * Zapis tekstu kropkami i kreskami: znaki rozdziela spacja, słowa ` / `.
     * Znaki spoza alfabetu są pomijane, np. `"SOS"` daje `"... --- ..."`.
     */
    fun encodeToString(text: String): String =
        encode(text)
            .filter { it.isTransmittable }
            .joinToString(WORD_SEPARATOR) { word ->
                word.letters.mapNotNull { it.code }.joinToString(LETTER_SEPARATOR)
            }

    private const val LETTER_SEPARATOR = " "
    private const val WORD_SEPARATOR = " / "
}
