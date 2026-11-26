package pl.dn.morsik.channels

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import pl.dn.morsik.transmit.SignalChannel

// `setTorchMode` nie wymaga uprawnienia do aparatu.
class FlashlightChannel private constructor(
    private val manager: CameraManager,
    private val cameraId: String,
) : SignalChannel {

    override fun setOn(on: Boolean) {
        try {
            manager.setTorchMode(cameraId, on)
        } catch (e: Exception) {
        }
    }

    override fun release() = setOn(false)

    companion object {
        fun createOrNull(context: Context): FlashlightChannel? {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null
            return try {
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
