package com.voltrigger.tiles

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.voltrigger.core.startActivityAndCollapseCompat

@RequiresApi(Build.VERSION_CODES.N)
class DndToggleTileService : TileService() {
    
    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapseCompat(intent)
            return
        }

        val currentFilter = notificationManager.currentInterruptionFilter
        val newFilter = if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        } else {
            NotificationManager.INTERRUPTION_FILTER_ALL
        }
        
        try {
            notificationManager.setInterruptionFilter(newFilter)
            updateTileState(newFilter)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun updateTileState(filter: Int? = null) {
        val tile = qsTile ?: return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentFilter = filter ?: notificationManager.currentInterruptionFilter
        
        tile.state = if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }
        tile.updateTile()
    }
}
