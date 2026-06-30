package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
//  AUTENTICACIÓN
// ─────────────────────────────────────────────────────────────

data class AuthResponse(
    val status : String,
    val user   : User?,
    val token  : String?,
    @SerializedName("message") val message: String? = null
)

data class User(
    val id           : Int,
    val name         : String?,
    val email        : String?,
    val username     : String?,
    val user_type    : String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    @SerializedName("is_verified")     val isVerified    : Boolean
)

// ─────────────────────────────────────────────────────────────
//  PERFIL COMPLETO
// ─────────────────────────────────────────────────────────────

data class ProfileResponse(
    val status : String,
    val message: String?   = null,
    val user   : UserFull? = null
)

data class UserFull(
    val id           : Int,
    val name         : String,
    val username     : String,
    val email        : String,
    val phone        : String?        = null,
    val city         : String?        = null,
    val neighborhood : String?        = null,
    val bio          : String?        = null,
    val website      : String?        = null,
    @SerializedName("profile_picture") val profilePicture: String?         = null,
    @SerializedName("cover_picture")   val coverPicture  : String?         = null,
    @SerializedName("user_type")       val userType      : String          = "visitor",
    @SerializedName("is_verified")     val isVerified    : Boolean         = false,
    @SerializedName("social_media")    val socialMedia   : SocialMediaDto? = null,
    val settings     : UserSettingsDto? = null
)

data class SocialMediaDto(
    val instagram: String? = null,
    val facebook : String? = null,
    val whatsapp : String? = null,
    val twitter  : String? = null
)

// ── UserSettingsDto actualizado con language + visualMode ────
data class UserSettingsDto(
    @SerializedName("public_profile")       val publicProfile      : Boolean = true,
    @SerializedName("email_notifications")  val emailNotifications : Boolean = true,
    @SerializedName("nearby_events_notify") val nearbyEventsNotify : Boolean = true,
    @SerializedName("language")             val language           : String  = "es",
    @SerializedName("visual_mode")          val visualMode         : String  = "dark"
)

// ─────────────────────────────────────────────────────────────
//  RESPUESTA GENÉRICA
// ─────────────────────────────────────────────────────────────

data class GenericResponse(
    val status : String,
    val message: String? = null
)

// NOTA: UpdateSettingsRequest fue movido a UpdateSettingsRequest.kt