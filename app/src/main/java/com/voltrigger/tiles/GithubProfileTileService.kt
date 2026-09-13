package com.voltrigger.tiles

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.voltrigger.MainActivity
import com.voltrigger.core.Prefs
import com.voltrigger.core.startActivityAndCollapseCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.N)
class GithubProfileTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val prefs = Prefs(this)
        val username = runBlocking { prefs.githubUsernameFlow.first() }

        if (username.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$username")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapseCompat(intent)
        } else {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapseCompat(intent)
        }
    }
}
