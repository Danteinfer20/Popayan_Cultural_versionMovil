package com.proyecto.popayancultural.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Events : BottomNavItem("events", Icons.Default.CalendarMonth, "Agenda") // Nuestra 5ta pantalla
    object Gallery : BottomNavItem("gallery", Icons.Default.AutoAwesomeMosaic, "Galería")
    object Store : BottomNavItem("store", Icons.Default.Storefront, "Tienda")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Perfil")
}