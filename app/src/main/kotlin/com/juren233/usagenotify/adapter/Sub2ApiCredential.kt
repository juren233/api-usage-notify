package com.juren233.usagenotify.adapter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class GatewayLoginCredential(
    val account: String,
    val password: String,
)

private const val SUB2API_CREDENTIAL_VERSION = "sub2api-login-v1"
private const val GATEWAY_CREDENTIAL_VERSION = "gateway-login-v1"

fun encodeGatewayLoginCredential(account: String, password: String): String {
    return buildJsonObject {
        put("type", GATEWAY_CREDENTIAL_VERSION)
        put("account", account)
        put("password", password)
    }.toString()
}

fun decodeGatewayLoginCredential(value: String, json: Json = Json): GatewayLoginCredential? {
    return try {
        val root = json.parseToJsonElement(value).jsonObject
        val type = root["type"]?.jsonPrimitive?.contentOrNull
        if (type != GATEWAY_CREDENTIAL_VERSION && type != SUB2API_CREDENTIAL_VERSION) return null
        val account = root["account"]?.jsonPrimitive?.contentOrNull
            ?: root["email"]?.jsonPrimitive?.contentOrNull
            ?: ""
        val password = root["password"]?.jsonPrimitive?.contentOrNull.orEmpty()
        if (account.isBlank() || password.isBlank()) null else GatewayLoginCredential(account, password)
    } catch (_: Exception) {
        null
    }
}

fun encodeSub2ApiCredential(email: String, password: String): String =
    encodeGatewayLoginCredential(email, password)

fun decodeSub2ApiCredential(value: String, json: Json = Json): GatewayLoginCredential? =
    decodeGatewayLoginCredential(value, json)
