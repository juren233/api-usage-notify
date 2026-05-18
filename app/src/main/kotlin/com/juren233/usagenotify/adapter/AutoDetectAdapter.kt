package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType

class AutoDetectAdapter(
    private val candidates: List<SiteAdapter>,
) : SiteAdapter {

    override val type: SiteType = SiteType.AUTO

    override suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult {
        val errors = mutableListOf<String>()
        for (candidate in candidates) {
            when (val result = candidate.queryBalance(baseUrl, credential)) {
                is BalanceResult.Success -> return result.copy(detectedType = result.detectedType ?: candidate.type)
                is BalanceResult.Error -> errors += "${candidate.type.displayName}: ${result.message}"
            }
        }
        return BalanceResult.Error("无法识别站点协议：${errors.joinToString("; ")}")
    }

    override suspend fun testConnection(baseUrl: String, credential: String): BalanceResult {
        return queryBalance(baseUrl, credential)
    }
}
