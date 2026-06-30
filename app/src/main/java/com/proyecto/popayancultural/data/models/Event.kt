package com.proyecto.popayancultural.data.models

import com.google.gson.annotations.SerializedName

data class EventSummary(
    val id: Int = 0,
    val title: String = "",
    val slug: String? = null,
    val description: String? = null,
    @SerializedName("cover_image")      val coverImage: String? = null,
    @SerializedName("start_date")       val startDate: String = "",
    @SerializedName("start_time")       val startTime: String = "",
    @SerializedName("is_free")          val isFree: Boolean = true,
    val price: Double? = null,
    @SerializedName("max_capacity")     val capacity: Int? = null,
    val location: EventLocation? = null,
    @SerializedName("attendance_count") val attendanceCount: Int = 0
)

data class EventDetail(
    val id: Int = 0,
    val title: String = "",
    val slug: String? = null,
    val description: String? = null,
    @SerializedName("cover_image")      val coverImage: String? = null,
    @SerializedName("start_date")       val startDate: String = "",
    @SerializedName("end_date")         val endDate: String? = null,
    @SerializedName("start_time")       val startTime: String = "",
    @SerializedName("end_time")         val endTime: String? = null,
    @SerializedName("is_free")          val isFree: Boolean = true,
    val price: Double? = null,
    @SerializedName("max_capacity")     val capacity: Int? = null,
    @SerializedName("attendance_count") val attendanceCount: Int = 0,
    val location: EventLocation? = null,
    val organizer: OrganizerInfo? = null,
    @SerializedName("user_attendance")  val userAttendance: UserAttendance? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class EventLocation(
    val id: Int? = null,
    val name: String = "",
    val address: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class OrganizerInfo(
    val id: Int = 0,
    val name: String = "",
    val username: String = "",
    @SerializedName("profile_photo")   val profilePhoto: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val role: String? = null
)

data class UserAttendance(
    val id: Int = 0,
    val status: String = "",
    @SerializedName("qr_code")     val qrCode: String? = null,
    @SerializedName("ticket_code") val ticketCode: String? = null
)

data class AttendResponse(
    val status: String = "",
    val message: String = "",
    val data: UserAttendance? = null,
    val attendance: UserAttendance? = null
) {
    fun ticket(): UserAttendance? = data ?: attendance
}

// ✅ EventDetailResponse vive en EventResponse.kt — eliminada de aquí para evitar Redeclaration

data class MyTicket(
    val id: Int = 0,
    val event: EventSummary = EventSummary(),
    val status: String = "",
    @SerializedName("qr_code")     val qrCode: String? = null,
    @SerializedName("ticket_code") val ticketCode: String? = null
)