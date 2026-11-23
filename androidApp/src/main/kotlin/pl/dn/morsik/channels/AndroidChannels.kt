package pl.dn.morsik.channels

import android.content.Context
import pl.dn.morsik.transmit.SignalChannel
import pl.dn.morsik.ui.ChannelsUi

/**
 * Z ustawień na ekranie ("latarka: tak, wibracja: tak, dźwięk: nie") robi listę kanałów do nadawania.
 * Kanał, którego telefon nie obsługuje (np. brak latarki), jest po prostu pomijany,
 * więc aplikacja nie wywala się, tylko nadaje na tym, co jest.
 */
object AndroidChannels {
    fun create(context: Context, selected: ChannelsUi): List<SignalChannel> = listOfNotNull(
        if (selected.flashlight) FlashlightChannel.createOrNull(context) else null,
        if (selected.vibration) VibrationChannel.createOrNull(context) else null,
        if (selected.sound) SoundChannel.createOrNull() else null,
    )
}
