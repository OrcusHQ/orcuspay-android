# Orcuspay Android SDK

Kotlin Android client for Orcuspay.

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
```

Open `checkoutUrl` in Chrome Custom Tabs or your preferred browser flow.
