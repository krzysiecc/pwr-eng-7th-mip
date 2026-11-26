package pl.dn.morsik.channels

import android.media.AudioManager
import android.media.ToneGenerator
import pl.dn.morsik.transmit.SignalChannel

class SoundChannel private constructor(private val generator: ToneGenerator) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            if (on) generator.startTone(ToneGenerator.TONE_DTMF_1) else generator.stopTone()
        } catch (e: Exception) {
        }
    }

    override fun release() {
        try {
            generator.stopTone()
            generator.release()
        } catch (e: Exception) {
        }
    }

    companion object {
        fun createOrNull(): SoundChannel? = try {
            SoundChannel(ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME_PERCENT))
        } catch (e: Exception) {
            null
        }

        private const val VOLUME_PERCENT = 100
    }
}
