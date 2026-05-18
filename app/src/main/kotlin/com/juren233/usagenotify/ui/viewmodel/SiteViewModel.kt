package com.juren233.usagenotify.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juren233.usagenotify.adapter.decodeGatewayLoginCredential
import com.juren233.usagenotify.adapter.encodeGatewayLoginCredential
import com.juren233.usagenotify.adapter.SiteAdapterFactory
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.Site
import com.juren233.usagenotify.data.model.SiteType
import com.juren233.usagenotify.data.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteFormState(
    val name: String = "",
    val url: String = "",
    val credential: String = "",
    val account: String = "",
    val password: String = "",
    val testResult: BalanceResult? = null,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val existingSite: Site? = null,
)

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val adapterFactory: SiteAdapterFactory,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _form = MutableStateFlow(SiteFormState())
    val form: StateFlow<SiteFormState> = _form.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun loadSite(siteId: Long) {
        viewModelScope.launch {
            val site = siteRepository.getSiteById(siteId) ?: return@launch
            val credential = siteRepository.getCredential(site) ?: ""
            val loginCredential = decodeGatewayLoginCredential(credential)
            _form.value = SiteFormState(
                name = site.name,
                url = site.url,
                credential = if (loginCredential == null) credential else "",
                account = loginCredential?.account.orEmpty(),
                password = loginCredential?.password.orEmpty(),
                existingSite = site,
            )
        }
    }

    fun updateName(name: String) { _form.value = _form.value.copy(name = name) }
    fun updateUrl(url: String) { _form.value = _form.value.copy(url = url) }
    fun updateCredential(credential: String) { _form.value = _form.value.copy(credential = credential) }
    fun updateAccount(account: String) { _form.value = _form.value.copy(account = account) }
    fun updatePassword(password: String) { _form.value = _form.value.copy(password = password) }

    fun testConnection() {
        val state = _form.value
        _form.value = state.copy(isTesting = true, testResult = null)
        viewModelScope.launch {
            val adapter = adapterFactory.get(SiteType.AUTO)
            val result = adapter.testConnection(state.url, state.connectionCredential())
            _form.value = _form.value.copy(isTesting = false, testResult = result)
        }
    }

    fun saveSite() {
        val state = _form.value
        _form.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val existing = state.existingSite
            val detectedType = (state.testResult as? BalanceResult.Success)?.detectedType ?: SiteType.AUTO
            if (existing != null) {
                siteRepository.updateSite(
                    existing.copy(name = state.name, url = state.url, type = detectedType),
                    newCredential = state.connectionCredential(),
                )
            } else {
                siteRepository.addSite(state.name, state.url, detectedType, state.connectionCredential())
            }
            _saved.value = true
        }
    }

    fun deleteSite() {
        val site = _form.value.existingSite ?: return
        viewModelScope.launch {
            siteRepository.deleteSite(site)
            _saved.value = true
        }
    }
}

fun SiteFormState.usesAccountLogin(): Boolean = true

fun SiteFormState.hasCredentialInput(): Boolean {
    return if (usesAccountLogin()) {
        account.isNotBlank() && password.isNotBlank()
    } else {
        credential.isNotBlank()
    }
}

fun SiteFormState.connectionCredential(): String {
    return if (usesAccountLogin()) {
        encodeGatewayLoginCredential(account, password)
    } else {
        credential
    }
}
