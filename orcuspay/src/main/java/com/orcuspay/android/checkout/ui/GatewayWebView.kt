package com.orcuspay.android.checkout.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun GatewayWebView(
    url: String,
    isHtml: Boolean,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    var progress by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 600.dp)
                .weight(1f, fill = false),
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val loadUrl = request?.url?.toString() ?: return false
                                if (isGatewayCallback(loadUrl)) {
                                    onComplete()
                                    return true
                                }
                                return false
                            }
                        }

                        if (isHtml) {
                            loadDataWithBaseURL(
                                "https://checkout.orcuspay.com",
                                url,
                                "text/html",
                                "UTF-8",
                                null,
                            )
                        } else {
                            loadUrl(url)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
        ) {
            Text("Cancel")
        }
    }
}

private fun isGatewayCallback(url: String): Boolean {
    return url.contains("/api/checkout/stripe-callback") ||
        url.contains("/api/checkout/sslcommerz-callback") ||
        url.contains("/checkout/success") ||
        url.contains("/checkout/cancel")
}
