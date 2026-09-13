package com.voltrigger.tiles

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.voltrigger.core.startActivityAndCollapseCompat

@RequiresApi(Build.VERSION_CODES.N)
class DigitalWellbeingTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent().apply {
            action = Settings.ACTION_APP_USAGE_SETTINGS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            startActivityAndCollapseCompat(intent)
        } catch (e: Exception) {
            // Fallback if specific intent is not available
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapseCompat(fallbackIntent)
        }
    }
}
