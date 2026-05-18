package com.juren233.usagenotify.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.juren233.usagenotify.R
import com.juren233.usagenotify.data.model.Site

class AlertNotificationManager(private val context: Context) {

    private val alerted = mutableSetOf<Long>()

    fun sendAlertIfNeeded(site: Site, balance: Double) {
        if (balance >= site.alertThreshold) {
            alerted.remove(site.id)
            return
        }
        if (site.id in alerted) return
        alerted.add(site.id)

        val notification = NotificationCompat.Builder(context, NotificationChannels.ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("余额不足: ${site.name}")
            .setContentText(
                "当前余额 $${String.format("%.2f", balance)}，低于阈值 $${
                    String.format("%.2f", site.alertThreshold)
                }"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify((2000 + site.id).toInt(), notification)
        } catch (_: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
