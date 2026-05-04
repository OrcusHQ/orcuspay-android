package com.orcuspay.android

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OrcusPayClient(
    private val accessKey: String,
    private val secretKey: String,
    private val apiUrl: String = DEFAULT_API_URL,
    timeoutSeconds: Long = 30,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class, OrcusPayException::class)
    fun createCheckoutSession(payload: JsonObject): JsonObject {
        return request("POST", "/checkout/session", payload)
    }

    @Throws(IOException::class, OrcusPayException::class)
    fun retrieveCheckoutSession(sessionId: String): JsonObject {
        return request("POST", "/checkout/session/$sessionId", null)
    }

    @Throws(IOException::class, OrcusPayException::class)
    fun listPayments(): JsonObject {
        return request("GET", "/v1/payments", null)
    }

    private fun request(method: String, path: String, payload: JsonObject?): JsonObject {
        val body = payload?.let {
            json.encodeToString(it).toRequestBody(JSON_MEDIA_TYPE)
        }

        val request = Request.Builder()
            .url(apiUrl.trimEnd('/') + path)
            .method(method, body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-auth-access-key", accessKey)
            .addHeader("x-auth-secret-key", secretKey)
            .build()

        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val parsed = if (raw.isBlank()) JsonObject(emptyMap()) else json.parseToJsonElement(raw).jsonObject

            if (!response.isSuccessful) {
                val message = parsed["message"]?.toString()
                    ?: parsed["error"]?.toString()
                    ?: "HTTP ${response.code}"
                throw OrcusPayException(message, response.code, parsed)
            }

            return parsed
        }
    }

    companion object {
        const val DEFAULT_API_URL = "https://brain.orcuspay.com/api"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
