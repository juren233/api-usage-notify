package com.juren233.usagenotify.adapter

import com.juren233.usagenotify.data.model.BalanceResult
import com.juren233.usagenotify.data.model.SiteType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneApiAdapterTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun queryBalanceLogsInWithAccountAndReadsSelfQuota() = runTest {
        val seenRequests = mutableListOf<Request>()
        val client = fakeClient { request ->
            seenRequests += request
            when (request.url.encodedPath) {
                "/api/user/login" -> {
                    val body = request.bodyText()
                    assertTrue(body.contains("\"username\":\"user@example.com\""))
                    assertTrue(body.contains("\"password\":\"password123\""))
                    jsonResponse(
                        request = request,
                        body = """{"success":true,"message":"","data":{"id":1}}""",
                        headers = Headers.headersOf("Set-Cookie", "session=abc; Path=/; HttpOnly"),
                    )
                }
                "/api/user/self" -> {
                    assertEquals("session=abc", request.header("Cookie"))
                    jsonResponse(request, """{"success":true,"data":{"quota":1000000}}""")
                }
                else -> error("未知请求路径: ${request.url.encodedPath}")
            }
        }
        val adapter = OneApiAdapter(client)

        val result = adapter.queryBalance("https://one-api.example.com", encodeGatewayLoginCredential("user@example.com", "password123"))

        assertTrue(result is BalanceResult.Success)
        result as BalanceResult.Success
        assertEquals(2.0, result.balance, 0.0)
        assertEquals(SiteType.ONE_API, result.detectedType)
        assertEquals(listOf("/api/user/login", "/api/user/self"), seenRequests.map { it.url.encodedPath })
    }

    @Test
    fun queryBalanceReturnsClearErrorWhenLoginRequires2FA() = runTest {
        val client = fakeClient { request ->
            jsonResponse(request, """{"success":true,"data":{"require_2fa":true}}""")
        }
        val adapter = OneApiAdapter(client)

        val result = adapter.queryBalance("https://new-api.example.com", encodeGatewayLoginCredential("user", "password123"))

        assertTrue(result is BalanceResult.Error)
        assertEquals("该账号启用了两步验证，暂不支持自动登录", (result as BalanceResult.Error).message)
    }

    @Test
    fun extractQuotaReadsNestedUserQuota() {
        val root = json.parseToJsonElement("""{"user":{"quota": 250000}}""").jsonObject

        val quota = OneApiAdapter.extractQuota(root)

        assertEquals(250000.0, quota ?: -1.0, 0.0)
    }

    private fun fakeClient(handler: (Request) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain -> handler(chain.request()) })
            .build()
    }

    private fun jsonResponse(
        request: Request,
        body: String,
        code: Int = 200,
        headers: Headers = Headers.headersOf(),
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .headers(headers)
            .body(body.toResponseBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    private fun Request.bodyText(): String {
        val buffer = Buffer()
        body?.writeTo(buffer)
        return buffer.readUtf8()
    }
}
