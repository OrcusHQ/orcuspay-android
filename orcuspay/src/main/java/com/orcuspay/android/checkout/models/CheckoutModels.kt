package com.orcuspay.android.checkout.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val status: Int,
)

@Serializable
data class CheckoutData(
    val id: String,
    val status: String,
    val amount: Double,
    @SerialName("amount_paisa") val amountPaisa: Long,
    val subtotal: Double? = null,
    @SerialName("subtotal_paisa") val subtotalPaisa: Long? = null,
    val currency: String = "BDT",
    val customer: Customer = Customer(),
    val business: Business,
    @SerialName("payment_methods") val paymentMethods: List<PaymentMethodInfo>,
    @SerialName("line_items") val lineItems: List<LineItem> = emptyList(),
    @SerialName("is_test_mode") val isTestMode: Boolean = false,
)

@Serializable
data class Customer(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
)

@Serializable
data class Business(
    val name: String,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("support_email") val supportEmail: String? = null,
    @SerialName("support_phone") val supportPhone: String? = null,
    val branding: Branding = Branding(),
)

@Serializable
data class Branding(
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("brand_color") val brandColor: String? = null,
    @SerialName("accent_color") val accentColor: String? = null,
)

@Serializable
data class PaymentMethodInfo(
    val id: String,
    val name: String,
    @SerialName("checkout_name") val checkoutName: String? = null,
    @SerialName("unique_name") val uniqueName: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("payment_method_id") val paymentMethodId: String,
)

@Serializable
data class LineItem(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int,
    val price: Long,
    @SerialName("pricing_mode") val pricingMode: String? = null,
)

@Serializable
data class StripeSessionResponse(
    @SerialName("client_secret") val clientSecret: String,
    @SerialName("publishable_key") val publishableKey: String,
)

@Serializable
data class SSLCommerzSessionResponse(
    val url: String,
    @SerialName("session_key") val sessionKey: String,
)

@Serializable
data class VerifyResponse(
    val status: String,
)

enum class GatewayType {
    STRIPE,
    SSLCOMMERZ,
    MANUAL_VERIFY,
}

fun PaymentMethodInfo.gatewayType(): GatewayType = when (uniqueName) {
    "stripe" -> GatewayType.STRIPE
    "sslcommerz" -> GatewayType.SSLCOMMERZ
    else -> GatewayType.MANUAL_VERIFY
}
