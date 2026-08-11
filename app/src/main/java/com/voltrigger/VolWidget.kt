package com.voltrigger

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VolWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(ctx.packageName, R.layout.widget_volume)

            val piUp = PendingIntent.getBroadcast(
                ctx,
                0,
                Intent(ctx, VolReceiver::class.java).setAction(VolReceiver.ACTION_VOL_UP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val piDown = PendingIntent.getBroadcast(
                ctx,
                1,
                Intent(ctx, VolReceiver::class.java).setAction(VolReceiver.ACTION_VOL_DOWN),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.btn_up, piUp)
            views.setOnClickPendingIntent(R.id.btn_down, piDown)
            mgr.updateAppWidget(id, views)
        }
    }
}
