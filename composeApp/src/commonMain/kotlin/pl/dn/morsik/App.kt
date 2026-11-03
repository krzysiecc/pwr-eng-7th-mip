package pl.dn.morsik

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import pl.dn.morsik.ui.MainScreen
import pl.dn.morsik.ui.MainUiState
import pl.dn.morsik.ui.MorsikTheme

@Composable
fun App() {
    var state by remember { mutableStateOf(MainUiState()) }

    MorsikTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MainScreen(
                state = state,
                onMessageChange = { state = state.withMessage(it) },
                onDictate = {},
                onMenu = {},
                onChannelsChange = { state = state.copy(channels = it) },
                onUnitMsChange = { state = state.copy(unitMs = it) },
                onStop = { state = state.copy(transmission = null) },
            )
        }
    }
}
