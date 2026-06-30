package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

/**
 * Request para POST api/v1/reactions/toggle
 *
 * El backend acepta DOS formas de identificar el modelo:
 *   - Forma A (post directo):  { post_id: Int, reaction_type: String }
 *   - Forma B (genérica):      { id: Int, type: "post"|"event"|"product", reaction_type: String }
 *
 * Usamos Forma A para posts (más directa y sin ambigüedad).
 * reaction_type acepta: "like" | "love" | "inspire" | "interest"
 */
data class ToggleReactionRequest(
    @SerializedName("post_id")       val postId: Int,
    @SerializedName("reaction_type") val reactionType: String = "like"
)