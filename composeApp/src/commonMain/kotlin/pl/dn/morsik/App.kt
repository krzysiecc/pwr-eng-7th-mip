package pl.dn.morsik

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.dn.morsik.ui.HelpScreen
import pl.dn.morsik.ui.InfoScreen
import pl.dn.morsik.ui.MainScreen
import pl.dn.morsik.ui.MorsikTheme
import pl.dn.morsik.ui.MorsikViewModel
import pl.dn.morsik.ui.Screen
import pl.dn.morsik.ui.Strings

/**
 * Korzeń interfejsu - to wywołuje MainActivity.
 *
 * Łączy widoki (ekrany) z ViewModelem: czyta z niego stan i przekazuje mu kliknięcia.
 * Sama nic nie decyduje - logika jest w [MorsikViewModel], a ekrany tylko rysują.
 *
 * @param viewModel stan i akcje aplikacji; MainActivity podaje swój (z prawdziwymi kanałami),
 *   a domyślny, bez kanałów, wystarcza np. do podglądu
 * @param startDictation otwiera systemowe dyktowanie (wynik trafia prosto do ViewModelu)
 * @param appVersion numer wersji do ekranów "O aplikacji" i deklaracji dostępności
 */
@OptIn(ExperimentalComposeUiApi::class) // BackHandler jest jeszcze oznaczony jako eksperymentalny
@Composable
fun App(
    viewModel: MorsikViewModel = viewModel { MorsikViewModel() },
    startDictation: () -> Unit = {},
    appVersion: String = "",
) {
    // collectAsState = obserwuj StateFlow; każda nowa wartość przerysowuje ekran.
    val main by viewModel.main.collectAsState()
    val help by viewModel.help.collectAsState()
    val screen by viewModel.screen.collectAsState()

    // Systemowy przycisk/gest Wstecz na ekranach z menu wraca do ekranu głównego.
    // Na ekranie głównym (enabled = false) działa jak zwykle, czyli zamyka aplikację.
    BackHandler(enabled = screen != Screen.Main) { viewModel.back() }

    MorsikTheme {
        // Surface = białe tło na cały ekran w kolorze z motywu.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (screen) {
                Screen.Main -> MainScreen(
                    state = main,
                    // Referencja do funkcji (viewModel::onMessageChange) = to samo co { viewModel.onMessageChange(it) }.
                    onMessageChange = viewModel::onMessageChange,
                    onDictate = { if (!main.isTransmitting) startDictation() },
                    onStart = viewModel::startMessage,
                    onOpen = viewModel::open,
                    onChannelsChange = viewModel::onChannelsChange,
                    onUnitMsChange = viewModel::onUnitMsChange,
                    onStop = viewModel::stop,
                )
                Screen.Help -> HelpScreen(
                    state = help,
                    onSend = viewModel::sendCharacter,
                    onStop = viewModel::stop,
                    onBack = viewModel::back,
                )
                Screen.Accessibility -> InfoScreen(
                    title = Strings.Accessibility,
                    sections = Strings.accessibilityStatement(appVersion),
                    onBack = viewModel::back,
                )
                Screen.About -> InfoScreen(
                    title = Strings.About,
                    sections = Strings.about(appVersion),
                    onBack = viewModel::back,
                )
            }
        }
    }
}
