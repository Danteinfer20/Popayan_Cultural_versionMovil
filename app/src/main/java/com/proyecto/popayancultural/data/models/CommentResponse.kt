package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("status")  val status: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("data")    val data: Comment? = null
)