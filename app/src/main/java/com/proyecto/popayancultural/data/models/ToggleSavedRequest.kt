package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class ToggleSavedRequest(
    @SerializedName("saveable_type") val saveableType: String,
    @SerializedName("saveable_id") val saveableId: Int
)