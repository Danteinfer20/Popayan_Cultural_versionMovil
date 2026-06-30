

package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class UpdateSettingsRequest(
    @SerializedName("public_profile")
    val publicProfile      : Boolean = true,

    @SerializedName("email_notifications")
    val emailNotifications : Boolean = true,

    @SerializedName("nearby_events_notify")
    val nearbyEventsNotify : Boolean = true,

    @SerializedName("language")
    val language           : String  = "es",

    @SerializedName("visual_mode")
    val visualMode         : String  = "dark"
)