package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    val status: String,
    val message: String,
    val data: List<Category>
)

data class Category(
    val id: Int,
    val name: String?,
    val slug: String?,
    @SerializedName("category_type") val categoryType: String?,
    val color: String?,
    @SerializedName("is_active") val isActive: Boolean
)