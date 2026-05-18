package com.juren233.usagenotify.data.model

sealed class BalanceResult {
    data class Success(val balance: Double, val detectedType: SiteType? = null) : BalanceResult()
    data class Error(val message: String) : BalanceResult()
}
