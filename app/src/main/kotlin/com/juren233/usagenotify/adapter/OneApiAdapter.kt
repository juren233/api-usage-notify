package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OneApiAdapter(
    private val client: OkHttpClient,
    override val type: SiteType = SiteType.ONE_API,
) : SiteAdapter {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun queryBalance(baseUrl: String, credential: String): BalanceResult {
        return withContext(Dispatchers.IO) {
            try {
                val sessionCookie = login(baseUrl, credential)
                    ?: return@withContext BalanceResult.Error("请输入账号和密码")
                val url = baseUrl.trimEnd('/') + "/api/user/self"
                val request = Request.Builder()
                    .url(url)
                    .header("Cookie", sessionCookie)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                    ?: return@withContext BalanceResult.Error("Empty response")

                if (!response.isSuccessful) {
                    return@withContext BalanceResult.Error("HTTP ${response.code}: $body")
                }

                val root = json.parseToJsonElement(body).jsonObject
                val success = root["success"]?.jsonPrimitive?.boolean ?: false
                if (!success) {
                    val msg = root["message"]?.jsonPrimitive?.contentOrNull ?: "Unknown error"
                    return@withContext BalanceResult.Error(msg)
                }

                val data = root["data"]?.jsonObject
                    ?: return@withContext BalanceResult.Error("No data field")

                val quota = extractQuota(data) ?: 0.0
                val balance = quota / 500_000.0

                BalanceResult.Success(balance, type)
            } catch (e: Exception) {
                BalanceResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun testConnection(baseUrl: String, credential: String): BalanceResult {
        return queryBalance(baseUrl, credential)
    }

    private fun login(baseUrl: String, credential: String): String? {
        val loginCredential = decodeGatewayLoginCredential(credential, json) ?: return null
        val payload = buildJsonObject {
            put("username", loginCredential.account)
            put("password", loginCredential.password)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/api/user/login")
            .post(payload)
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw IllegalStateException("登录响应为空")

        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code}: $body")
        }

        val root = json.parseToJsonElement(body).jsonObject
        val success = root["success"]?.jsonPrimitive?.boolean ?: false
        if (!success) {
            val message = root["message"]?.jsonPrimitive?.contentOrNull ?: "登录失败"
            throw IllegalStateException(message)
        }

        val data = root["data"]?.jsonObjectOrNull()
        if (data?.get("require_2fa")?.jsonPrimitiveOrNull()?.booleanOrNull == true) {
            throw IllegalStateException("该账号启用了两步验证，暂不支持自动登录")
        }

        return response.headers("Set-Cookie")
            .map { it.substringBefore(';') }
            .filter { it.isNotBlank() }
            .joinToString("; ")
            .takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("登录响应缺少会话 Cookie")
    }

    internal companion object {
        fun extractQuota(data: JsonObject): Double? {
            return data.firstNumber("quota", "remain_quota", "balance", "credit", "credits")
                ?: data.jsonObjectOrNull("user")?.firstNumber("quota", "remain_quota", "balance", "credit", "credits")
        }

        private fun JsonObject.firstNumber(vararg keys: String): Double? {
            for (key in keys) {
                val value = this[key]?.jsonPrimitiveOrNull()?.doubleOrNull
                if (value != null) return value
            }
            return null
        }

        private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

        private fun JsonObject.jsonObjectOrNull(key: String): JsonObject? = this[key] as? JsonObject

        private fun JsonElement.jsonPrimitiveOrNull() = try {
            jsonPrimitive
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
