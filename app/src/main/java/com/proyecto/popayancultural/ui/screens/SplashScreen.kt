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
    val DeepBlack = Color(0xFF000000)
    val PopayanPurple = Color(0xFFA855F7)

    // ESTADOS PARA EL LOGO
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.6f) }

    // ESTADOS PARA EL TEXTO (Sincronizados para aparecer después)
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(25f) }

    LaunchedEffect(key1 = true) {
        // FASE 1: El Logo aparece primero
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
        }

        // ESPERA TÉCNICA: Justo antes de que el logo termine, arranca el texto
        delay(900)

        // FASE 2: "ARTE Y CULTURA POPAYÁN" aparece con estilo Serif
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(1000))
        }
        launch {
            textOffsetY.animateTo(0f, animationSpec = tween(1000, easing = LinearOutSlowInEasing))
        }

        delay(2800)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // LOGO ANIMADO
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // NOMBRE CON TIPOGRAFÍA SERIFADA (Clásica y Elegante)
            Text(
                text = buildAnnotatedString {
                    // "ARTE Y CULTURA" en Blanco
                    withStyle(style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )) {
                        append("ARTE Y CULTURA ")
                    }
                    // "POPAYÁN" en Púrpura y Bien Grueso
                    withStyle(style = SpanStyle(
                        color = PopayanPurple,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )) {
                        append("POPAYÁN")
                    }
                },
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif, // <--- AQUÍ ESTÁ EL CAMBIO A SERIFADA
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )
        }
    }
}