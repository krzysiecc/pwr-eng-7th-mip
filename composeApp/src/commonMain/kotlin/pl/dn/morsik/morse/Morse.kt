package pl.dn.morsik.morse

// Ten plik to czysta logika: zero Androida, zero Compose. Dzięki temu da się go
// przetestować zwykłymi testami jednostkowymi na komputerze (MorseTest.kt).

/**
 * Jeden znak wiadomości razem z kodem Morse'a.
 *
 * [code] to ciąg kropek i kresek, np. `".-"` dla A, albo `null`, gdy znaku nie ma w alfabecie
 * (np. `#` albo emoji - takich znaków nie nadajemy).
 *
 * `data class` = Kotlin sam generuje equals(), toString() i copy(), więc dwa obiekty
 * z tymi samymi polami są sobie równe (przydaje się w testach: assertEquals).
 */
data class MorseLetter(val text: String, val code: String?) {
    /** `true`, gdy znak jest w alfabecie. `get() = ...` liczy wartość przy każdym odczycie. */
    val isKnown: Boolean get() = code != null
}

/** Słowo wiadomości, czyli znaki między spacjami. */
data class MorseWord(val letters: List<MorseLetter>) {
    /** `true`, gdy w słowie jest co nadać (przynajmniej jeden znak ma kod). */
    val isTransmittable: Boolean get() = letters.any { it.isKnown }
}

/**
 * `object` = jedna jedyna instancja na całą aplikację (singleton).
 * Wywołujemy po prostu `Morse.encode("SOS")`, bez tworzenia obiektu.
 */
object Morse {
    // const val = stała znana już podczas kompilacji.
    const val DOT = '.'
    const val DASH = '-'

    /**
     * Alfabet Morse'a: znak -> kod.
     *
     * `linkedMapOf` pamięta kolejność dodawania, więc ekran Pomocy wyświetli znaki w tej kolejności:
     * litery, polskie litery, cyfry, interpunkcja. `'A' to ".-"` tworzy parę (klucz, wartość).
     */
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

        // Polskie litery (polski standard alfabetu Morse'a).
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
        '\'' to ".----.", // apostrof trzeba poprzedzić \, bo inaczej zamknąłby literał znakowy
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

    /**
     * Kod jednego znaku albo `null`, gdy znaku nie ma w alfabecie.
     * Alfabet ma tylko wielkie litery, więc najpierw zamieniamy np. 'ż' na 'Ż'.
     */
    fun codeOf(char: Char): String? = alphabet[char.uppercaseChar()]

    /**
     * Dzieli tekst na słowa, a słowa na znaki z kodem Morse'a.
     *
     * Przykład: `"Ala ma"` -> `[ [A, l, a], [m, a] ]` (każdy znak z kodem).
     *
     * Słowa rozdziela dowolny ciąg białych znaków (spacje, tabulatory, nowe linie),
     * więc `"SOS   SOS"` to nadal dwa słowa. Znaki spoza alfabetu zostają w wyniku z `code == null`.
     */
    fun encode(text: String): List<MorseWord> =
        text.split(WHITESPACE)                // "  ala  ma " -> ["", "ala", "ma", ""]
            .filter { it.isNotEmpty() }       // usuwamy puste kawałki z początku i końca -> ["ala", "ma"]
            .map { word ->                    // każde słowo...
                MorseWord(
                    word.map { char ->        // ...każdy jego znak zamieniamy na MorseLetter
                        MorseLetter(text = char.toString(), code = codeOf(char))
                    },
                )
            }

    /**
     * Zapis tekstu kropkami i kreskami w jednej linii: znaki rozdziela spacja, słowa ` / `.
     * Znaki spoza alfabetu są pomijane, np. `"SOS"` daje `"... --- ..."`.
     */
    fun encodeToString(text: String): String =
        encode(text)
            .filter { it.isTransmittable }                              // słowa typu "###" nic nie nadają
            .joinToString(WORD_SEPARATOR) { word ->                     // słowa łączymy " / "
                word.letters.mapNotNull { it.code }                     // bierzemy tylko znane kody
                    .joinToString(LETTER_SEPARATOR)                     // kody liter łączymy spacją
            }

    private const val LETTER_SEPARATOR = " "
    private const val WORD_SEPARATOR = " / "

    /** Wyrażenie regularne: `\s` = biały znak, `+` = jeden lub więcej pod rząd. */
    private val WHITESPACE = Regex("\\s+")
}
