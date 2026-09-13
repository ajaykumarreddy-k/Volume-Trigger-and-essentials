package com.voltrigger.tiles

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class FlashlightTileService : TileService() {

    private var isFlashlightOn = false
    private var cameraId: String? = null
    
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            super.onTorchModeChanged(id, enabled)
            if (id == cameraId) {
                isFlashlightOn = enabled
                updateTileState()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (cameraId == null) {
                cameraId = cameraManager.cameraIdList.firstOrNull { cameraManager.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
            }
            cameraManager.registerTorchCallback(torchCallback, null)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    override fun onClick() {
        super.onClick()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, !isFlashlightOn)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = if (isFlashlightOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
