package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
//  GET /api/v1/artist/dashboard
// ─────────────────────────────────────────────────────────────

data class ArtistDashboardResponse(
    val status : String               = "",
    val data   : ArtistDashboardData? = null
)

data class ArtistDashboardData(
    val kpis        : ArtistKpisRemote       = ArtistKpisRemote(),
    @SerializedName("recent_works")
    val recentWorks : List<RecentWorkRemote> = emptyList()
)

data class ArtistKpisRemote(
    @SerializedName("total_works") val totalWorks : Int    = 0,
    val followers : Int    = 0,
    val sales     : Int    = 0,
    val revenue   : Double = 0.0
)

data class RecentWorkRemote(
    val id        : Int    = 0,
    val title     : String = "",
    val status    : String = "",
    @SerializedName("main_image") val mainImage : String = "",
    @SerializedName("created_at") val createdAt : String = ""
)

// ─────────────────────────────────────────────────────────────
//  GET /api/v1/my-sales
// ─────────────────────────────────────────────────────────────

data class MySalesResponse(
    val status : String     = "",
    val data   : SalesData? = null
)

data class SalesData(
    val kpis   : SalesKpis   = SalesKpis(),
    val orders : List<Order> = emptyList()
)

data class SalesKpis(
    @SerializedName("pending_capital")  val pendingCapital : Double = 0.0,
    @SerializedName("secured_capital")  val securedCapital : Double = 0.0,
    @SerializedName("total_orders")     val totalOrders    : Int    = 0
)