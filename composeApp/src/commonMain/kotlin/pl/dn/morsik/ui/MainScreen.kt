package pl.dn.morsik.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
//   które woła, gdy użytkownik coś zrobi. Stan zmienia MorsikViewModel. To się nazywa "state hoisting"
//   i pozwala pokazać ekran w podglądzie (MainScreenPreview.kt) z dowolnymi danymi.
// * Modifier to łańcuch ustawień elementu: rozmiar, odstępy, tło, kliknięcia...
//   KOLEJNOŚĆ MA ZNACZENIE: `.padding(8.dp).background(...)` daje odstęp NA ZEWNĄTRZ tła,
//   a `.background(...).padding(8.dp)` odstęp WEWNĄTRZ tła.
// * Column = elementy jeden pod drugim, Row = obok siebie, Box = jeden na drugim.
// =============================================================================================

/**
 * Cały ekran główny: przewijana część z treścią i przyciski Nadaj i Stop przyklejone na dole.
 *
 * @param state wszystko, co ma być widać na ekranie
 * @param onMessageChange wołane przy każdej zmianie tekstu w polu wiadomości
 * @param onDictate kliknięcie mikrofonu
 * @param onStart kliknięcie "Nadaj" albo "Wyślij" na klawiaturze
 * @param onOpen wybór pozycji z menu (Pomoc, deklaracja dostępności, O aplikacji)
 * @param onChannelsChange włączenie/wyłączenie kanału (latarka, wibracja, dźwięk)
 * @param onUnitMsChange przesunięcie suwaka szybkości (nowa wartość w ms)
 * @param onStop kliknięcie Stop
 */
@OptIn(ExperimentalComposeUiApi::class) // BackHandler jest jeszcze oznaczony jako eksperymentalny
@Composable
fun MainScreen(
    state: MainUiState,
    onMessageChange: (String) -> Unit,
    onDictate: () -> Unit,
    onStart: () -> Unit,
    onOpen: (Screen) -> Unit,
    onChannelsChange: (ChannelsUi) -> Unit,
    onUnitMsChange: (Int) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Czy menu jest otwarte - to czysto "wizualny" stan, więc trzyma go sam ekran, a nie ViewModel.
    // rememberSaveable (zamiast remember) = przetrwa też obrót ekranu.
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    // Rozmycie ekranu pod menu rośnie płynnie. Modifier.blur działa od Androida 12; na starszych
    // zostaje tylko półprzezroczysta zasłona z [MenuOverlay].
    val blur by animateDpAsState(if (menuOpen) 16.dp else 0.dp, label = "menuBlur")
    val focusManager = LocalFocusManager.current

    // Systemowy przycisk/gest Wstecz najpierw zamyka menu (wygrywa z BackHandlerem w App, bo jest "głębiej").
    BackHandler(enabled = menuOpen) { menuOpen = false }

    // Box = elementy jeden na drugim: ekran, a nad nim (gdy menu otwarte) zasłona i menu.
    // safeDrawingPadding = nie wchodzimy pod pasek stanu, wycięcie aparatu ani klawiaturę.
    // paneTitle: po powrocie z Pomocy TalkBack ogłosi, że to znowu ekran główny.
    Box(modifier = modifier.fillMaxSize().safeDrawingPadding().semantics { paneTitle = Strings.AppName }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blur)
                // Gdy menu jest otwarte, czytnik ekranu nie ma wchodzić w rozmyty ekran pod spodem.
                .then(if (menuOpen) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f) // zajmij całe wolne miejsce nad przyciskami
                    .verticalScroll(rememberScrollState()) // na małym ekranie da się przewinąć
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), // 12 dp odstępu między sekcjami
            ) {
                Header(onMenu = {
                    focusManager.clearFocus() // chowa klawiaturę, żeby nie wisiała nad menu
                    menuOpen = true
                })
                MessageField(
                    message = state.message,
                    onMessageChange = onMessageChange,
                    onDictate = onDictate,
                    onStart = onStart,
                    readOnly = state.isTransmitting, // w trakcie nadawania nie zmieniamy tekstu
                )
                CodePreview(letters = state.letters, activeLetter = state.transmission?.activeLetter)
                StatusLine(state = state)
                SignalIndicator(signal = state.activeSignal, signalOn = state.transmission?.signalOn == true)
                ChannelToggles(channels = state.channels, onChange = onChannelsChange)
                SpeedControl(unitMs = state.unitMs, onUnitMsChange = onUnitMsChange)
            }
            // Nadaj i Stop w jednej linii (po równo), dzięki czemu cały ekran mieści się bez przewijania.
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StartButton(enabled = state.canStart, onClick = onStart, modifier = Modifier.weight(1f))
                StopButton(enabled = state.isTransmitting, onClick = onStop, modifier = Modifier.weight(1f))
            }
        }
        MenuOverlay(
            open = menuOpen,
            onClose = { menuOpen = false },
            onOpen = { screen ->
                menuOpen = false
                onOpen(screen)
            },
        )
    }
}

/** Górny pasek: nazwa aplikacji dużymi literami i przycisk menu przy prawej krawędzi. */
@Composable
private fun Header(onMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = Strings.AppName.uppercase(),
            // weight(1f) = nazwa zajmuje całe miejsce oprócz przycisku menu, który zawsze jest po prawej.
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            letterSpacing = 3.sp, // duże litery lepiej wyglądają z lekkim rozstrzeleniem
        )
        // FilledIconButton sam pilnuje minimalnego pola dotyku 48 dp i efektu kliknięcia.
        FilledIconButton(
            onClick = onMenu,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MorsikGrey, contentColor = MorsikInk),
        ) {
            // contentDescription = co powie TalkBack o tym przycisku.
            Icon(Icons.Filled.Menu, contentDescription = Strings.Menu)
        }
    }
}

/**
 * Menu: zasłona (rozmyty ekran jest już pod spodem) i biała karta na środku z marginesem, która
 * wjeżdża z prawej strony. Dotknięcie zasłony albo strzałki Wstecz zamyka menu.
 */
@Composable
private fun MenuOverlay(open: Boolean, onClose: () -> Unit, onOpen: (Screen) -> Unit) {
    // Zasłona: lekko wybiela rozmyty ekran (na Androidzie < 12, bez blur, jest jedynym wygaszeniem).
    AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.55f))
                // Dotknięcie obok karty zamyka menu. Dla TalkBacka jest do tego strzałka, więc zasłona
                // nie ma własnych semantyk.
                .pointerInput(Unit) { detectTapGestures { onClose() } }
                .clearAndSetSemantics {},
        )
    }
    AnimatedVisibility(
        visible = open,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
    ) {
        val shape = MaterialTheme.shapes.large
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp) // marginesy: karta nie dotyka krawędzi ekranu
                .shadow(16.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(2.dp, MorsikGrey, shape)
                // Puste miejsce na karcie połyka dotknięcia, żeby nie "przeciekały" do zasłony i nie zamykały menu.
                .pointerInput(Unit) { detectTapGestures { } }
                .semantics { paneTitle = Strings.Menu }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Ten sam przycisk Wstecz co na ekranach Pomocy i deklaracji (spójność, WCAG 3.2.4).
                FilledIconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MorsikGrey, contentColor = MorsikInk),
                ) {
                    // AutoMirrored = strzałka sama się odwraca w językach pisanych od prawej do lewej.
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.Back)
                }
                Text(
                    text = Strings.Menu,
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MorsikInk,
                )
            }
            MenuItem(Strings.Help, Icons.AutoMirrored.Filled.HelpOutline) { onOpen(Screen.Help) }
            MenuItem(Strings.Accessibility, Icons.Filled.Accessibility) { onOpen(Screen.Accessibility) }
            MenuItem(Strings.About, Icons.Filled.Info) { onOpen(Screen.About) }
        }
    }
}

/** Jedna pozycja menu: duży szary pasek z ikoną i podpisem. Ikona to ozdoba, TalkBack czyta podpis. */
@Composable
private fun MenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MorsikGrey)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MorsikInk)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MorsikInk)
    }
}

/**
 * Mały, przygaszony nagłówek sekcji, np. "WIADOMOŚĆ".
 * [heading] = TalkBack ogłasza go jako nagłówek i pozwala do niego przeskoczyć.
 * Podpisy "Wolno" / "Szybko" pod suwakiem wyglądają tak samo, ale nagłówkami nie są.
 */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier, heading: Boolean = true) {
    Text(
        text = text,
        modifier = modifier.semantics { if (heading) heading() },
        style = MaterialTheme.typography.labelSmall,
        color = MorsikInkMuted,
    )
}

/** Pole do wpisania wiadomości i przycisk dyktowania obok. */
@Composable
private fun MessageField(
    message: String,
    onMessageChange: (String) -> Unit,
    onDictate: () -> Unit,
    onStart: () -> Unit,
    readOnly: Boolean,
) {
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
                readOnly = readOnly,
                // Przycisk akcji na klawiaturze zmienia się na "Wyślij" i od razu rozpoczyna nadawanie.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onStart() }),
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MorsikGrey,
                    unfocusedContainerColor = MorsikGrey,
                    focusedTextColor = MorsikInk,
                    unfocusedTextColor = MorsikInk,
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
 * Zwijana belka "KOD MORSE'A": domyślnie zwinięta, po dotknięciu pokazuje [LetterPreview].
 * Dzięki temu kafelki z kodem nie zaśmiecają ekranu, a ciekawy użytkownik nadal je ma.
 */
@Composable
private fun CodePreview(letters: List<LetterUi>, activeLetter: Int?) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp) // pole dotyku co najmniej 48 dp
                .clip(MaterialTheme.shapes.medium)
                .clickable(
                    onClickLabel = if (expanded) Strings.Collapse else Strings.Expand,
                    role = Role.Button,
                    onClick = { expanded = !expanded },
                )
                .semantics { stateDescription = if (expanded) Strings.Expanded else Strings.Collapsed },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(Strings.Preview, Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null, // stan mówi stateDescription
                tint = MorsikInkMuted,
            )
        }
        AnimatedVisibility(visible = expanded) {
            LetterPreview(letters = letters, activeLetter = activeLetter)
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
                    .background(if (active) MorsikGreen else MorsikGrey, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    // Dla TalkBack cały kafelek to jeden element z opisem "O: kreska kreska kreska".
                    .clearAndSetSemantics { contentDescription = spokenLetter(letter.symbol, letter.code) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val textColor = if (active) Color.White else MorsikInk // biały na zielonym
                Text(letter.symbol, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(
                    text = displayCode(letter.code),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = textColor,
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
            Box(Modifier.size(8.dp).background(MorsikGreen, CircleShape))
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(MorsikGrey, CircleShape)
                .border(3.dp, MorsikGreen, CircleShape),
            contentAlignment = Alignment.Center, // zawartość na środku koła
        ) {
            // Wspólny wygląd znaku: zielony, pełny gdy sygnał trwa, blady w przerwie.
            val mark = Modifier.alpha(if (signalOn) 1f else 0.25f)
            // `when` = rozbudowany switch. Musi obsłużyć wszystkie przypadki, także null.
            when (signal) {
                Signal.Dot -> Box(mark.size(40.dp).background(MorsikGreen, CircleShape))
                Signal.Dash -> Box(mark.size(width = 96.dp, height = 30.dp).background(MorsikGreen, CircleShape))
                null -> Unit // nic nie rysujemy
            }
        }
        Text(
            text = when (signal) {
                Signal.Dot -> Strings.SignalDot
                Signal.Dash -> Strings.SignalDash
                null -> Strings.SignalIdle
            },
            style = MaterialTheme.typography.titleMedium,
            color = MorsikInk,
            textAlign = TextAlign.Center,
        )
    }
}

/** Trzy przełączniki kanałów obok siebie, każdy tej samej szerokości i wysokości. */
@Composable
private fun ChannelToggles(channels: ChannelsUi, onChange: (ChannelsUi) -> Unit) {
    // Szeroki odstęp (20 dp): w nim siedzą ptaszki włączonych kanałów. IntrinsicSize.Max = wiersz
    // ma wysokość najwyższego przycisku, a fillMaxHeight() w ChannelToggle rozciąga pozostałe.
    Row(modifier = Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
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
 * Jeden przełącznik kanału: ikona i podpis. Włączony = zielony z białym tekstem, wyłączony = szary.
 * Ptaszek włączonego kanału siedzi na prawym górnym rogu, głównie w odstępie między przyciskami,
 * więc nie przesuwa podpisu. Jest po to, żeby stan był widoczny nie tylko kolorem (WCAG 1.4.1).
 */
@Composable
private fun ChannelToggle(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) Color.White else MorsikInkMuted
    // Box zewnętrzny NIE przycina zawartości, dzięki czemu ptaszek może wystawać poza przycisk.
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .heightIn(min = 64.dp) // wszystkie trzy tej samej wysokości, także gdy jeden podpis się zawinie
                .clip(MaterialTheme.shapes.medium) // przycina też efekt kliknięcia (ripple) do zaokrąglonych rogów
                .background(if (selected) MorsikGreen else MorsikGrey)
                // toggleable = kliknięcie przełącza wartość; Role.Switch mówi TalkBackowi "przełącznik, włączony".
                .toggleable(value = selected, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            // Ikona jest ozdobą (contentDescription = null), bo podpis pod nią mówi to samo.
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, textAlign = TextAlign.Center)
        }
        if (selected) {
            // Ozdoba: stan czyta TalkBack z przełącznika, więc znaczek nie ma własnych semantyk.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 9.dp, y = (-9).dp) // środek znaczka w rogu przycisku, głównie w odstępie
                    .size(22.dp)
                    .background(MorsikInk, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

/**
 * Suwak długości jednostki: od 350 ms (wolno, lewo) do 50 ms (bardzo szybko, prawo).
 * Prawe 40% (szybciej niż 170 ms, czyli ponad 3 błyski na sekundę) to szara strefa: kolor wyszarzały,
 * uchwyt lekko pulsuje, a obok pojawia się ostrzeżenie tekstem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedControl(unitMs: Int, onUnitMsChange: (Int) -> Unit) {
    val beyondSafe = MainUiState.isBeyondSafeSpeed(unitMs)
    val valueText = if (beyondSafe) Strings.speedValueWithWarning(unitMs) else Strings.speedValue(unitMs)
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
            thumb = { SpeedThumb(beyondSafe) },
            track = { SpeedTrack(it) },
        )
        // Podpisy pod suwakiem: "Wolno" na lewym końcu, "Szybko" dokładnie pod granicą szarej strefy,
        // "Bardzo szybko" na prawym końcu. 10 dp z boku = połowa uchwytu, o tyle krótszy jest tor.
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
            SectionLabel(Strings.SpeedSlow, Modifier.align(Alignment.CenterStart), heading = false)
            SectionLabel(
                Strings.SpeedFast,
                Modifier.align(BiasAlignment(horizontalBias = 2 * MainUiState.SafeZoneEnd - 1, verticalBias = 0f)),
                heading = false,
            )
            SectionLabel(Strings.SpeedVeryFast, Modifier.align(Alignment.CenterEnd), heading = false)
        }
    }
}

/** Uchwyt suwaka: zielony; w szarej strefie wyszarzały i powoli mruga (przezroczystość 1 -> 0,85). */
@Composable
private fun SpeedThumb(beyondSafe: Boolean) {
    val alpha = if (beyondSafe) {
        // Animację uruchamiamy tylko w szarej strefie, poza nią nic się nie liczy.
        rememberInfiniteTransition(label = "speedWarning").animateFloat(
            initialValue = 1f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Reverse),
            label = "speedThumbAlpha",
        ).value
    } else {
        1f
    }
    SliderDefaults.Thumb(
        interactionSource = remember { MutableInteractionSource() },
        modifier = Modifier.graphicsLayer { this.alpha = alpha },
        colors = SliderDefaults.colors(thumbColor = if (beyondSafe) MorsikGreenMuted else MorsikGreen),
    )
}

/**
 * Tor suwaka rysowany ręcznie, bo ma dwa kolory: zielony do uchwytu, ale za granicą szarej strefy
 * wyszarzały. Reszta toru (za uchwytem) jest szara, a jej strefa szybka ma zielonkawy odcień.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedTrack(state: SliderState) {
    val range = state.valueRange
    val fraction = ((state.value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
        val corner = CornerRadius(size.height / 2)
        val zoneStart = size.width * MainUiState.SafeZoneEnd
        val thumbX = size.width * fraction
        drawRoundRect(MorsikGrey, cornerRadius = corner)
        clipRect(left = zoneStart) { drawRoundRect(MorsikGreenMuted.copy(alpha = 0.35f), cornerRadius = corner) }
        clipRect(right = thumbX) { drawRoundRect(MorsikGreen, cornerRadius = corner) }
        if (thumbX > zoneStart) {
            clipRect(left = zoneStart, right = thumbX) { drawRoundRect(MorsikGreenMuted, cornerRadius = corner) }
        }
    }
}

/** Zielony przycisk "Nadaj" obok przycisku Stop. Aktywny, gdy jest co nadać i nic się nie nadaje. */
@Composable
private fun StartButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MorsikGreen,
            contentColor = Color.White, // biały na zielonym
            disabledContainerColor = MorsikGrey,
            disabledContentColor = MorsikInkMuted,
        ),
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(Strings.Start, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Przycisk Stop na dole ekranu: czerwony tylko, gdy coś się nadaje, w przeciwnym razie szary jak Nadaj.
 * Gdy nic nie jest nadawane, przycisk jest naprawdę wyłączony (enabled = false): nie da się go kliknąć,
 * a TalkBack mówi "wyłączony". Wcześniej tylko bladł, ale dalej był klikalny.
 */
@Composable
internal fun StopButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MorsikRed,
            contentColor = Color.White,
            // Wygląd wyłączonego przycisku: szare tło i ciemny tekst, czyli nadal czytelny.
            disabledContainerColor = MorsikGrey,
            disabledContentColor = MorsikInkMuted,
        ),
    ) {
        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(Strings.Stop, style = MaterialTheme.typography.labelLarge)
    }
}
