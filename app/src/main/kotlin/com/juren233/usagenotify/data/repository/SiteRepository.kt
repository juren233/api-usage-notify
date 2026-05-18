package com.juren233.usagenotify.data.repository

import com.juren233.usagenotify.data.local.CredentialStore
import com.juren233.usagenotify.data.local.SiteDao
import com.juren233.usagenotify.data.model.Site
import com.juren233.usagenotify.data.model.SiteType
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepository @Inject constructor(
    private val siteDao: SiteDao,
    private val credentialStore: CredentialStore,
) {
    fun observeAllSites(): Flow<List<Site>> = siteDao.observeAll()

    suspend fun addSite(name: String, url: String, type: SiteType, credential: String): Long {
        val ref = "site_cred_${UUID.randomUUID()}"
        credentialStore.save(ref, credential)
        return siteDao.upsert(Site(name = name, url = url, type = type, credentialRef = ref))
    }

    suspend fun updateSite(site: Site, newCredential: String? = null) {
        if (newCredential != null) credentialStore.save(site.credentialRef, newCredential)
        siteDao.upsert(site)
    }

    suspend fun deleteSite(site: Site) {
        credentialStore.delete(site.credentialRef)
        siteDao.delete(site)
    }

    suspend fun getSiteById(id: Long): Site? = siteDao.getById(id)

    fun getCredential(site: Site): String? = credentialStore.load(site.credentialRef)
}
