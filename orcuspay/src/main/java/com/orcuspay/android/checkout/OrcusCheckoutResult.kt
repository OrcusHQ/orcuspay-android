package com.orcuspay.android.checkout

sealed class OrcusCheckoutResult {
    data class Completed(val sessionId: String) : OrcusCheckoutResult()
    data class Failed(val sessionId: String, val error: String) : OrcusCheckoutResult()
    data object Canceled : OrcusCheckoutResult()
}
