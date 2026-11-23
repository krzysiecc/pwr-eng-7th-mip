package pl.dn.morsik

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pl.dn.morsik.ui.MainScreen
import pl.dn.morsik.ui.MainUiState
import pl.dn.morsik.ui.MorsikTheme

/**
 * Korzeń interfejsu - to wywołuje MainActivity.
 *
 * Tu mieszka stan ekranu głównego i tu decydujemy, co się dzieje po kliknięciach.
 * Sam MainScreen tylko rysuje to, co dostanie, i zgłasza zdarzenia przez funkcje onXxx.
 */
@Composable
fun App() {
    // mutableStateOf = wartość, której zmiana każe Compose przerysować ekran.
    // remember = zachowaj tę wartość między kolejnymi wywołaniami App() (inaczej co klatkę byłby nowy, pusty stan).
    // `by` + getValue/setValue = można pisać `state` zamiast `state.value`.
    var state by remember { mutableStateOf(MainUiState()) }

    MorsikTheme {
        // Surface = białe tło na cały ekran w kolorze z motywu.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MainScreen(
                state = state,
                // `it` = domyślna nazwa jedynego parametru lambdy (tu: nowy tekst / nowe kanały / nowe ms).
                onMessageChange = { state = state.withMessage(it) },
                onDictate = {}, // TODO zadanie 11: dyktowanie (SpeechRecognizer)
                onMenu = {}, // TODO zadanie 12: menu i ekran Pomocy
                onChannelsChange = { state = state.copy(channels = it) },
                onUnitMsChange = { state = state.copy(unitMs = it) },
                // TODO zadanie 10: zatrzymać też silnik nadawania, gdy już będzie.
                onStop = { state = state.copy(transmission = null) },
            )
        }
    }
}
