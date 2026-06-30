package com.proyecto.popayancultural.data.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,  // ✅ Campo requerido por el backend
    val user_type: String = "visitor"
)