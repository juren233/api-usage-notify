package com.juren233.usagenotify.service

import com.juren233.usagenotify.data.model.BalanceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class PollingState(
    val balances: Map<Long, BalanceResult> = emptyMap(),
    val totalBalance: Double = 0.0,
    val lastUpdated: Long = 0L,
    val isPolling: Boolean = false,
)

@Singleton
class PollingStateHolder @Inject constructor() {
    private val _state = MutableStateFlow(PollingState())
    val state: StateFlow<PollingState> = _state.asStateFlow()

    fun update(transform: (PollingState) -> PollingState) {
        _state.update(transform)
    }
}
