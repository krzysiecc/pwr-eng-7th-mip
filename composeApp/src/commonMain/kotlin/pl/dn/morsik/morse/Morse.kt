package pl.dn.morsik.morse

// `code` = null dla znaków spoza alfabetu.
data class MorseLetter(val text: String, val code: String?) {
    val isKnown: Boolean get() = code != null
}

data class MorseWord(val letters: List<MorseLetter>) {
    val isTransmittable: Boolean get() = letters.any { it.isKnown }
}

object Morse {
    const val DOT = '.'
    const val DASH = '-'

    // Kolejność wpisów wyznacza kolejność na ekranie Pomocy.
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

    fun codeOf(char: Char): String? = alphabet[char.uppercaseChar()]

    fun encode(text: String): List<MorseWord> =
        text.split(WHITESPACE)
            .filter { it.isNotEmpty() }
            .map { word ->
                MorseWord(
                    word.map { char ->
                        MorseLetter(text = char.toString(), code = codeOf(char))
                    },
                )
            }

    // Znaki rozdziela spacja, słowa " / "; nieznane znaki są pomijane.
    fun encodeToString(text: String): String =
        encode(text)
            .filter { it.isTransmittable }
            .joinToString(WORD_SEPARATOR) { word ->
                word.letters.mapNotNull { it.code }
                    .joinToString(LETTER_SEPARATOR)
            }

    private const val LETTER_SEPARATOR = " "
    private const val WORD_SEPARATOR = " / "

    private val WHITESPACE = Regex("\\s+")
}
