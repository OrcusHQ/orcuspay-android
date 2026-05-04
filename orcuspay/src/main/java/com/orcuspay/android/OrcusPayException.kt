package com.orcuspay.android

import kotlinx.serialization.json.JsonObject

class OrcusPayException(
    message: String,
    val statusCode: Int,
    val response: JsonObject,
) : RuntimeException(message)
