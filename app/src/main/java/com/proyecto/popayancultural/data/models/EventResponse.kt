package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

// ─── Paginación Laravel ────────────────────────────────────────────────────
data class Links(
    val first: String? = null,
    val last:  String? = null,
    val prev:  String? = null,
    val next:  String? = null
)

data class Meta(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page")    val lastPage:    Int? = null,
    @SerializedName("per_page")     val perPage:     Int? = null,
    val total: Int? = null,
    val from:  Int? = null,
    val to:    Int? = null
)

// ─── Respuestas ────────────────────────────────────────────────────────────
data class EventResponse(
    val data:  List<EventSummary> = emptyList(),
    val links: Links? = null,
    val meta:  Meta?  = null
)

data class EventDetailResponse(
    val data: EventDetail? = null
)