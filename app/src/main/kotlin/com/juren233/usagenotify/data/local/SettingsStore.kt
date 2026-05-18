package com.juren233.usagenotify.data.local

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var refreshIntervalSeconds: Int
        get() = prefs.getInt("refresh_interval", 30)
        set(value) = prefs.edit().putInt("refresh_interval", value).apply()

    var isRealtimeMode: Boolean
        get() = prefs.getBoolean("realtime_mode", false)
        set(value) = prefs.edit().putBoolean("realtime_mode", value).apply()

    var liveUpdateSufficientThreshold: Double
        get() = if (prefs.contains("live_update_sufficient_threshold")) {
            prefs.getFloat("live_update_sufficient_threshold", 10.0f).toDouble()
        } else {
            prefs.getFloat("alert_threshold", 10.0f).toDouble()
        }
        set(value) = prefs.edit().putFloat("live_update_sufficient_threshold", value.toFloat()).apply()

    var liveUpdateDangerThreshold: Double
        get() = prefs.getFloat("live_update_danger_threshold", 2.5f).toDouble()
        set(value) = prefs.edit().putFloat("live_update_danger_threshold", value.toFloat()).apply()

    var liveUpdateInsufficientThreshold: Double
        get() = prefs.getFloat("live_update_insufficient_threshold", 1.0f).toDouble()
        set(value) = prefs.edit().putFloat("live_update_insufficient_threshold", value.toFloat()).apply()

    @Deprecated("Use liveUpdateSufficientThreshold")
    var globalAlertThreshold: Double
        get() = liveUpdateSufficientThreshold
        set(value) {
            liveUpdateSufficientThreshold = value
        }

    val effectiveIntervalSeconds: Int
        get() = if (isRealtimeMode) 10 else refreshIntervalSeconds
}
