package com.juren233.usagenotify.service

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.juren233.usagenotify.data.local.SettingsStore
import com.juren233.usagenotify.data.local.SiteDao
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.repository.BalanceRepository
import com.juren233.usagenotify.notification.BalanceThresholds
import com.juren233.usagenotify.notification.AlertNotificationManager
import com.juren233.usagenotify.notification.LiveUpdateNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BalancePollingService : LifecycleService() {

    @Inject lateinit var balanceRepository: BalanceRepository
    @Inject lateinit var siteDao: SiteDao
    @Inject lateinit var settingsStore: SettingsStore
    @Inject lateinit var pollingStateHolder: PollingStateHolder

    private lateinit var liveNotification: LiveUpdateNotificationManager
    private lateinit var alertNotification: AlertNotificationManager
    private var pollingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        liveNotification = LiveUpdateNotificationManager(this)
        alertNotification = AlertNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val liveUpdateThresholds = currentLiveUpdateThresholds()
        val notification = liveNotification.buildNotification(0.0, 0, liveUpdateThresholds)
        ServiceCompat.startForeground(
            this,
            LiveUpdateNotificationManager.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )

        pollingStateHolder.update { it.copy(isPolling = true) }

        if (pollingJob == null || pollingJob?.isActive != true) {
            pollingJob = lifecycleScope.launch {
                while (isActive) {
                    runPollingCycle()
                    delay(settingsStore.effectiveIntervalSeconds * 1000L)
                }
            }
        }

        return START_STICKY
    }

    private suspend fun runPollingCycle() {
        val results = balanceRepository.refreshAll()
        val sites = siteDao.getEnabled()
        val liveUpdateThresholds = currentLiveUpdateThresholds()

        var totalBalance = 0.0
        var siteCount = 0

        for (site in sites) {
            val result = results[site.id]
            if (result is BalanceResult.Success) {
                totalBalance += result.balance
                siteCount++
                alertNotification.sendAlertIfNeeded(site, result.balance)
            }
        }

        pollingStateHolder.update {
            it.copy(
                balances = results,
                totalBalance = totalBalance,
                lastUpdated = System.currentTimeMillis(),
            )
        }

        liveNotification.update(totalBalance, siteCount, liveUpdateThresholds)
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        pollingStateHolder.update { it.copy(isPolling = false) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun currentLiveUpdateThresholds(): BalanceThresholds {
        return BalanceThresholds(
            sufficient = settingsStore.liveUpdateSufficientThreshold,
            danger = settingsStore.liveUpdateDangerThreshold,
            insufficient = settingsStore.liveUpdateInsufficientThreshold,
        )
    }
}
