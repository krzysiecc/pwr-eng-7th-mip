package pl.dn.morsik.channels

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import pl.dn.morsik.transmit.SignalChannel

// Brak wibracji "bez końca": zlecamy bardzo długą i anulujemy.
class VibrationChannel private constructor(private val vibrator: Vibrator) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            if (on) vibrate() else vibrator.cancel()
        } catch (e: Exception) {
        }
    }

    override fun release() = setOn(false)

    private fun vibrate() {
        val effect = VibrationEffect.createOneShot(MAX_VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        // Jako alarm, bo zwykłą wibrację system może wyciszyć.
        if (Build.VERSION.SDK_INT >= 33) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
        }
    }

    companion object {
        private const val MAX_VIBRATION_MS = 60_000L

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
