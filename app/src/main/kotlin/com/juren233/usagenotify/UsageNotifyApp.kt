package com.juren233.usagenotify

import android.app.Application
import com.juren233.usagenotify.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UsageNotifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
    }
}
