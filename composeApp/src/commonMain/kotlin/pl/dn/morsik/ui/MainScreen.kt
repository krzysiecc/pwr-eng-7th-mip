package pl.dn.morsik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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
    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header(onMenu = onMenu)
            MessageField(
                message = state.message,
                onMessageChange = onMessageChange,
                onDictate = onDictate,
            )
            LetterPreview(letters = state.letters, activeLetter = state.transmission?.activeLetter)
            StatusLine(state = state)
            SignalIndicator(state = state)
            ChannelToggles(channels = state.channels, onChange = onChannelsChange)
            SpeedControl(unitMs = state.unitMs, onUnitMsChange = onUnitMsChange)
        }
        StopButton(
            enabled = state.transmission != null,
            onClick = onStop,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun Header(onMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(Strings.AppName, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = Strings.AppNameMorse,
            modifier = Modifier.clip(CircleShape).background(MorsikRed).padding(horizontal = 10.dp, vertical = 3.dp),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MorsikPink)
                .clickable(onClickLabel = Strings.Menu, role = Role.Button, onClick = onMenu),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Menu, contentDescription = Strings.Menu)
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MorsikInk.copy(alpha = 0.6f),
    )
}

@Composable
private fun MessageField(message: String, onMessageChange: (String) -> Unit, onDictate: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(Strings.MessageLabel)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(Strings.MessagePlaceholder) },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MorsikPink,
                    unfocusedContainerColor = MorsikPink,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MorsikInk,
                ),
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MorsikInk)
                    .clickable(onClickLabel = Strings.Dictate, role = Role.Button, onClick = onDictate),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = Strings.Dictate, tint = Color.White)
            }
        }
    }
}

private fun String.toDisplayCode() = map { if (it == '.') "·" else "–" }.joinToString(" ")

@Composable
private fun LetterPreview(letters: List<LetterUi>, activeLetter: Int?) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(letters) { index, letter ->
            val active = index == activeLetter
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (active) MorsikGreen else MorsikPink)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(letter.symbol, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = letter.code.toDisplayCode(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (active) MorsikInk else MorsikInk.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun StatusLine(state: MainUiState) {
    val letter = state.activeLetter
    val transmission = state.transmission
    val text = if (letter != null && transmission != null) {
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
            Box(Modifier.size(7.dp).clip(CircleShape).background(MorsikGreen))
            Spacer(Modifier.width(6.dp))
        }
        SectionLabel(text)
    }
}

@Composable
private fun SignalIndicator(state: MainUiState) {
    val signal = state.activeSignal
    val signalOn = state.transmission?.signalOn == true
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MorsikPink)
                .border(3.dp, MorsikGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val mark = Modifier.alpha(if (signalOn) 1f else 0.25f).background(MorsikGreen)
            when (signal) {
                Signal.Dot -> Box(Modifier.size(48.dp).clip(CircleShape).then(mark))
                Signal.Dash -> Box(Modifier.size(width = 120.dp, height = 36.dp).clip(CircleShape).then(mark))
                null -> Unit
            }
        }
        Text(
            text = when (signal) {
                Signal.Dot -> Strings.SignalDot
                Signal.Dash -> Strings.SignalDash
                null -> Strings.SignalIdle
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChannelToggles(channels: ChannelsUi, onChange: (ChannelsUi) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            icon = Icons.Filled.VolumeUp,
            selected = channels.sound,
            onChange = { onChange(channels.copy(sound = it)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChannelToggle(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = if (selected) MorsikInk else MorsikInk.copy(alpha = 0.5f)
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) MorsikGreen else MorsikPink)
            .toggleable(value = selected, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
private fun SpeedControl(unitMs: Int, onUnitMsChange: (Int) -> Unit) {
    val range = MainUiState.SlowestUnitMs - MainUiState.FastestUnitMs
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(Strings.SpeedLabel, Modifier.weight(1f))
            Text(Strings.speedValue(unitMs), style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = (MainUiState.SlowestUnitMs - unitMs).toFloat() / range,
            onValueChange = { onUnitMsChange(MainUiState.SlowestUnitMs - (it * range).roundToInt()) },
            colors = SliderDefaults.colors(
                thumbColor = MorsikGreen,
                activeTrackColor = MorsikGreen,
                inactiveTrackColor = MorsikInk.copy(alpha = 0.15f),
            ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            SectionLabel(Strings.SpeedSlow, Modifier.weight(1f))
            SectionLabel(Strings.SpeedFast)
        }
    }
}

@Composable
private fun StopButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) MorsikRed else MorsikRed.copy(alpha = 0.35f),
            contentColor = Color.White,
        ),
    ) {
        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(Strings.Stop, style = MaterialTheme.typography.labelLarge)
    }
}
