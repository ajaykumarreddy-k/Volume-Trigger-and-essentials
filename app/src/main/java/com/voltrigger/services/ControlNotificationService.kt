package com.voltrigger.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Surface
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.voltrigger.R
import com.voltrigger.MainActivity
import com.voltrigger.core.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ControlNotificationService : Service() {

    private val CHANNEL_ID = "control_center_channel"
    private val NOTIFICATION_VOL_ID = 101
    private val NOTIFICATION_ORIENT_ID = 102
    private val NOTIFICATION_TILES_ID = 103

    private var isFlashlightOn = false
    private var cameraId: String? = null
    
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            super.onTorchModeChanged(id, enabled)
            if (id == cameraId) {
                isFlashlightOn = enabled
                updateNotificationTiles()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupCamera()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            handleAction(intent.action)
        }
        
        // Start foreground with the first notification
        startForeground(NOTIFICATION_VOL_ID, buildVolumeNotification())
        
        // Post the other two notifications
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ORIENT_ID, buildOrientationNotification())
        manager.notify(NOTIFICATION_TILES_ID, buildTilesNotification())
        
        return START_STICKY
    }

    private fun handleAction(action: String?) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        if (action?.startsWith("ACTION_VOL_SET_") == true) {
            val levelIndex = action.removePrefix("ACTION_VOL_SET_").toIntOrNull() ?: 0
            val targetVolume = ((levelIndex + 1) / 10f * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            updateNotificationVolume()
        }
        
        when (action) {
            "ACTION_VOL_UP" -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
                updateNotificationVolume()
            }
            "ACTION_VOL_DOWN" -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
                updateNotificationVolume()
            }
            "ACTION_PORTRAIT" -> setRotation(Surface.ROTATION_0)
            "ACTION_LAND_RIGHT" -> setRotation(Surface.ROTATION_90)
            "ACTION_LAND_LEFT" -> setRotation(Surface.ROTATION_270)
            "ACTION_UPSIDE_DOWN" -> setRotation(Surface.ROTATION_180)
            "ACTION_TOGGLE_FLASHLIGHT" -> toggleFlashlight()
            "ACTION_TOGGLE_DND" -> toggleDnd()
            "ACTION_TOGGLE_ROTATION_LOCK" -> toggleRotationLock()
            "ACTION_WELLBEING" -> {
                val settingsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(settingsIntent)
                // Collapse status bar
                sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }
            "ACTION_GITHUB" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val username = Prefs(applicationContext).githubUsernameFlow.first()
                    val gitIntent = if (username.isNotBlank()) {
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$username")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    } else {
                        Intent(applicationContext, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }
                    startActivity(gitIntent)
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
            }
        }
    }

    private fun setRotation(rotation: Int) {
        if (Settings.System.canWrite(this)) {
            try {
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                Settings.System.putInt(contentResolver, Settings.System.USER_ROTATION, rotation)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    private fun toggleRotationLock() {
        if (Settings.System.canWrite(this)) {
            try {
                val isAutoRotateOn = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (isAutoRotateOn) 0 else 1)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun toggleFlashlight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, !isFlashlightOn)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    private fun toggleDnd() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val currentFilter = notificationManager.currentInterruptionFilter
            val newFilter = if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            try {
                notificationManager.setInterruptionFilter(newFilter)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun setupCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { 
                cameraManager.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true 
            }
            cameraManager.registerTorchCallback(torchCallback, null)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.unregisterTorchCallback(torchCallback)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        val manager = getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_VOL_ID)
        manager.cancel(NOTIFICATION_ORIENT_ID)
        manager.cancel(NOTIFICATION_TILES_ID)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotificationVolume() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_VOL_ID, buildVolumeNotification())
    }
    
    private fun updateNotificationTiles() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_TILES_ID, buildTilesNotification())
    }

    private fun buildVolumeNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_volume)

        remoteViews.setOnClickPendingIntent(R.id.btn_vol_up, getPendingIntent("ACTION_VOL_UP"))
        remoteViews.setOnClickPendingIntent(R.id.btn_vol_down, getPendingIntent("ACTION_VOL_DOWN"))

        return createBaseNotification(remoteViews, "group_volume")
    }

    private fun buildOrientationNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_orientation)
        remoteViews.setOnClickPendingIntent(R.id.btn_orient_portrait, getPendingIntent("ACTION_PORTRAIT"))
        remoteViews.setOnClickPendingIntent(R.id.btn_orient_land_right, getPendingIntent("ACTION_LAND_RIGHT"))
        remoteViews.setOnClickPendingIntent(R.id.btn_orient_land_left, getPendingIntent("ACTION_LAND_LEFT"))
        remoteViews.setOnClickPendingIntent(R.id.btn_orient_upside, getPendingIntent("ACTION_UPSIDE_DOWN"))
        return createBaseNotification(remoteViews, "group_orientation")
    }

    private fun buildTilesNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_tiles)
        remoteViews.setOnClickPendingIntent(R.id.btn_tile_flashlight, getPendingIntent("ACTION_TOGGLE_FLASHLIGHT"))
        remoteViews.setOnClickPendingIntent(R.id.btn_tile_dnd, getPendingIntent("ACTION_TOGGLE_DND"))
        remoteViews.setOnClickPendingIntent(R.id.btn_tile_rotation_lock, getPendingIntent("ACTION_TOGGLE_ROTATION_LOCK"))
        remoteViews.setOnClickPendingIntent(R.id.btn_tile_wellbeing, getPendingIntent("ACTION_WELLBEING"))
        remoteViews.setOnClickPendingIntent(R.id.btn_tile_github, getPendingIntent("ACTION_GITHUB"))

        if (isFlashlightOn) {
            remoteViews.setInt(R.id.btn_tile_flashlight, "setBackgroundResource", R.drawable.bg_tile_circle_active)
        } else {
            remoteViews.setInt(R.id.btn_tile_flashlight, "setBackgroundResource", R.drawable.bg_tile_circle)
        }
        
        return createBaseNotification(remoteViews, "group_tiles")
    }

    private fun createBaseNotification(remoteViews: RemoteViews, groupId: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(true) // Fixes re-triggering
            .setGroup(groupId) // Unique group ID prevents bundling and collapsing
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, ControlNotificationService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Control Center",
                NotificationManager.IMPORTANCE_LOW // Low priority ensures it stays in silent
            ).apply {
                description = "Persistent notification for quick controls"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
