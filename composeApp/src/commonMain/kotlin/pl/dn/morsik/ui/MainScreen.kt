package pl.dn.morsik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================================
// Ekran główny. Jak czytać ten plik:
//
// * Funkcja z adnotacją @Composable opisuje kawałek interfejsu. Compose wywołuje ją ponownie
//   ("rekompozycja") za każdym razem, gdy zmienią się jej parametry, i rysuje ekran od nowa.
// * Ekran NIE trzyma własnego stanu. Dostaje gotowy [MainUiState] i funkcje "onCośtam",
//   które woła, gdy użytkownik coś zrobi. Stan zmienia App.kt. To się nazywa "state hoisting"
//   i pozwala pokazać ekran w podglądzie (MainScreenPreview.kt) z dowolnymi danymi.
// * Modifier to łańcuch ustawień elementu: rozmiar, odstępy, tło, kliknięcia...
//   KOLEJNOŚĆ MA ZNACZENIE: `.padding(8.dp).background(...)` daje odstęp NA ZEWNĄTRZ tła,
//   a `.background(...).padding(8.dp)` odstęp WEWNĄTRZ tła.
// * Column = elementy jeden pod drugim, Row = obok siebie, Box = jeden na drugim.
// =============================================================================================

/**
 * Cały ekran główny: przewijana część z treścią i przycisk Stop przyklejony na dole.
 *
 * @param state wszystko, co ma być widać na ekranie
 * @param onMessageChange wołane przy każdej zmianie tekstu w polu wiadomości
 * @param onDictate kliknięcie mikrofonu
 * @param onMenu kliknięcie przycisku menu
 * @param onChannelsChange włączenie/wyłączenie kanału (latarka, wibracja, dźwięk)
 * @param onUnitMsChange przesunięcie suwaka szybkości (nowa wartość w ms)
 * @param onStop kliknięcie Stop
 */
@Composable
fun MainScreen(
    state: MainUiState,
    onMessageChange: (String) -> Unit,
    onDictate: () -> Unit,
    onMenu: () -> Unit,
    onChannelsChange: (ChannelsUi) -> Unit,
    onUnitMsChange: (Int) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // safeDrawingPadding = nie wchodzimy pod pasek stanu, wycięcie aparatu ani klawiaturę.
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .weight(1f) // zajmij całe wolne miejsce nad przyciskiem Stop
                .verticalScroll(rememberScrollState()) // na małym ekranie da się przewinąć
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), // 12 dp odstępu między sekcjami
        ) {
            Header(onMenu = onMenu)
            MessageField(message = state.message, onMessageChange = onMessageChange, onDictate = onDictate)
            LetterPreview(letters = state.letters, activeLetter = state.transmission?.activeLetter)
            StatusLine(state = state)
            SignalIndicator(signal = state.activeSignal, signalOn = state.transmission?.signalOn == true)
            ChannelToggles(channels = state.channels, onChange = onChannelsChange)
            SpeedControl(unitMs = state.unitMs, onUnitMsChange = onUnitMsChange)
        }
        StopButton(enabled = state.isTransmitting, onClick = onStop, modifier = Modifier.padding(16.dp))
    }
}

/** Górny pasek: nazwa aplikacji, plakietka z nazwą zapisaną Morse'em i przycisk menu. */
@Composable
private fun Header(onMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lewa część (nazwa + plakietka) zajmuje całe miejsce oprócz przycisku menu,
        // dzięki czemu przycisk zawsze jest przy prawej krawędzi.
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(Strings.AppName, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = Strings.AppNameMorse,
                modifier = Modifier
                    // fill = false: plakietka ma swoją naturalną szerokość, ale skurczy się,
                    // gdy brakuje miejsca (np. duża czcionka w ustawieniach telefonu)...
                    .weight(1f, fill = false)
                    .background(MorsikRed, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                    // ...a czytnik ekranu ją pomija - to tylko ozdoba, nazwa jest już obok.
                    .clearAndSetSemantics {},
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1, // zawsze jedna linia...
                overflow = TextOverflow.Ellipsis, // ...a jeśli się nie mieści, na końcu "…" zamiast ucięcia
            )
        }
        // FilledIconButton sam pilnuje minimalnego pola dotyku 48 dp i efektu kliknięcia.
        FilledIconButton(
            onClick = onMenu,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MorsikPink, contentColor = MorsikInk),
        ) {
            // contentDescription = co powie TalkBack o tym przycisku.
            Icon(Icons.Filled.Menu, contentDescription = Strings.Menu)
        }
    }
}

/** Mały, przygaszony nagłówek sekcji, np. "WIADOMOŚĆ". */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MorsikInkMuted,
    )
}

/** Pole do wpisania wiadomości i przycisk dyktowania obok. */
@Composable
private fun MessageField(message: String, onMessageChange: (String) -> Unit, onDictate: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(Strings.MessageLabel)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = message, // co jest wpisane - pole samo tego nie pamięta, bierze z parametru
                onValueChange = onMessageChange, // użytkownik coś wpisał -> przekazujemy nowy tekst wyżej
                modifier = Modifier.weight(1f), // pole zajmuje całą szerokość oprócz przycisku mikrofonu
                placeholder = { Text(Strings.MessagePlaceholder, color = MorsikInkMuted) },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MorsikPink,
                    unfocusedContainerColor = MorsikPink,
                    focusedIndicatorColor = Color.Transparent, // bez kreski pod polem
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MorsikInk,
                ),
            )
            FilledIconButton(
                onClick = onDictate,
                modifier = Modifier.size(56.dp), // tej samej wysokości co pole tekstowe
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MorsikInk, contentColor = Color.White),
            ) {
                Icon(Icons.Filled.Mic, contentDescription = Strings.Dictate)
            }
        }
    }
}

/**
 * Poziomy pasek kafelków: każda litera wiadomości z kodem pod spodem.
 * Litera nadawana w tej chwili ([activeLetter]) jest zielona.
 */
@Composable
private fun LetterPreview(letters: List<LetterUi>, activeLetter: Int?) {
    // Pozycja przewinięcia paska. `remember...` = przetrwa rekompozycję, nie zeruje się co klatkę.
    val listState = rememberLazyListState()

    // LaunchedEffect uruchamia blok w tle, gdy zmieni się któryś z kluczy (liczba liter lub aktywna litera).
    // Przewijamy tak, żeby było widać literę nadawaną, a przy pisaniu - ostatnią wpisaną.
    LaunchedEffect(letters.size, activeLetter) {
        val target = activeLetter ?: letters.lastIndex
        if (target >= 0) listState.animateScrollToItem(target)
    }

    // LazyRow tworzy tylko te kafelki, które są widoczne - nie zwalnia przy długiej wiadomości.
    LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(letters) { index, letter ->
            val active = index == activeLetter
            Column(
                modifier = Modifier
                    .padding(start = if (letter.startsWord) 16.dp else 0.dp) // większy odstęp między słowami
                    // Minimalna szerokość zamiast stałej: kafelek rośnie, gdy kod jest długi
                    // (np. Ś = "· · · – · · ·" nie mieściło się w stałych 96 dp).
                    .widthIn(min = 72.dp)
                    .background(if (active) MorsikGreen else MorsikPink, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    // Dla TalkBack cały kafelek to jeden element z opisem "O: kreska kreska kreska".
                    .clearAndSetSemantics { contentDescription = spokenLetter(letter.symbol, letter.code) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(letter.symbol, style = MaterialTheme.typography.titleMedium, color = MorsikInk)
                Text(
                    text = displayCode(letter.code),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MorsikInk,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Linia pod podglądem: "GOTOWY DO NADAWANIA" albo "NADAWANIE · LITERA O · ZNAK 2 Z 3". */
@Composable
private fun StatusLine(state: MainUiState) {
    val letter = state.activeLetter
    val transmission = state.transmission
    val text = if (letter != null && transmission != null) {
        // activeSymbol liczy się od 0, a człowiek liczy od 1 - stąd + 1.
        Strings.status(letter.symbol, transmission.activeSymbol + 1, letter.code.length)
    } else {
        Strings.StatusIdle
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (transmission != null) {
            // Zielona kropka "na żywo" przed tekstem.
            Box(Modifier.size(8.dp).background(MorsikGreenDark, CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MorsikInkMuted,
            textAlign = TextAlign.Center, // przy dużej czcionce tekst zawija się i zostaje wyśrodkowany
        )
    }
}

/**
 * Duże koło na środku: kropka albo kreska, zsynchronizowana z sygnałem.
 * Gdy sygnał jest wyłączony (przerwa), znak jest blady.
 */
@Composable
private fun SignalIndicator(signal: Signal?, signalOn: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MorsikPink, CircleShape)
                .border(3.dp, MorsikGreenDark, CircleShape),
            contentAlignment = Alignment.Center, // zawartość na środku koła
        ) {
            // Wspólny wygląd znaku: zielony, pełny gdy sygnał trwa, blady w przerwie.
            val mark = Modifier.alpha(if (signalOn) 1f else 0.25f)
            // `when` = rozbudowany switch. Musi obsłużyć wszystkie przypadki, także null.
            when (signal) {
                Signal.Dot -> Box(mark.size(48.dp).background(MorsikGreenDark, CircleShape))
                Signal.Dash -> Box(mark.size(width = 120.dp, height = 36.dp).background(MorsikGreenDark, CircleShape))
                null -> Unit // nic nie rysujemy
            }
        }
        Text(
            text = when (signal) {
                Signal.Dot -> Strings.SignalDot
                Signal.Dash -> Strings.SignalDash
                null -> Strings.SignalIdle
            },
            style = MaterialTheme.typography.titleLarge,
            color = MorsikInk,
            textAlign = TextAlign.Center,
        )
    }
}

/** Trzy przełączniki kanałów obok siebie, każdy tej samej szerokości. */
@Composable
private fun ChannelToggles(channels: ChannelsUi, onChange: (ChannelsUi) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // weight(1f) dla każdego = dzielą szerokość po równo.
        // channels.copy(flashlight = it) = te same ustawienia, tylko latarka zmieniona na nową wartość.
        ChannelToggle(
            label = Strings.Flashlight,
            icon = Icons.Filled.FlashlightOn,
            selected = channels.flashlight,
            onChange = { onChange(channels.copy(flashlight = it)) },
            modifier = Modifier.weight(1f),
        )
        ChannelToggle(
            label = Strings.Vibration,
            icon = Icons.Filled.Vibration,
            selected = channels.vibration,
            onChange = { onChange(channels.copy(vibration = it)) },
            modifier = Modifier.weight(1f),
        )
        ChannelToggle(
            label = Strings.Sound,
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            selected = channels.sound,
            onChange = { onChange(channels.copy(sound = it)) },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Jeden przełącznik kanału: ikona i podpis. Włączony = zielony z ptaszkiem, wyłączony = różowy.
 * Ptaszek jest po to, żeby stan był widoczny nie tylko kolorem (np. dla osób nierozróżniających kolorów).
 */
@Composable
private fun ChannelToggle(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) MorsikInk else MorsikInkMuted
    Column(
        modifier = modifier
            .heightIn(min = 64.dp) // wszystkie trzy tej samej wysokości, także gdy jeden podpis się zawinie
            .clip(MaterialTheme.shapes.medium) // przycina też efekt kliknięcia (ripple) do zaokrąglonych rogów
            .background(if (selected) MorsikGreen else MorsikPink)
            // toggleable = kliknięcie przełącza wartość; Role.Switch mówi TalkBackowi "przełącznik, włączony".
            .toggleable(value = selected, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        // Ikona jest ozdobą (contentDescription = null), bo podpis pod nią mówi to samo.
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, textAlign = TextAlign.Center)
        }
    }
}

/** Suwak długości jednostki: od 400 ms (wolno, lewo) do 150 ms (szybko, prawo). */
@Composable
private fun SpeedControl(unitMs: Int, onUnitMsChange: (Int) -> Unit) {
    val valueText = Strings.speedValue(unitMs)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(Strings.SpeedLabel, Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.labelMedium, color = MorsikInk)
        }
        Slider(
            // Suwak jest "odwrócony" (w prawo = mniej ms), wyjaśnienie w MainUiState.sliderPosition.
            value = MainUiState.sliderPosition(unitMs),
            onValueChange = { onUnitMsChange(MainUiState.unitMsAt(it)) },
            valueRange = MainUiState.FastestUnitMs.toFloat()..MainUiState.SlowestUnitMs.toFloat(),
            // Bez tego TalkBack czytałby procenty, które przy odwróconym suwaku nic nie mówią.
            modifier = Modifier.semantics {
                contentDescription = Strings.SpeedLabel
                stateDescription = valueText
            },
            // Kolory bierze z motywu (primary = MorsikGreenDark), więc nie trzeba ich tu podawać.
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            SectionLabel(Strings.SpeedSlow, Modifier.weight(1f))
            SectionLabel(Strings.SpeedFast)
        }
    }
}

/**
 * Czerwony przycisk Stop na dole ekranu.
 * Gdy nic nie jest nadawane, przycisk jest naprawdę wyłączony (enabled = false): nie da się go kliknąć,
 * a TalkBack mówi "wyłączony". Wcześniej tylko bladł, ale dalej był klikalny.
 */
@Composable
private fun StopButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MorsikRed,
            contentColor = Color.White,
            // Wygląd wyłączonego przycisku: różowe tło i ciemny tekst, czyli nadal czytelny.
            disabledContainerColor = MorsikPink,
            disabledContentColor = MorsikInkMuted,
        ),
    ) {
        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(Strings.Stop, style = MaterialTheme.typography.labelLarge)
    }
}
