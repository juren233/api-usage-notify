package com.juren233.usagenotify.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.BalanceRecord
import com.juren233.usagenotify.data.model.Site
import com.juren233.usagenotify.data.repository.BalanceRepository
import com.juren233.usagenotify.data.repository.SiteRepository
import com.juren233.usagenotify.service.PollingStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteDetailViewModel @Inject constructor(
    private val balanceRepository: BalanceRepository,
    private val siteRepository: SiteRepository,
    pollingStateHolder: PollingStateHolder,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val siteId: Long = savedStateHandle["siteId"]!!

    private val _site = MutableStateFlow<Site?>(null)
    val site: StateFlow<Site?> = _site

    val historyDays = MutableStateFlow(7)

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<BalanceRecord>> = historyDays.flatMapLatest { days ->
        balanceRepository.observeHistory(siteId, days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balanceResult: StateFlow<BalanceResult?> = pollingStateHolder.state
        .map { it.balances[siteId] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            _site.value = siteRepository.getSiteById(siteId)
        }
    }

    fun setHistoryDays(days: Int) {
        historyDays.value = days
    }
}
