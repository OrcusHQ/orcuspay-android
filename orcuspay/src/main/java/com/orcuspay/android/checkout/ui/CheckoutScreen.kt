package com.orcuspay.android.checkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orcuspay.android.checkout.CheckoutViewModel
import com.orcuspay.android.checkout.CheckoutScreen as ScreenState

@Composable
internal fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
) {
    val screen by viewModel.screen.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    BackHandler {
        when (screen) {
            is ScreenState.MethodSelection, is ScreenState.Loading, is ScreenState.Error -> onCancel()
            else -> viewModel.goBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Crossfade(targetState = screen, label = "checkout_screen") { currentScreen ->
                when (currentScreen) {
                    is ScreenState.Loading -> {
                        LoadingContent()
                    }
                    is ScreenState.Error -> {
                        ErrorContent(
                            message = currentScreen.message,
                            onRetry = { viewModel.loadCheckoutData() },
                            onCancel = onCancel,
                        )
                    }
                    is ScreenState.MethodSelection -> {
                        PaymentMethodList(
                            checkoutData = currentScreen.data,
                            onMethodSelected = { viewModel.selectPaymentMethod(it) },
                            onCancel = onCancel,
                        )
                    }
                    is ScreenState.ManualVerify -> {
                        VerifyScreen(
                            checkoutData = currentScreen.data,
                            method = currentScreen.method,
                            isProcessing = isProcessing,
                            onSubmit = { txId ->
                                viewModel.submitTransactionId(txId, currentScreen.method)
                            },
                            onBack = { viewModel.goBack() },
                        )
                    }
                    is ScreenState.StripeWebView -> {
                        GatewayWebView(
                            url = buildStripeCheckoutHtml(
                                currentScreen.publishableKey,
                                currentScreen.clientSecret,
                            ),
                            isHtml = true,
                            onComplete = { viewModel.onGatewayComplete() },
                            onCancel = { viewModel.goBack() },
                        )
                    }
                    is ScreenState.SSLCommerzWebView -> {
                        GatewayWebView(
                            url = currentScreen.url,
                            isHtml = false,
                            onComplete = { viewModel.onGatewayComplete() },
                            onCancel = { viewModel.goBack() },
                        )
                    }
                    is ScreenState.Success -> {
                        LaunchedEffect(Unit) { onSuccess() }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun buildStripeCheckoutHtml(publishableKey: String, clientSecret: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <script src="https://js.stripe.com/v3/"></script>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: -apple-system, sans-serif; padding: 16px; background: #fff; }
                #checkout { min-height: 300px; }
                .loading { text-align: center; padding: 40px; color: #64748b; }
            </style>
        </head>
        <body>
            <div id="checkout"><div class="loading">Loading payment form...</div></div>
            <script>
                const stripe = Stripe('$publishableKey');
                stripe.initEmbeddedCheckout({ clientSecret: '$clientSecret' })
                    .then(checkout => checkout.mount('#checkout'))
                    .catch(err => {
                        document.getElementById('checkout').innerHTML =
                            '<div class="loading" style="color:#dc2626">' + err.message + '</div>';
                    });
            </script>
        </body>
        </html>
    """.trimIndent()
}
