package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoDetectAdapterTest {

    @Test
    fun queryBalanceReturnsFirstSuccessfulCandidate() = runTest {
        val adapter = AutoDetectAdapter(
            listOf(
                FakeAdapter(SiteType.ONE_API, BalanceResult.Error("不匹配")),
                FakeAdapter(SiteType.SUB2API, BalanceResult.Success(12.5)),
            ),
        )

        val result = adapter.queryBalance("https://example.com", "token")

        assertTrue(result is BalanceResult.Success)
        result as BalanceResult.Success
        assertEquals(12.5, result.balance, 0.0)
        assertEquals(SiteType.SUB2API, result.detectedType)
    }

    @Test
    fun queryBalanceReturnsAggregatedErrorsWhenNoCandidateMatches() = runTest {
        val adapter = AutoDetectAdapter(
            listOf(
                FakeAdapter(SiteType.ONE_API, BalanceResult.Error("不匹配")),
                FakeAdapter(SiteType.SUB2API, BalanceResult.Error("无效 token")),
            ),
        )

        val result = adapter.queryBalance("https://example.com", "token")

        assertTrue(result is BalanceResult.Error)
        val message = (result as BalanceResult.Error).message
        assertTrue(message.contains("One API: 不匹配"))
        assertTrue(message.contains("Sub2API: 无效 token"))
    }

    private class FakeAdapter(
        override val type: SiteType,
        private val result: BalanceResult,
    ) : SiteAdapter {
        override suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult = result
        override suspend fun testConnection(baseUrl: String, credential: String): BalanceResult = result
    }
}
