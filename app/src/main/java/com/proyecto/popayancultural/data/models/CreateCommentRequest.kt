package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class CreateCommentRequest(
    @SerializedName("post_id") val postId: Int,
    @SerializedName("content") val content: String
)