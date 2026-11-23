package pl.dn.morsik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ekran Pomocy (zadanie 12): krótka instrukcja i tabela wszystkich znaków.
 * Dotknięcie znaku nadaje go od razu, na kanałach i z szybkością ustawionymi na ekranie głównym.
 *
 * @param onSend dotknięcie kafelka ze znakiem
 */
@Composable
fun HelpScreen(
    state: HelpUiState,
    onSend: (LetterUi) -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding().semantics { paneTitle = Strings.Help }) {
        ScreenTopBar(title = Strings.Help, onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))

        // Siatka, która sama dobiera liczbę kolumn: tyle, ile zmieści się kafelków o szerokości min. 96 dp
        // (3 na zwykłym telefonie pionowo, więcej poziomo). Jak LazyRow tworzy tylko widoczne elementy.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            fullWidth { SectionTitle(Strings.HowToTitle) }
            Strings.HowToSteps.forEachIndexed { index, step ->
                fullWidth { Text("${index + 1}. $step", style = MaterialTheme.typography.bodyLarge, color = MorsikInk) }
            }

            fullWidth { SectionTitle(Strings.AlphabetTitle, Modifier.padding(top = 12.dp)) }
            fullWidth { Text(Strings.AlphabetHint, style = MaterialTheme.typography.bodyLarge, color = MorsikInk) }

            for (group in state.groups) {
                fullWidth { SectionLabel(group.title.uppercase(), Modifier.padding(top = 8.dp)) }
                // key = symbol: Compose rozpoznaje kafelek po znaku, a nie po pozycji na liście.
                items(group.characters, key = { it.symbol }) { letter ->
                    CharacterTile(
                        letter = letter,
                        active = letter.symbol == state.activeSymbol,
                        onClick = { onSend(letter) },
                    )
                }
            }
        }

        // Ten sam Stop co na ekranie głównym. Najdłuższy znak (Ś) przy najwolniejszej szybkości trwa ok. 6 s,
        // a WCAG 2.2.2 wymaga możliwości zatrzymania wszystkiego, co miga dłużej niż 5 s.
        StopButton(enabled = state.isTransmitting, onClick = onStop, modifier = Modifier.padding(16.dp))
    }
}

/** Element siatki na całą szerokość (nagłówek, akapit), a nie w jednej kolumnie. */
private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

/** Nagłówek sekcji ekranu Pomocy, np. "Jak korzystać". */
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        color = MorsikInk,
    )
}

/**
 * Kafelek z jednym znakiem: znak i jego kod. Nadawany znak jest zielony i ma ciemną obwódkę
 * (obwódka, żeby nie odróżniał się tylko kolorem - WCAG 1.4.1).
 */
@Composable
private fun CharacterTile(letter: LetterUi, active: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(if (active) MorsikGreen else MorsikPink)
            .then(if (active) Modifier.border(3.dp, MorsikInk, shape) else Modifier)
            // onClickLabel: TalkBack powie "dwukrotnie dotknij, aby nadać".
            .clickable(onClickLabel = Strings.SendAction, role = Role.Button, onClick = onClick)
            // Jeden opis na cały kafelek: "Ą: kropka kreska kropka kreska" (i "nadawanie", gdy aktywny).
            .semantics {
                contentDescription = spokenLetter(letter.symbol, letter.code)
                if (active) stateDescription = Strings.SendingNow
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        // clearAndSetSemantics {}: napisy w środku już są w opisie kafelka, TalkBack nie czyta ich drugi raz.
        Text(
            text = letter.symbol,
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.titleLarge,
            color = MorsikInk,
        )
        Text(
            text = displayCode(letter.code),
            modifier = Modifier.clearAndSetSemantics {},
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MorsikInk,
            textAlign = TextAlign.Center, // długie kody (np. $) przy dużej czcionce zawijają się na środku
        )
    }
}
