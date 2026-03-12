package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proyecto.popayancultural.navigation.BottomNavItem
import com.proyecto.popayancultural.ui.theme.*

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

    Scaffold(
        containerColor = BackgroundDeep, // #0A0A0C
        bottomBar = {
            PopayanBottomBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. INICIO
            composable(BottomNavItem.Home.route) { HomeScreen() }

            // 2. AGENDA CULTURAL
            composable(BottomNavItem.Events.route) { EventsScreen() }

            // 3. GALERÍA
            composable(BottomNavItem.Gallery.route) { GalleryScreen() }

            // 4. TIENDA
            composable(BottomNavItem.Store.route) { StoreScreen() }

            // 5. PERFIL (Ahora llama automáticamente a tu archivo ProfileScreen.kt real)
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun PopayanBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Events,
        BottomNavItem.Gallery,
        BottomNavItem.Store,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = CardBackground, // #111113
        shadowElevation = 24.dp,
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = isSelected,
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VioletAcento,
                        selectedTextColor = VioletAcento,
                        unselectedIconColor = Color.Gray.copy(alpha = 0.5f),
                        unselectedTextColor = Color.Gray.copy(alpha = 0.5f),
                        indicatorColor = Color.Transparent // Elimina la píldora de Material 3
                    ),
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}