package com.voltrigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class VolReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val direction = when (intent.action) {
            ACTION_VOL_UP -> AudioManager.ADJUST_RAISE
            ACTION_VOL_DOWN -> AudioManager.ADJUST_LOWER
            else -> return
        }
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    companion object {
        const val ACTION_VOL_UP = "com.voltrigger.ACTION_VOL_UP"
        const val ACTION_VOL_DOWN = "com.voltrigger.ACTION_VOL_DOWN"
    }
}
