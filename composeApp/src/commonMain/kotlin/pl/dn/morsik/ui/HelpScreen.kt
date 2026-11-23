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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    // Obie zakładki startują zwinięte. rememberSaveable = stan przetrwa obrót ekranu.
    var howToExpanded by rememberSaveable { mutableStateOf(false) }
    var alphabetExpanded by rememberSaveable { mutableStateOf(false) }

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
            fullWidth { ExpandableTitle(Strings.HowToTitle, howToExpanded) { howToExpanded = !howToExpanded } }
            if (howToExpanded) {
                Strings.HowToSteps.forEachIndexed { index, step ->
                    fullWidth {
                        Text(
                            // Numer też jest pogrubiony, żeby punkty łatwo było odróżnić.
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${index + 1}. ") }
                                append(boldMarkup(step))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MorsikInk,
                        )
                    }
                }
            }

            fullWidth { ExpandableTitle(Strings.AlphabetTitle, alphabetExpanded, Modifier.padding(top = 4.dp)) { alphabetExpanded = !alphabetExpanded } }
            if (alphabetExpanded) {
                fullWidth { Text(boldMarkup(Strings.AlphabetHint), style = MaterialTheme.typography.bodyMedium, color = MorsikInk) }

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

/**
 * Zamienia tekst z `**pogrubieniem**` na [AnnotatedString]: słowa między parami ** są pogrubione,
 * reszta ma zwykłą wagę. Czytnik ekranu czyta tekst bez gwiazdek.
 */
internal fun boldMarkup(text: String): AnnotatedString = buildAnnotatedString {
    // split("**"): co drugi kawałek (indeksy nieparzyste) leży między gwiazdkami.
    text.split("**").forEachIndexed { index, part ->
        if (index % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) } else append(part)
    }
}

/**
 * Nagłówek rozwijanej zakładki: tytuł i strzałka. Cały pasek jest przyciskiem;
 * TalkBack czyta "Jak korzystać, zwinięte, nagłówek, przycisk" i "dwukrotnie dotknij, aby rozwinąć".
 */
@Composable
private fun ExpandableTitle(title: String, expanded: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(MorsikGrey)
            .clickable(
                onClickLabel = if (expanded) Strings.Collapse else Strings.Expand,
                role = Role.Button,
                onClick = onToggle,
            )
            .semantics {
                heading()
                stateDescription = if (expanded) Strings.Expanded else Strings.Collapsed
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MorsikInk,
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null, // stan mówi stateDescription
            tint = MorsikInk,
        )
    }
}

/**
 * Kafelek z jednym znakiem: znak i jego kod. Nadawany znak jest zielony i ma ciemną obwódkę
 * (obwódka, żeby nie odróżniał się tylko kolorem - WCAG 1.4.1).
 */
@Composable
private fun CharacterTile(letter: LetterUi, active: Boolean, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    val textColor = if (active) Color.White else MorsikInk // biały na zielonym
    Column(
        modifier = Modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(if (active) MorsikGreen else MorsikGrey)
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
            color = textColor,
        )
        Text(
            text = displayCode(letter.code),
            modifier = Modifier.clearAndSetSemantics {},
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center, // długie kody (np. $) przy dużej czcionce zawijają się na środku
        )
    }
}
