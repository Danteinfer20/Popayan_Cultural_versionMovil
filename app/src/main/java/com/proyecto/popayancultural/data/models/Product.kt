package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

/**
 * Campos mapeados contra ProductResource.php:
 *   name        → getLocalizedField() → String directo (ya viene parseado)
 *   description → getLocalizedField() → String directo (ya viene parseado)
 *   stock       → stock_quantity en BD  (el resource lo renombra a "stock")
 *   type        → product_type en BD   (el resource lo renombra a "type")
 *   images      → array de URLs        (el resource lo llama "images")
 *   author      → el resource usa "author", no "user"
 *   specs       → objeto con materials, dimensions, weight
 */
data class Product(
    val id: Int = 0,

    // El resource ya resuelve el JSON localizado — llega como String plano
    val name: String = "",
    val description: String = "",

    val price: Double = 0.0,
    @SerializedName("sale_price") val salePrice: Double? = null,

    // Resource devuelve "stock", no "stock_quantity"
    @SerializedName("stock") val stockQuantity: Int = 0,

    // Resource devuelve "type", no "product_type"
    @SerializedName("type") val productType: String = "physical",

    @SerializedName("main_image") val mainImage: String? = null,

    // Resource devuelve "images" (array de URLs ya resueltas)
    val images: List<String> = emptyList(),

    val status: String = "",
    @SerializedName("is_featured") val isFeatured: Boolean = false,

    // Resource devuelve "author", no "user"
    val author: ProductSeller? = null,

    val category: ProductCategory? = null,

    // Resource devuelve "specs" como objeto anidado
    val specs: ProductSpecs? = null,

    val stats: ProductStats? = null,

    @SerializedName("user_interaction") val userInteraction: ProductUserInteraction? = null
) {
    val imageUrl: String get() {
        val first = images.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: mainImage?.takeIf { it.isNotBlank() }
            ?: return ""
        return if (first.startsWith("http")) first
        else "https://vivelarte.com/storage/$first"
    }

    val isAvailable: Boolean get() = status == "available" && stockQuantity > 0

    val displayPrice: String get() {
        return try {
            "$ ${String.format("%,.0f", price)}"
        } catch (e: Exception) { "$price" }
    }
}

data class ProductSeller(
    val id: Int = 0,
    val name: String = "",
    val username: String = "",
    @SerializedName("profile_picture") val avatar: String? = null,
    val phone: String? = null
)

data class ProductCategory(
    val id: Int = 0,
    val name: String = ""
)

data class ProductSpecs(
    val materials: String? = null,
    val dimensions: String? = null,
    val weight: String? = null
)

data class ProductStats(
    val sales: Int = 0
)

data class ProductUserInteraction(
    @SerializedName("is_saved") val isSaved: Boolean = false
)

// CartItem — local, sin backend por ahora
data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val subtotal: Double get() = product.price * quantity
}