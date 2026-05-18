package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class Sub2ApiAdapter(
    private val client: OkHttpClient,
) : SiteAdapter {

    override val type: SiteType = SiteType.SUB2API
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult {
        return withContext(Dispatchers.IO) {
            try {
                queryModernSub2Api(baseUrl, credential) ?: queryLegacySub2Api(baseUrl, credential)
            } catch (e: Exception) {
                BalanceResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun testConnection(baseUrl: String, credential: String): BalanceResult {
        return queryBalance(baseUrl, credential)
    }

    private fun queryModernSub2Api(baseUrl: String, credential: String): BalanceResult? {
        val url = baseUrl.trimEnd('/') + "/api/v1/auth/me"
        val token = resolveModernToken(baseUrl, credential)
            ?: return BalanceResult.Error("请输入 Sub2API 账号和密码")
        val response = client.newCall(buildRequest(url, token)).execute()
        val body = response.body?.string()
            ?: return BalanceResult.Error("Empty response")

        if (response.code == 404) return null

        if (!response.isSuccessful) {
            if (response.code == 401) {
                return BalanceResult.Error("Sub2API 登录态无效")
            }
            return BalanceResult.Error(formatHttpError(response.code, body))
        }

        val root = json.parseToJsonElement(body).jsonObject
        return extractSub2ApiBalance(root)?.let { BalanceResult.Success(it, type) }
            ?: BalanceResult.Error("Sub2API 响应缺少余额字段")
    }

    private fun resolveModernToken(baseUrl: String, credential: String): String? {
        val loginCredential = decodeGatewayLoginCredential(credential, json)
            ?: return credential.takeIf { it.isNotBlank() && !it.isOpenAiApiKey() }
        val url = baseUrl.trimEnd('/') + "/api/v1/auth/login"
        val payload = buildJsonObject {
            put("email", loginCredential.account)
            put("password", loginCredential.password)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(payload)
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw IllegalStateException("登录响应为空")

        if (!response.isSuccessful) {
            throw IllegalStateException(formatHttpError(response.code, body))
        }

        val root = json.parseToJsonElement(body).jsonObject
        val requires2fa = root["requires_2fa"]?.jsonPrimitiveOrNull()?.contentOrNull == "true"
        if (requires2fa) {
            throw IllegalStateException("该账号启用了两步验证，暂不支持自动登录")
        }
        return root["access_token"]?.jsonPrimitive?.contentOrNull
            ?: root["data"]?.jsonObjectOrNull()?.get("access_token")?.jsonPrimitiveOrNull()?.contentOrNull
            ?: throw IllegalStateException("登录响应缺少 access_token")
    }

    private fun queryLegacySub2Api(baseUrl: String, credential: String): BalanceResult {
        val url = baseUrl.trimEnd('/') + "/v1/dashboard/billing/subscription"
        val response = client.newCall(buildRequest(url, credential)).execute()
        val body = response.body?.string()
            ?: return BalanceResult.Error("Empty response")

        if (!response.isSuccessful) {
            return BalanceResult.Error(formatHttpError(response.code, body))
        }

        val root = json.parseToJsonElement(body).jsonObject
        val balance = root["hard_limit_usd"]?.jsonPrimitive?.double
            ?: root["soft_limit_usd"]?.jsonPrimitive?.double
            ?: 0.0

        return BalanceResult.Success(balance, type)
    }

    private fun buildRequest(url: String, credential: String): Request =
        Request.Builder()
            .url(url)
            .header("Authorization", credential.authorizationHeaderValue())
            .get()
            .build()

    private fun formatHttpError(code: Int, body: String): String {
        return try {
            val root = json.parseToJsonElement(body).jsonObject
            val message = root["message"]?.jsonPrimitive?.contentOrNull
                ?: root["error"]?.jsonPrimitive?.contentOrNull
            if (message.isNullOrBlank()) "HTTP $code: $body" else "HTTP $code: $message"
        } catch (_: Exception) {
            "HTTP $code: $body"
        }
    }

    internal companion object {
        fun extractSub2ApiBalance(root: JsonObject): Double? {
            val data = root["data"]?.jsonObjectOrNull()
            return root.firstNumber("balance", "quota", "credit", "credits")
                ?: data?.firstNumber("balance", "quota", "credit", "credits")
                ?: data?.jsonObjectOrNull("user")?.firstNumber("balance", "quota", "credit", "credits")
        }

        private fun JsonObject.firstNumber(vararg keys: String): Double? {
            for (key in keys) {
                val value = this[key]?.jsonPrimitiveOrNull()?.doubleOrNull
                if (value != null) return value
            }
            return null
        }

        fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

        fun JsonObject.jsonObjectOrNull(key: String): JsonObject? = this[key] as? JsonObject

        fun JsonElement.jsonPrimitiveOrNull() = try {
            jsonPrimitive
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

internal fun String.authorizationHeaderValue(): String {
    val value = trim()
    return if (value.startsWith("Bearer ", ignoreCase = true)) value else "Bearer $value"
}

private fun String.isOpenAiApiKey(): Boolean {
    val value = trim().removePrefix("Bearer ").trim()
    return value.startsWith("sk-", ignoreCase = true)
}
