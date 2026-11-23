package pl.dn.morsik.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
private fun MainScreenPreviewContent(state: MainUiState) {
    MorsikTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MainScreen(
                state = state,
                onMessageChange = {},
                onDictate = {},
                onMenu = {},
                onChannelsChange = {},
                onUnitMsChange = {},
                onStop = {},
            )
        }
    }
}

@Preview(name = "Nadawanie", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainScreenTransmittingPreview() {
    MainScreenPreviewContent(MainUiState.Demo)
}

@Preview(name = "Bezczynny", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MainScreenIdlePreview() {
    MainScreenPreviewContent(MainUiState.Demo.copy(transmission = null))
}
