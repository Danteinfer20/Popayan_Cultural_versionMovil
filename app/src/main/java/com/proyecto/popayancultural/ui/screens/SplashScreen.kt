package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.popayancultural.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val BgColor = Color(0xFF050505) // Negro premium con profundidad óptica
    val PopayanPurple = Color(0xFFA855F7)

    // Transición de salida global (Fade Out)
    val masterAlpha = remember { Animatable(1f) }

    // Estados para el Logo (Física de resorte natural)
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.4f) }

    // Estados para el Texto (Desplazamiento e interpolación)
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(30f) }

    LaunchedEffect(key1 = true) {
        // FASE 1: El Logo emerge con un rebote de resorte (Spring)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, // Rebote fluido orgánico
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // ESPERA TÉCNICA SINCRONIZADA: Revelado del texto antes del asentamiento total
        delay(750)

        // FASE 2: Entrada del texto en tipografía Serifada
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = LinearEasing)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // FASE 3: Sostenido cinemático en pantalla
        delay(2500)

        // FASE 4: Fundido a negro (Fade Out) controlado antes del desmonte
        masterAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(450, easing = FastOutLinearInEasing)
        )

        // Ejecución de la navegación nativa
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(masterAlpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PopayanPurple.copy(alpha = 0.08f), // Aura lumínica detrás del logo
                        BgColor,
                        BgColor
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // LOGO ANIMADO CON PROPORCIONES OPTIMIZADAS
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(190.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // NOMBRE CON TIPOGRAFÍA SERIFADA Y ESPACIADO EQUILIBRADO
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )) {
                        append("ARTE Y CULTURA ")
                    }
                    withStyle(style = SpanStyle(
                        color = PopayanPurple,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )) {
                        append("POPAYÁN")
                    }
                },
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )
        }
    }
}