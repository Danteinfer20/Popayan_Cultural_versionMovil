package com.proyecto.popayancultural.data.models

/**
 * Este archivo actúa como contenedor para la respuesta de asistencia.
 * Asegura que el JSON de Laravel con llave "attendance" se mapee correctamente.
 */
data class AttendanceResponse(
    val attendance: UserAttendance?
)