package com.juren233.usagenotify.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.juren233.usagenotify.data.local.SettingsStore
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.notification.BalanceThresholds
import com.juren233.usagenotify.notification.LiveUpdateNotificationManager
import com.juren233.usagenotify.service.PollingStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val pollingStateHolder: PollingStateHolder,
    private val liveNotification: LiveUpdateNotificationManager,
) : ViewModel() {

    val isPolling = pollingStateHolder.state

    val refreshInterval = MutableStateFlow(settingsStore.refreshIntervalSeconds)
    val isRealtimeMode = MutableStateFlow(settingsStore.isRealtimeMode)
    val liveUpdateSufficientThresholdInput = MutableStateFlow(formatThresholdInput(settingsStore.liveUpdateSufficientThreshold))
    val liveUpdateDangerThresholdInput = MutableStateFlow(formatThresholdInput(settingsStore.liveUpdateDangerThreshold))
    val liveUpdateInsufficientThresholdInput = MutableStateFlow(formatThresholdInput(settingsStore.liveUpdateInsufficientThreshold))

    fun setRefreshInterval(seconds: Int) {
        settingsStore.refreshIntervalSeconds = seconds
        refreshInterval.value = seconds
    }

    fun setRealtimeMode(enabled: Boolean) {
        settingsStore.isRealtimeMode = enabled
        isRealtimeMode.value = enabled
    }

    fun setLiveUpdateSufficientThresholdInput(text: String) {
        setThresholdInput(text, liveUpdateSufficientThresholdInput) {
            settingsStore.liveUpdateSufficientThreshold = it
        }
    }

    fun setLiveUpdateDangerThresholdInput(text: String) {
        setThresholdInput(text, liveUpdateDangerThresholdInput) {
            settingsStore.liveUpdateDangerThreshold = it
        }
    }

    fun setLiveUpdateInsufficientThresholdInput(text: String) {
        setThresholdInput(text, liveUpdateInsufficientThresholdInput) {
            settingsStore.liveUpdateInsufficientThreshold = it
        }
    }

    fun liveUpdateThresholds(): BalanceThresholds {
        return BalanceThresholds(
            sufficient = settingsStore.liveUpdateSufficientThreshold,
            danger = settingsStore.liveUpdateDangerThreshold,
            insufficient = settingsStore.liveUpdateInsufficientThreshold,
        )
    }

    private fun setThresholdInput(
        text: String,
        input: MutableStateFlow<String>,
        save: (Double) -> Unit,
    ) {
        input.value = text
        val value = parseThresholdInput(text) ?: return
        save(value)
        refreshLiveUpdateNotification()
    }

    private fun refreshLiveUpdateNotification() {
        val state = pollingStateHolder.state.value
        if (!state.isPolling) return
        val monitoredId = settingsStore.monitoredSiteId
        val monitoredResult = state.balances[monitoredId]
        val balance = if (monitoredResult is BalanceResult.Success) monitoredResult.balance else 0.0
        val siteName = settingsStore.monitoredSiteName.ifEmpty { "监控中..." }
        liveNotification.update(balance, siteName, liveUpdateThresholds())
    }
}

internal fun formatThresholdInput(value: Double): String {
    if (!value.isFinite()) return ""
    return BigDecimal.valueOf(value)
        .stripTrailingZeros()
        .toPlainString()
}

internal fun parseThresholdInput(text: String): Double? {
    val value = text.trim().toDoubleOrNull() ?: return null
    return value.takeIf { it.isFinite() && it >= 0.0 }
}
