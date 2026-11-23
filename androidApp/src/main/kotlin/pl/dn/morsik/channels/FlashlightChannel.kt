package pl.dn.morsik.channels

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import pl.dn.morsik.transmit.SignalChannel

/**
 * Latarka (zadanie 8): włącza i wyłącza diodę błyskową tylnego aparatu przez CameraManager.
 *
 * `setTorchMode` nie wymaga uprawnienia do aparatu. Konstruktor jest prywatny:
 * obiekt tworzymy przez [createOrNull], które zwraca `null`, gdy telefon nie ma latarki.
 */
class FlashlightChannel private constructor(
    private val manager: CameraManager,
    private val cameraId: String,
) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            manager.setTorchMode(cameraId, on)
        } catch (e: Exception) {
            // Np. aparat zajęty przez inną aplikację. Pomijamy, żeby nie przerwać nadawania
            // na pozostałych kanałach.
        }
    }

    /** Na koniec latarka ma być zgaszona. */
    override fun release() = setOn(false)

    companion object {
        /** Zwraca kanał albo `null`, gdy telefon nie ma aparatu z lampą błyskową. */
        fun createOrNull(context: Context): FlashlightChannel? {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null
            return try {
                // Szukamy pierwszego aparatu, który ma lampę błyskową.
                val id = manager.cameraIdList.firstOrNull {
                    manager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                id?.let { FlashlightChannel(manager, it) }
            } catch (e: Exception) {
                null
            }
        }
    }
}
