package com.voltrigger

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VolWidgetDown : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, VolReceiver::class.java).apply {
                action = VolReceiver.ACTION_VOL_DOWN
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(context.packageName, R.layout.widget_down)
            views.setOnClickPendingIntent(R.id.btn_vol_down, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
