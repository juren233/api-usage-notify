package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType

interface SiteAdapter {
    val type: SiteType
    suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult
    suspend fun testConnection(baseUrl: String, credential: String): BalanceResult
}
