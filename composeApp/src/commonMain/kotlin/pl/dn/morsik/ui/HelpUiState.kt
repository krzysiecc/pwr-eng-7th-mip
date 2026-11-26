package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse

data class CharacterGroup(val title: String, val characters: List<LetterUi>)

fun alphabetGroups(): List<CharacterGroup> {
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

data class HelpUiState(
    val groups: List<CharacterGroup> = AllGroups,
    val activeSymbol: String? = null,
) {
    val isTransmitting: Boolean get() = activeSymbol != null

    companion object {
        private val AllGroups = alphabetGroups()
    }
}
