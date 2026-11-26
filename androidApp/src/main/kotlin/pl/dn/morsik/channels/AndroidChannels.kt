package pl.dn.morsik.channels

import android.content.Context
import pl.dn.morsik.transmit.SignalChannel
import pl.dn.morsik.ui.ChannelsUi

// Kanały nieobsługiwane przez telefon są pomijane.
object AndroidChannels {
    fun create(context: Context, selected: ChannelsUi): List<SignalChannel> = listOfNotNull(
        if (selected.flashlight) FlashlightChannel.createOrNull(context) else null,
        if (selected.vibration) VibrationChannel.createOrNull(context) else null,
        if (selected.sound) SoundChannel.createOrNull() else null,
    )
}
