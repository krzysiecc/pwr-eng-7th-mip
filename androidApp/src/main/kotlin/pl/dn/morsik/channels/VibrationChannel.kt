package pl.dn.morsik.channels

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import pl.dn.morsik.transmit.SignalChannel

/**
 * Wibracja (zadanie 9). Android nie ma "włącz wibrację bez końca", więc przy włączeniu zlecamy
 * bardzo długą wibrację, a przy wyłączeniu ją anulujemy ([Vibrator.cancel]).
 * Wymaga uprawnienia VIBRATE w AndroidManifest.xml.
 */
class VibrationChannel private constructor(private val vibrator: Vibrator) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            if (on) vibrate() else vibrator.cancel()
        } catch (e: Exception) {
            // Nie przerywamy nadawania na innych kanałach.
        }
    }

    override fun release() = setOn(false)

    private fun vibrate() {
        val effect = VibrationEffect.createOneShot(MAX_VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        // Oznaczamy wibrację jako "alarm", bo zwykła wibracja bywa wyciszana przez system
        // (np. gdy w ustawieniach wyłączono wibracje dotyku).
        if (Build.VERSION.SDK_INT >= 33) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
        }
    }

    companion object {
        // Dłużej niż najdłuższa kreska (3 x 400 ms); i tak zawsze zatrzymujemy ją ręcznie.
        private const val MAX_VIBRATION_MS = 60_000L

        /** Zwraca kanał albo `null`, gdy telefon nie ma wibracji. */
        fun createOrNull(context: Context): VibrationChannel? {
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            return vibrator?.takeIf { it.hasVibrator() }?.let { VibrationChannel(it) }
        }
    }
}
