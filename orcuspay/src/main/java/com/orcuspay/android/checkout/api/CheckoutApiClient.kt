package com.orcuspay.android.checkout.api

import com.orcuspay.android.OrcusPayException
import com.orcuspay.android.checkout.models.CheckoutData
import com.orcuspay.android.checkout.models.SSLCommerzSessionResponse
import com.orcuspay.android.checkout.models.StripeSessionResponse
import com.orcuspay.android.checkout.models.VerifyResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

internal class CheckoutApiClient(
    private val apiUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getCheckoutData(sessionId: String): CheckoutData {
        val request = Request.Builder()
            .url("${apiUrl}/v1/checkout/sessions/$sessionId/checkout")
            .get()
            .addHeader("Accept", "application/json")
            .build()

        return executeAndParse(request)
    }

    fun createStripeSession(
        sessionId: String,
        businessPaymentMethodId: String,
    ): StripeSessionResponse {
        val body = buildJsonBody(
            "business_payment_method_id" to businessPaymentMethodId,
        )
        val request = Request.Builder()
            .url("${apiUrl}/v1/checkout/sessions/$sessionId/stripe")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        return executeAndParse(request)
    }

    fun createSSLCommerzSession(
        sessionId: String,
        businessPaymentMethodId: String,
    ): SSLCommerzSessionResponse {
        val body = buildJsonBody(
            "business_payment_method_id" to businessPaymentMethodId,
        )
        val request = Request.Builder()
            .url("${apiUrl}/v1/checkout/sessions/$sessionId/sslcommerz")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        return executeAndParse(request)
    }

    fun verifyTransaction(
        sessionId: String,
        transactionId: String,
        paymentMethodId: String,
        businessPaymentMethodId: String,
    ): VerifyResponse {
        val body = buildJsonBody(
            "transaction_id" to transactionId,
            "payment_method_id" to paymentMethodId,
            "business_payment_method_id" to businessPaymentMethodId,
        )
        val request = Request.Builder()
            .url("${apiUrl}/v1/checkout/sessions/$sessionId/verify")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        return executeAndParse(request)
    }

    private inline fun <reified T> executeAndParse(request: Request): T {
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val parsed = if (raw.isBlank()) {
                JsonObject(emptyMap())
            } else {
                json.parseToJsonElement(raw).jsonObject
            }

            if (!response.isSuccessful) {
                val errorObj = parsed["error"]?.jsonObject
                val message = errorObj?.get("message")?.jsonPrimitive?.content
                    ?: "HTTP ${response.code}"
                throw OrcusPayException(message, response.code, parsed)
            }

            val data = parsed["data"]
                ?: throw OrcusPayException("Missing data in response", response.code, parsed)

            return json.decodeFromJsonElement(
                kotlinx.serialization.serializer(),
                data,
            )
        }
    }

    private fun buildJsonBody(vararg pairs: Pair<String, String>): okhttp3.RequestBody {
        val map = pairs.toMap()
        val jsonStr = json.encodeToString(
            kotlinx.serialization.builtins.MapSerializer(
                kotlinx.serialization.builtins.serializer<String>(),
                kotlinx.serialization.builtins.serializer<String>(),
            ),
            map,
        )
        return jsonStr.toRequestBody("application/json; charset=utf-8".toMediaType())
    }
}
