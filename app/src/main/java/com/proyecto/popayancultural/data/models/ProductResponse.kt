package com.proyecto.popayancultural.data.models

// Links y Meta viven en EventResponse.kt (mismo package, no necesitan import)

data class ProductResponse(
    val data: List<Product> = emptyList(),
    val links: Links? = null,
    val meta: Meta? = null
)