package pl.dn.morsik.channels

import android.media.AudioManager
import android.media.ToneGenerator
import pl.dn.morsik.transmit.SignalChannel

/**
 * Dźwięk (zadanie 9): ciągły ton z ToneGenerator, grany dopóki go nie zatrzymamy.
 * Nie wymaga żadnych uprawnień. Głośność zależy od głośności multimediów w telefonie.
 */
class SoundChannel private constructor(private val generator: ToneGenerator) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            // startTone(typ) bez czasu gra do wywołania stopTone().
            if (on) generator.startTone(ToneGenerator.TONE_DTMF_1) else generator.stopTone()
        } catch (e: Exception) {
            // Nie przerywamy nadawania na innych kanałach.
        }
    }

    /** ToneGenerator trzyma zasoby systemowe, więc trzeba go zwolnić. */
    override fun release() {
        try {
            generator.stopTone()
            generator.release()
        } catch (e: Exception) {
        }
    }

    companion object {
        /** Zwraca kanał albo `null`, gdy system nie pozwolił utworzyć generatora. */
        fun createOrNull(): SoundChannel? = try {
            SoundChannel(ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME_PERCENT))
        } catch (e: Exception) {
            null
        }

        private const val VOLUME_PERCENT = 100 // 0-100, względem głośności strumienia
    }
}
