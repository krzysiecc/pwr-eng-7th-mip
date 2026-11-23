package pl.dn.morsik.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Górny pasek ekranów otwieranych z menu: przycisk Wstecz i tytuł ekranu.
 *
 * Tytuł jest oznaczony jako nagłówek (heading), więc użytkownik TalkBacka może przeskakiwać
 * między nagłówkami zamiast słuchać całego ekranu po kolei.
 */
@Composable
internal fun ScreenTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Ten sam wygląd co przycisk menu na ekranie głównym (spójność, WCAG 3.2.4).
        FilledIconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MorsikPink, contentColor = MorsikInk),
        ) {
            // AutoMirrored = strzałka sama się odwraca w językach pisanych od prawej do lewej.
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.Back)
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            color = MorsikInk,
        )
    }
}

/**
 * Ekran z samym tekstem: deklaracja dostępności i "O aplikacji".
 *
 * paneTitle = przy wejściu na ekran TalkBack ogłasza jego tytuł, więc użytkownik niewidomy
 * wie, że ekran się zmienił.
 */
@Composable
fun InfoScreen(title: String, sections: List<InfoSection>, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding().semantics { paneTitle = title }) {
        ScreenTopBar(title = title, onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()) // długi tekst i duża czcionka: przewijamy
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (section in sections) {
                if (section.title != null) {
                    Text(
                        text = section.title,
                        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MorsikInk,
                    )
                }
                for (paragraph in section.paragraphs) {
                    Text(
                        text = if (section.bullets) "•  $paragraph" else paragraph,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MorsikInk,
                    )
                }
            }
        }
    }
}
