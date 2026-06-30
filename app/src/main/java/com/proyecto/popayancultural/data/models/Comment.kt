package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id")          val id: Int = 0,
    @SerializedName("content")     val content: String? = "",
    @SerializedName("status")      val status: String? = null,
    @SerializedName("created_at")  val createdAt: String? = "",
    @SerializedName("human_time")  val humanTime: String? = null,
    @SerializedName("user")        val user: CommentUser? = null
)

data class CommentUser(
    @SerializedName("id")              val id: Int = 0,
    @SerializedName("name")            val name: String? = "",
    @SerializedName("username")        val username: String? = "",
    @SerializedName("avatar")          val avatar: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null
)