package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse

// Dane ekranu Pomocy (zadanie 12). Jak MainUiState.kt: bez @Composable, testowane zwykłymi testami.

/** Jedna grupa w tabeli znaków, np. "Cyfry" z kafelkami 0-9. */
data class CharacterGroup(val title: String, val characters: List<LetterUi>)

/**
 * Dzieli alfabet z [Morse.alphabet] na grupy w kolejności z tabeli:
 * litery A-Z, polskie litery, cyfry, znaki interpunkcyjne.
 */
fun alphabetGroups(): List<CharacterGroup> {
    // groupBy zachowuje kolejność, w jakiej grupy pojawiają się po raz pierwszy w alfabecie.
    val groups = Morse.alphabet.entries.groupBy { (char, _) ->
        when {
            char in 'A'..'Z' -> Strings.GroupLetters
            char.isLetter() -> Strings.GroupPolish
            char.isDigit() -> Strings.GroupDigits
            else -> Strings.GroupPunctuation
        }
    }
    return groups.map { (title, entries) ->
        CharacterGroup(title, entries.map { (char, code) -> LetterUi(char.toString(), code) })
    }
}

/**
 * Stan ekranu Pomocy.
 *
 * [activeSymbol] = znak nadawany w tej chwili (podświetlony kafelek) albo `null`.
 */
data class HelpUiState(
    val groups: List<CharacterGroup> = AllGroups,
    val activeSymbol: String? = null,
) {
    val isTransmitting: Boolean get() = activeSymbol != null

    companion object {
        /** Alfabet się nie zmienia, więc grupy liczymy raz na całą aplikację. */
        private val AllGroups = alphabetGroups()
    }
}
