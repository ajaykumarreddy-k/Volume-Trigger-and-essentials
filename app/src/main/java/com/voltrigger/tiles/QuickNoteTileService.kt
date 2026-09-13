package com.voltrigger.tiles

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.voltrigger.core.startActivityAndCollapseCompat
import com.voltrigger.ui.QuickNoteActivity

@RequiresApi(Build.VERSION_CODES.N)
class QuickNoteTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, QuickNoteActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapseCompat(intent)
    }
}
