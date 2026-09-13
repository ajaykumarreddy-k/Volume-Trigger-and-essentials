package com.voltrigger.tiles

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.voltrigger.core.startActivityAndCollapseCompat

@RequiresApi(Build.VERSION_CODES.N)
class RotationLockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapseCompat(intent)
            return
        }

        val isAutoRotateOn = isAutoRotateEnabled()
        try {
            Settings.System.putInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (isAutoRotateOn) 0 else 1
            )
            updateTileState(!isAutoRotateOn)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun isAutoRotateEnabled(): Boolean {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    private fun updateTileState(isEnabled: Boolean? = null) {
        val tile = qsTile ?: return
        val state = isEnabled ?: isAutoRotateEnabled()
        // State ACTIVE means Auto-Rotate is OFF (Rotation is locked)
        tile.state = if (state) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.updateTile()
    }
}
