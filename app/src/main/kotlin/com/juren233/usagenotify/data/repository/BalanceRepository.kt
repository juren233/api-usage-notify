package com.juren233.usagenotify.data.repository

import com.juren233.usagenotify.adapter.SiteAdapterFactory
import com.juren233.usagenotify.data.local.BalanceRecordDao
import com.juren233.usagenotify.data.local.CredentialStore
import com.juren233.usagenotify.data.local.SiteDao
import com.juren233.usagenotify.data.model.BalanceRecord
import com.juren233.usagenotify.data.model.BalanceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceRepository @Inject constructor(
    private val siteDao: SiteDao,
    private val balanceRecordDao: BalanceRecordDao,
    private val credentialStore: CredentialStore,
    private val adapterFactory: SiteAdapterFactory,
) {
    suspend fun refreshAll(): Map<Long, BalanceResult> = withContext(Dispatchers.IO) {
        val sites = siteDao.getEnabled()
        sites.map { site ->
            async {
                val credential = credentialStore.load(site.credentialRef)
                    ?: return@async site.id to BalanceResult.Error("No credential")
                val adapter = adapterFactory.get(site.type)
                val result = adapter.queryBalance(site.url, credential)
                if (result is BalanceResult.Success) {
                    balanceRecordDao.insert(
                        BalanceRecord(siteId = site.id, balance = result.balance)
                    )
                }
                site.id to result
            }
        }.awaitAll().toMap()
    }

    fun observeHistory(siteId: Long, days: Int): Flow<List<BalanceRecord>> {
        val since = System.currentTimeMillis() - days * 86_400_000L
        return balanceRecordDao.observeForSite(siteId, since)
    }

    suspend fun getLatestBalance(siteId: Long): BalanceRecord? =
        balanceRecordDao.getLatest(siteId)

    suspend fun pruneOldRecords() {
        balanceRecordDao.deleteOlderThan(System.currentTimeMillis() - 31 * 86_400_000L)
    }
}
