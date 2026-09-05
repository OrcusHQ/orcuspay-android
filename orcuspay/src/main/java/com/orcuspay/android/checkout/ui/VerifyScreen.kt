package com.orcuspay.android.checkout.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.orcuspay.android.checkout.models.CheckoutData
import com.orcuspay.android.checkout.models.PaymentMethodInfo

@Composable
internal fun VerifyScreen(
    checkoutData: CheckoutData,
    method: PaymentMethodInfo,
    isProcessing: Boolean,
    onSubmit: (transactionId: String) -> Unit,
    onBack: () -> Unit,
) {
    var transactionId by remember { mutableStateOf("") }
    val canSubmit = transactionId.isNotBlank() && !isProcessing

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (method.imageUrl != null) {
            AsyncImage(
                model = method.imageUrl,
                contentDescription = method.name,
                modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Pay with ${method.checkoutName ?: method.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Send ${formatAmountSimple(checkoutData.amount, checkoutData.currency)} to ${checkoutData.business.name}, then enter your Transaction ID below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = transactionId,
            onValueChange = { transactionId = it },
            label = { Text("Transaction ID") },
            placeholder = { Text("e.g. ABC123XYZ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canSubmit) onSubmit(transactionId.trim()) },
            ),
            enabled = !isProcessing,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSubmit(transactionId.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
            shape = RoundedCornerShape(12.dp),
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Verify Payment", modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun formatAmountSimple(amount: Double, currency: String): String {
    return "$currency ${String.format("%.2f", amount)}"
}
