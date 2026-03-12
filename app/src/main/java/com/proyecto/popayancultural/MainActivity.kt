package com.proyecto.popayancultural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.proyecto.popayancultural.ui.theme.PopayanCulturalTheme
import com.proyecto.popayancultural.ui.theme.BackgroundDeep
import com.proyecto.popayancultural.ui.screens.SplashScreen

// ¡Paso clave! Importamos MainScreen en lugar de HomeScreen
import com.proyecto.popayancultural.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita que el diseño use toda la pantalla (incluyendo barras)
        enableEdgeToEdge()

        setContent {
            PopayanCulturalTheme {
                // Surface garantiza que el fondo sea SIEMPRE #0A0A0C
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDeep
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    // Animación de transición entre pantallas
                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(800)) togetherWith
                                    fadeOut(animationSpec = tween(800))
                        },
                        label = "ScreenTransition"
                    ) { targetShowSplash ->
                        if (targetShowSplash) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            // ¡AQUÍ ESTÁ LA SOLUCIÓN!
                            // Cuando termina el Splash, cargamos el MainScreen (que ya tiene la barra y tu contenido)
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}