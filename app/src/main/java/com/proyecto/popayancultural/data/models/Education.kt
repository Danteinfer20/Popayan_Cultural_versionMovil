package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class Education(
    val id: Int = 0,
    val title: String = "",
    val excerpt: String? = null,
    val content: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    val metadata: EducationMetadata? = null,
    val author: Author? = null,
    val stats: Stats? = null
) {
    val imageUrl: String? get() = coverImage
    val durationLabel: String get() = "${metadata?.estimatedReadTime ?: 5} min"
    val levelLabel: String
        get() = when (metadata?.difficultyLevel?.lowercase()) {
            "beginner"     -> "Básico"
            "intermediate" -> "Intermedio"
            "advanced"     -> "Avanzado"
            else           -> "Básico"
        }
}

data class EducationMetadata(
    @SerializedName("difficulty_level")    val difficultyLevel: String? = null,
    @SerializedName("estimated_read_time") val estimatedReadTime: Int? = null,
    @SerializedName("knowledge_area")      val knowledgeArea: String? = null,
    @SerializedName("historical_period")   val historicalPeriod: String? = null,
    @SerializedName("category_name")       val categoryName: String? = null
)

data class Author(
    val name: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class Stats(
    val reactions: Int? = null,
    val comments: Int? = null
)

data class EducationResponse(
    val status: String = "",
    val data: List<Education> = emptyList()
)

data class EducationDetailResponse(
    val status: String = "",
    val data: Education? = null
)