package com.juren233.usagenotify.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val LIVE_UPDATE_CHANNEL_ID = "balance_live_update"
    const val ALERT_CHANNEL_ID = "balance_alert"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val liveChannel = NotificationChannel(
            LIVE_UPDATE_CHANNEL_ID,
            "余额实时监控",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "显示所有站点的总余额"
        }

        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "余额不足提醒",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "当余额低于阈值时提醒"
            enableVibration(true)
        }

        manager.createNotificationChannels(listOf(liveChannel, alertChannel))
    }
}
