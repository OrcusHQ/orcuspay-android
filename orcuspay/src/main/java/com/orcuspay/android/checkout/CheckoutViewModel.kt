package com.orcuspay.android.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.orcuspay.android.checkout.api.CheckoutApiClient
import com.orcuspay.android.checkout.models.CheckoutData
import com.orcuspay.android.checkout.models.GatewayType
import com.orcuspay.android.checkout.models.PaymentMethodInfo
import com.orcuspay.android.checkout.models.gatewayType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed class CheckoutScreen {
    data object Loading : CheckoutScreen()
    data class Error(val message: String) : CheckoutScreen()
    data class MethodSelection(val data: CheckoutData) : CheckoutScreen()
    data class ManualVerify(val data: CheckoutData, val method: PaymentMethodInfo) : CheckoutScreen()
    data class StripeWebView(
        val clientSecret: String,
        val publishableKey: String,
    ) : CheckoutScreen()
    data class SSLCommerzWebView(val url: String) : CheckoutScreen()
    data object Success : CheckoutScreen()
}

internal class CheckoutViewModel(
    private val sessionId: String,
    private val api: CheckoutApiClient,
) : ViewModel() {

    private val _screen = MutableStateFlow<CheckoutScreen>(CheckoutScreen.Loading)
    val screen: StateFlow<CheckoutScreen> = _screen

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private var checkoutData: CheckoutData? = null

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData() {
        viewModelScope.launch {
            _screen.value = CheckoutScreen.Loading
            try {
                val data = withContext(Dispatchers.IO) {
                    api.getCheckoutData(sessionId)
                }
                checkoutData = data
                if (data.status == "SUCCEEDED") {
                    _screen.value = CheckoutScreen.Success
                } else {
                    _screen.value = CheckoutScreen.MethodSelection(data)
                }
            } catch (e: Exception) {
                _screen.value = CheckoutScreen.Error(e.message ?: "Failed to load checkout")
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethodInfo) {
        val data = checkoutData ?: return
        when (method.gatewayType()) {
            GatewayType.MANUAL_VERIFY -> {
                _screen.value = CheckoutScreen.ManualVerify(data, method)
            }
            GatewayType.STRIPE -> initiateStripe(method)
            GatewayType.SSLCOMMERZ -> initiateSSLCommerz(method)
        }
    }

    fun goBack() {
        val data = checkoutData
        if (data != null) {
            _screen.value = CheckoutScreen.MethodSelection(data)
        }
    }

    fun submitTransactionId(
        transactionId: String,
        method: PaymentMethodInfo,
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    api.verifyTransaction(
                        sessionId = sessionId,
                        transactionId = transactionId,
                        paymentMethodId = method.paymentMethodId,
                        businessPaymentMethodId = method.id,
                    )
                }
                if (result.status == "SUCCEEDED") {
                    _screen.value = CheckoutScreen.Success
                } else {
                    _screen.value = CheckoutScreen.Error("Verification failed")
                }
            } catch (e: Exception) {
                _screen.value = CheckoutScreen.Error(e.message ?: "Verification failed")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun onGatewayComplete() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val data = withContext(Dispatchers.IO) {
                    api.getCheckoutData(sessionId)
                }
                if (data.status == "SUCCEEDED") {
                    _screen.value = CheckoutScreen.Success
                } else {
                    _screen.value = CheckoutScreen.Error("Payment was not confirmed. Please try again.")
                }
            } catch (e: Exception) {
                _screen.value = CheckoutScreen.Error(e.message ?: "Failed to verify payment")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun initiateStripe(method: PaymentMethodInfo) {
        viewModelScope.launch {
            _screen.value = CheckoutScreen.Loading
            try {
                val session = withContext(Dispatchers.IO) {
                    api.createStripeSession(sessionId, method.id)
                }
                _screen.value = CheckoutScreen.StripeWebView(
                    clientSecret = session.clientSecret,
                    publishableKey = session.publishableKey,
                )
            } catch (e: Exception) {
                _screen.value = CheckoutScreen.Error(e.message ?: "Failed to start Stripe checkout")
            }
        }
    }

    private fun initiateSSLCommerz(method: PaymentMethodInfo) {
        viewModelScope.launch {
            _screen.value = CheckoutScreen.Loading
            try {
                val session = withContext(Dispatchers.IO) {
                    api.createSSLCommerzSession(sessionId, method.id)
                }
                _screen.value = CheckoutScreen.SSLCommerzWebView(session.url)
            } catch (e: Exception) {
                _screen.value = CheckoutScreen.Error(e.message ?: "Failed to start payment")
            }
        }
    }

    class Factory(
        private val sessionId: String,
        private val apiUrl: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutViewModel(sessionId, CheckoutApiClient(apiUrl)) as T
        }
    }
}
