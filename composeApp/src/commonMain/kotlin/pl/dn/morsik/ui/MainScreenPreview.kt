package pl.dn.morsik.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Podglądy ekranu dla Android Studio: otwórz ten plik i włącz widok "Split" albo "Design"
// (prawy górny róg edytora). Nie trafiają do aplikacji na telefonie, służą tylko do oglądania UI.

/** Wspólna "rama" podglądu: motyw + tło + ekran z podanym stanem i pustymi akcjami. */
@Composable
private fun MainScreenPreviewContent(state: MainUiState) {
    MorsikTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MainScreen(
                state = state,
                onMessageChange = {},
                onDictate = {},
                onStart = {},
                onOpen = {},
                onMenuOpenChange = {},
                onChannelsChange = {},
                onUnitMsChange = {},
                onStop = {},
            )
        }
    }
}

// widthDp/heightDp = rozmiar typowego telefonu. fontScale = ustawienie "rozmiar czcionki" w systemie.

@Preview(name = "nadawanie", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainScreenTransmittingPreview() {
    MainScreenPreviewContent(MainUiState.Demo)
}

@Preview(name = "bezczynny", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainScreenIdlePreview() {
    MainScreenPreviewContent(MainUiState.Demo.copy(transmission = null))
}

/** Sprawdzenie, czy napisy mieszczą się w swoich ramkach przy dużej czcionce i długich kodach (Ś, Ź). */
@Preview(name = "duża czcionka", showBackground = true, widthDp = 360, heightDp = 780, fontScale = 1.5f)
@Composable
private fun MainScreenLargeFontPreview() {
    MainScreenPreviewContent(MainUiState().withMessage("Gęślą jaźń"))
}

// ---- Pozostałe ekrany ----

@Preview(name = "pomoc", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HelpScreenPreview() {
    MorsikTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HelpScreen(state = HelpUiState(activeSymbol = "S"), onSend = {}, onStop = {}, onBack = {})
        }
    }
}

@Preview(name = "pomoc, duża czcionka", showBackground = true, widthDp = 360, heightDp = 780, fontScale = 2f)
@Composable
private fun HelpScreenLargeFontPreview() {
    MorsikTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HelpScreen(state = HelpUiState(), onSend = {}, onStop = {}, onBack = {})
        }
    }
}

@Preview(name = "deklaracja dostępności", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AccessibilityScreenPreview() {
    MorsikTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            InfoScreen(title = Strings.Accessibility, sections = Strings.accessibilityStatement("1.0.0"), onBack = {})
        }
    }
}
