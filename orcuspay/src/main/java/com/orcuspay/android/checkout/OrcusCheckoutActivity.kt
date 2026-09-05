package com.orcuspay.android.checkout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.orcuspay.android.checkout.ui.CheckoutScreen
import com.orcuspay.android.checkout.ui.theme.OrcusCheckoutTheme

class OrcusCheckoutActivity : ComponentActivity() {

    private val viewModel: CheckoutViewModel by viewModels {
        CheckoutViewModel.Factory(
            sessionId = intent.getStringExtra(EXTRA_SESSION_ID).orEmpty(),
            apiUrl = intent.getStringExtra(EXTRA_API_URL) ?: DEFAULT_API_URL,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OrcusCheckoutTheme {
                CheckoutScreen(
                    viewModel = viewModel,
                    onSuccess = {
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(EXTRA_SESSION_ID, intent.getStringExtra(EXTRA_SESSION_ID)),
                        )
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onError = { error ->
                        setResult(
                            RESULT_FAILED,
                            Intent()
                                .putExtra(EXTRA_SESSION_ID, intent.getStringExtra(EXTRA_SESSION_ID))
                                .putExtra(EXTRA_ERROR, error),
                        )
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        internal const val EXTRA_SESSION_ID = "orcus_session_id"
        internal const val EXTRA_API_URL = "orcus_api_url"
        internal const val EXTRA_ERROR = "orcus_error"
        internal const val RESULT_FAILED = 2
        private const val DEFAULT_API_URL = "https://brain.orcuspay.com/api"

        fun createIntent(context: Context, sessionId: String, apiUrl: String): Intent {
            return Intent(context, OrcusCheckoutActivity::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_API_URL, apiUrl)
            }
        }
    }
}
