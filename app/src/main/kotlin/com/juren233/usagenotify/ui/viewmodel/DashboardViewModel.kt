package com.juren233.usagenotify.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.juren233.usagenotify.data.model.Site
import com.juren233.usagenotify.data.repository.SiteRepository
import com.juren233.usagenotify.service.PollingState
import com.juren233.usagenotify.service.PollingStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    siteRepository: SiteRepository,
    pollingStateHolder: PollingStateHolder,
) : ViewModel() {

    val sites: StateFlow<List<Site>> = siteRepository.observeAllSites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pollingState: StateFlow<PollingState> = pollingStateHolder.state
}
