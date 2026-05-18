package com.juren233.usagenotify.adapter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class Sub2ApiAdapterTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun extractSub2ApiBalanceReadsTopLevelBalance() {
        val root = json.parseToJsonElement("""{"balance": 8.75}""").jsonObject

        val balance = Sub2ApiAdapter.extractSub2ApiBalance(root)

        assertEquals(8.75, balance ?: -1.0, 0.0)
    }

    @Test
    fun extractSub2ApiBalanceReadsNestedDataBalance() {
        val root = json.parseToJsonElement("""{"code":0,"data":{"balance": 6.25}}""").jsonObject

        val balance = Sub2ApiAdapter.extractSub2ApiBalance(root)

        assertEquals(6.25, balance ?: -1.0, 0.0)
    }

    @Test
    fun extractSub2ApiBalanceReadsNestedUserBalance() {
        val root = json.parseToJsonElement("""{"code":0,"data":{"user":{"balance": 4.5}}}""").jsonObject

        val balance = Sub2ApiAdapter.extractSub2ApiBalance(root)

        assertEquals(4.5, balance ?: -1.0, 0.0)
    }

    @Test
    fun authorizationHeaderValueKeepsExistingBearerPrefix() {
        val header = "Bearer token-value".authorizationHeaderValue()

        assertEquals("Bearer token-value", header)
    }

    @Test
    fun authorizationHeaderValueAddsBearerPrefixForRawToken() {
        val header = "token-value".authorizationHeaderValue()

        assertEquals("Bearer token-value", header)
    }

    @Test
    fun sub2ApiCredentialRoundTripsEmailAndPassword() {
        val encoded = encodeGatewayLoginCredential("user@example.com", "password123")

        val decoded = decodeGatewayLoginCredential(encoded)

        assertEquals("user@example.com", decoded?.account)
        assertEquals("password123", decoded?.password)
    }
}
