# Orcuspay Android SDK

Kotlin Android SDK for Orcuspay. Supports two integration modes:

1. **Hosted checkout** — create a session server-side, redirect to a browser
2. **Native checkout** — full in-app payment UI with Jetpack Compose (no browser redirect)

## Installation

### JitPack

Add JitPack to your project-level `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.OrcusHQ:orcuspay-android:1.1.0")
}
```

## Native Checkout (Recommended)

Take payments directly inside your app — no browser redirect needed. The SDK
shows a bottom-sheet UI with payment method selection, bKash/Nagad transaction
verification, Stripe checkout, and SSLCommerz gateway.

### 1. Create a checkout session (server-side)

Use the OrcusPay API or any server SDK to create a checkout session and pass the
session ID to your Android app.

```typescript
// Node.js example
const session = await orcus.createCheckoutSession({
  success_url: 'https://yoursite.com/success',
  cancel_url: 'https://yoursite.com/cancel',
  line_items: [{ quantity: 1, price_data: { unit_amount: 50000, product_data: { name: 'Plan' } } }],
})
// Send session.id to your Android app
```

### 2. Register for result and open checkout

```kotlin
import com.orcuspay.android.checkout.OrcusCheckout
import com.orcuspay.android.checkout.OrcusCheckoutResult

class PaymentActivity : ComponentActivity() {

    private val checkoutLauncher = OrcusCheckout.registerForResult(this) { result ->
        when (result) {
            is OrcusCheckoutResult.Completed -> {
                // Payment succeeded — result.sessionId has the payment ID
                showSuccess()
            }
            is OrcusCheckoutResult.Failed -> {
                // Payment failed — result.error has the message
                showError(result.error)
            }
            is OrcusCheckoutResult.Canceled -> {
                // User dismissed the checkout
            }
        }
    }

    fun startPayment(sessionId: String) {
        OrcusCheckout.open(
            launcher = checkoutLauncher,
            activity = this,
            sessionId = sessionId,
        )
    }
}
```

### What the user sees

1. **Payment method selection** — available methods with icons, business branding, and amount
2. **bKash / Nagad / Rocket** — native form to enter the transaction ID after sending money
3. **Stripe** — embedded checkout form (card, Google Pay, etc.)
4. **SSLCommerz** — gateway page for local bank and card payments

### Custom API URL

For self-hosted OrcusPay instances:

```kotlin
OrcusCheckout.open(
    launcher = checkoutLauncher,
    activity = this,
    sessionId = sessionId,
    apiUrl = "https://your-instance.com/api",
)
```

## Hosted Checkout

For the redirect-based flow, create a session server-side and open the checkout
URL in a browser:

```kotlin
val client = OrcusPayClient(
    accessKey = "access_key",
    secretKey = "secret_key",
)

val session = client.createCheckoutSession(
    buildJsonObject {
        putJsonObject("customer") {
            put("name", "Test Customer")
            put("email", "test@example.com")
            put("phone", "01700000000")
        }
        put("success_url", "https://example.com/success")
        put("cancel_url", "https://example.com/cancel")
        putJsonArray("line_items") {
            addJsonObject {
                put("quantity", 1)
                putJsonObject("price_data") {
                    put("unit_amount", 10000)
                    putJsonObject("product_data") {
                        put("name", "Demo product")
                    }
                }
            }
        }
    }
)

val checkoutUrl = session["url"].toString().trim('"')
// Open checkoutUrl in Chrome Custom Tabs or WebView
```

## Requirements

- `minSdk` 23 (Android 6.0+)
- Kotlin 1.9+
- Jetpack Compose (for native checkout)

## License

MIT
