package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import okhttp3.OkHttpClient

class OneHubAdapter(
    client: OkHttpClient,
) : SiteAdapter {

    override val type: SiteType = SiteType.ONE_HUB

    private val delegate = OneApiAdapter(client, SiteType.ONE_HUB)

    override suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult {
        return delegate.queryBalance(baseUrl, credential)
    }

    override suspend fun testConnection(baseUrl: String, credential: String): BalanceResult {
        return delegate.testConnection(baseUrl, credential)
    }
}
