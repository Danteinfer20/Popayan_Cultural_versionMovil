package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

// ─── Response wrapper ─────────────────────────────────────────────────────────

data class SavedItemsResponse(
    val status: String = "",
    val data: List<SavedItem> = emptyList()
)

// ─── SavedItem ────────────────────────────────────────────────────────────────

data class SavedItem(
    val id: Int = 0,
    val category: String = "",
    @SerializedName("savable_id")   val savableId: Int = 0,
    @SerializedName("savable_type") val savableType: String = "",
    @SerializedName("post_id")      val postId: Int? = null,
    @SerializedName("created_at")   val createdAt: String = "",

    // Campos raíz — útiles para listas rápidas
    val title: String = "",
    @SerializedName("cover_image")  val coverImage: String? = null,
    val author: SavedItemAuthor? = null,

    // Objeto savable completo
    val savable: SavedItemObra? = null
)

// ─── Savable (obra/post anidado) ──────────────────────────────────────────────

data class SavedItemObra(
    val id: Int = 0,
    val title: String = "",
    val user: SavedItemAuthor? = null,
    val media: List<SavedItemMedia> = emptyList()
) {
    // La UI accede a imageUrl y author directamente
    val imageUrl: String         get() = media.firstOrNull()?.url ?: ""
    val author: SavedItemAuthor? get() = user
}

// ─── Autor ────────────────────────────────────────────────────────────────────

data class SavedItemAuthor(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("profile_picture") val profilePicture: String? = null
)

// ─── Media ────────────────────────────────────────────────────────────────────

data class SavedItemMedia(
    val url: String = ""
)