package com.orcuspay.android.checkout

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

object OrcusCheckout {

    private const val DEFAULT_API_URL = "https://brain.orcuspay.com/api"

    fun registerForResult(
        activity: ComponentActivity,
        onResult: (OrcusCheckoutResult) -> Unit,
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val data = result.data
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val sessionId = data?.getStringExtra(OrcusCheckoutActivity.EXTRA_SESSION_ID).orEmpty()
                    onResult(OrcusCheckoutResult.Completed(sessionId))
                }
                OrcusCheckoutActivity.RESULT_FAILED -> {
                    val sessionId = data?.getStringExtra(OrcusCheckoutActivity.EXTRA_SESSION_ID).orEmpty()
                    val error = data?.getStringExtra(OrcusCheckoutActivity.EXTRA_ERROR).orEmpty()
                    onResult(OrcusCheckoutResult.Failed(sessionId, error))
                }
                else -> {
                    onResult(OrcusCheckoutResult.Canceled)
                }
            }
        }
    }

    fun open(
        launcher: ActivityResultLauncher<Intent>,
        activity: Activity,
        sessionId: String,
        apiUrl: String = DEFAULT_API_URL,
    ) {
        val intent = OrcusCheckoutActivity.createIntent(activity, sessionId, apiUrl)
        launcher.launch(intent)
    }
}
