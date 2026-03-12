package com.proyecto.popayancultural.ui.screens

data class CulturalItem(
    val id: Int,
    val titulo: String,
    val info: String,
    val imagenUrl: String,
    val descripcion: String,
    val categoria: String
) // <--- ESTA LLAVE ES LA QUE TE FALTABA