package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.SiteType
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteAdapterFactory @Inject constructor(
    private val client: OkHttpClient,
) {
    private val adapters: Map<SiteType, SiteAdapter> by lazy {
        val oneApi = OneApiAdapter(client)
        val newApi = OneApiAdapter(client, SiteType.NEW_API)
        val sub2Api = Sub2ApiAdapter(client)
        val oneHub = OneHubAdapter(client)
        val candidates = listOf(sub2Api, newApi, oneApi, oneHub)
        mapOf(
            SiteType.AUTO to AutoDetectAdapter(candidates),
            SiteType.ONE_API to oneApi,
            SiteType.NEW_API to newApi,
            SiteType.SUB2API to sub2Api,
            SiteType.ONE_HUB to oneHub,
        )
    }

    fun get(type: SiteType): SiteAdapter =
        adapters[type] ?: throw IllegalArgumentException("No adapter for $type")
}
