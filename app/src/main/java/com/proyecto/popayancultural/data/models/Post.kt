package com.proyecto.popayancultural.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// ─── Deserializador para campos multilingüe {"es": "valor"} o "valor" ────────
class MultilingualStringDeserializer : JsonDeserializer<String> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): String {
        return try {
            if (json.isJsonObject) {
                // Formato {"es": "Mi obra"} → extrae "es", o la primera clave disponible
                val obj = json.asJsonObject
                obj.get("es")?.asString
                    ?: obj.entrySet().firstOrNull()?.value?.asString
                    ?: ""
            } else {
                json.asString ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────

data class Post(
    val id      : Int    = 0,

    // title, excerpt y content pueden venir como {"es": "..."} o como String plano
    @JsonAdapter(MultilingualStringDeserializer::class)
    val title   : String  = "",

    val slug    : String? = null,   // nullable — a veces el backend no lo devuelve

    @JsonAdapter(MultilingualStringDeserializer::class)
    val excerpt : String  = "",

    @JsonAdapter(MultilingualStringDeserializer::class)
    val content : String  = "",

    val status  : String  = "",

    @SerializedName("published_at") val publishedAt : String? = null,
    @SerializedName("is_featured")  val isFeatured  : Boolean = false,

    val image   : String?       = null,
    val images  : List<String>  = emptyList(),

    val author   : AuthorInfo?   = null,
    val category : CategoryInfo? = null,
    val stats    : PostStats?    = null,

    @SerializedName("user_interaction") val userInteraction : UserInteraction? = null,
    val comments : List<Comment>? = null
) {
    val imageUrl: String
        get() {
            val rawPath = image ?: images.firstOrNull() ?: ""
            if (rawPath.isBlank()) return ""
            if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) return rawPath
            return "https://vivelarte.com/storage/$rawPath"
        }

    // Helper para usar en navegación — evita NPE si slug es null
    val safeSlug: String get() = slug ?: id.toString()
}

data class AuthorInfo(
    val name     : String  = "",
    val username : String  = "",
    val avatar   : String? = null
)

data class CategoryInfo(
    val id   : Int    = 0,
    val name : String = ""
)

data class PostStats(
    val views     : Int = 0,
    val shares    : Int = 0,
    val reactions : Int = 0
)

data class UserInteraction(
    @SerializedName("is_saved")    val isSaved    : Boolean = false,
    @SerializedName("has_reacted") val hasReacted : Boolean = false
)

// Wrapper para GET /posts (paginado por Laravel Resource)
data class PostResponse(
    val data : List<Post> = emptyList()
)

// Wrapper para GET /posts/{identifier}
data class PostDetailResponse(
    val status : String = "",
    val data   : Post?  = null
)

// ── Content Types ─────────────────────────────────────────────
data class ContentTypeResponse(
    val status  : String           = "",
    val data    : List<ContentType> = emptyList()
)

data class ContentType(
    val id   : Int    = 0,
    val name : String = ""
)