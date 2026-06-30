package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

// ─── Requests ─────────────────────────────────────────────────────────────────

data class CreateOrderRequest(
    @SerializedName("items") val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("id")       val id: Int,
    @SerializedName("cantidad") val cantidad: Int
)

// ─── Responses ────────────────────────────────────────────────────────────────

data class OrderResponse(
    val status: String = "",
    val message: String = "",
    val data: Order? = null
)

data class OrdersListResponse(
    val status: String = "",
    val data: List<Order> = emptyList()
)

// ─── Order ────────────────────────────────────────────────────────────────────

data class Order(
    val id: Int = 0,
    val uuid: String = "",
    @SerializedName("order_number")  val orderNumber: String = "",
    @SerializedName("total_amount")  val totalAmount: Double = 0.0,
    val status: String = "",
    @SerializedName("order_type")    val orderType: String = "product",
    @SerializedName("contact_phone") val contactPhone: String? = null,
    @SerializedName("order_items")   val orderItems: List<OrderItem> = emptyList(),
    val items: List<OrderItemFlat>   = emptyList(),
    @SerializedName("created_at")    val createdAt: String = "",
    val date: String = ""
) {
    val statusLabel: String get() = when (status) {
        "pending"   -> "Pendiente de pago"
        "confirmed" -> "Confirmado"
        "delivered" -> "Entregado"
        "cancelled" -> "Cancelado"
        else        -> status
    }

    // Normaliza ambos formatos para que la UI siempre use lo mismo
    val displayItems: List<OrderItemDisplay> get() {
        return if (orderItems.isNotEmpty()) {
            orderItems.map {
                OrderItemDisplay(
                    name     = it.product?.name ?: "Producto",
                    quantity = it.quantity,
                    subtotal = it.subtotal
                )
            }
        } else {
            items.map {
                OrderItemDisplay(
                    name     = it.parsedProductName,
                    quantity = it.quantity,
                    subtotal = it.subtotal
                )
            }
        }
    }
}

// ─── OrderItem (usado por OrderResource / mySales) ────────────────────────────

data class OrderItem(
    val id: Int = 0,
    @SerializedName("product_id") val productId: Int = 0,
    val quantity: Int = 0,
    @SerializedName("unit_price") val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0,
    val product: Product? = null
)

// ─── OrderItemFlat (usado por myOrders) ───────────────────────────────────────

data class OrderItemFlat(
    @SerializedName("product_name") val productName: String = "",
    val quantity: Int = 0,
    @SerializedName("unit_price")   val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0
) {
    // El backend manda {"es": "Nombre"} como string — lo parseamos a mano
    val parsedProductName: String get() = try {
        productName
            .trim()
            .removePrefix("{")
            .removeSuffix("}")
            .split(":")
            .last()
            .trim()
            .removeSurrounding("\"")
    } catch (e: Exception) {
        productName
    }
}

// ─── DTO limpio para la UI ────────────────────────────────────────────────────

data class OrderItemDisplay(
    val name: String,
    val quantity: Int,
    val subtotal: Double
)