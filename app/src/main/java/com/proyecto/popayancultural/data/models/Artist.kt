package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class Artist(
    val id       : Int    = 0,
    val name     : String = "",
    val username : String = "",

    @SerializedName("profile_picture") val avatar        : String? = null,
    @SerializedName("cover_picture")   val coverPicture  : String? = null,
    @SerializedName("is_verified")     val isVerified    : Boolean = false,

    val bio     : String? = null,
    val website : String? = null,

    @SerializedName("city")         val location     : String? = null,
    @SerializedName("neighborhood") val neighborhood : String? = null,
    @SerializedName("user_type")    val userType     : String  = "",

    // Redes sociales — objeto JSON: { "instagram": "url", "facebook": "url", ... }
    @SerializedName("social_media") val socialMedia : Map<String, String>? = null,

    // Conteos de comunidad
    @SerializedName("follower_count")     val followerCount   : Int     = 0,
    @SerializedName("following_count")    val followingCount  : Int     = 0,
    @SerializedName("is_following_by_me") val isFollowingByMe : Boolean = false
) {
    val imageUrl: String get() = avatar ?: ""

    // Ubicación legible: "Barrio, Ciudad" o solo ciudad
    val displayLocation: String get() = when {
        !neighborhood.isNullOrBlank() && !location.isNullOrBlank() -> "$neighborhood, $location"
        !location.isNullOrBlank() -> location
        else -> ""
    }

    // Label legible del rol
    val roleLabel: String get() = when (userType) {
        "cultural_manager" -> "Gestor Cultural"
        "educator"         -> "Educador"
        "artisan"          -> "Maestro Artesano"
        "artist"           -> "Artista"
        else               -> "Creador"
    }
}

data class ArtistResponse(
    val status : String       = "",
    val data   : List<Artist> = emptyList()
)

data class ArtistDetailResponse(
    val status : String  = "",
    val data   : Artist? = null
)