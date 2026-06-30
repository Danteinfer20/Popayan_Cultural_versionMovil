package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.AuthViewModel

// Variables matemáticas para estética Dark Premium
private val AbsoluteBlack = Color(0xFF050505)
private val SurfaceDark = Color(0xFF0A0A0C)
private val VioletAcentoV3 = Color(0xFFA855F7)
private val TextSecondaryV3 = Color(0xFF888888)
private val ErrorRed = Color(0xFFCF6679)
private val ErrorBackground = Color(0xFF1E0A0D)
private val SuccessGreen = Color(0xFF10B981)
private val SuccessBackground = Color(0xFF05170E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val rawError by viewModel.error.collectAsState()
    val recoverySuccess by viewModel.recoverySuccess.collectAsState() // Escucha el estado de éxito
    val focusManager = LocalFocusManager.current

    // Limpiar el estado de éxito al desmontar la vista para evitar estados fantasma
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetRecoveryState()
            viewModel.clearError()
        }
    }

    // Interceptor UI: Purifica errores del backend
    val displayError = remember(rawError) {
        when {
            rawError == null -> null
            rawError!!.contains("JsonReader") || rawError!!.contains("<html") -> "Error de conexión con el servidor."
            else -> rawError
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera: Full-Bleed Fading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AsyncImage(
                model = "https://inspiringcolombia.travel/wp-content/uploads/2025/03/Popayan-Centro-Historico.webp",
                contentDescription = "Popayán",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, AbsoluteBlack),
                            startY = 50f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-20).dp)
        ) {
            // Títulos
            Text(
                text = "SEGURIDAD",
                color = VioletAcentoV3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Recuperar clave.",
                color = Color.White,
                fontSize = 40.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto explicativo
            Text(
                text = "Ingresa el correo electrónico asociado a tu cuenta. Te enviaremos un enlace seguro para restablecer tu contraseña.",
                color = TextSecondaryV3,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Cápsula de Error General
            AnimatedVisibility(
                visible = displayError != null && !recoverySuccess,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(ErrorBackground, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayError ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Cápsula de Éxito
            AnimatedVisibility(
                visible = recoverySuccess,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(SuccessBackground, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Éxito",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enlace enviado. Revisa tu bandeja de entrada.",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Campo: Correo Electrónico
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                    viewModel.resetRecoveryState() // Limpiar éxito si vuelve a escribir
                },
                placeholder = { Text("Correo electrónico", color = TextSecondaryV3) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = textFieldColorsV3(isError = displayError != null),
                singleLine = true,
                isError = displayError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank()) {
                            viewModel.recoverPassword(email)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón: Enviar Enlace
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.recoverPassword(email)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletAcentoV3,
                    disabledContainerColor = VioletAcentoV3.copy(alpha = 0.5f)
                ),
                enabled = !isLoading && email.isNotBlank() && !recoverySuccess // Se deshabilita tras éxito
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = if (recoverySuccess) "ENVIADO" else "ENVIAR ENLACE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enlace: Volver al Login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateBack() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← VOLVER AL INICIO DE SESIÓN",
                    color = TextSecondaryV3,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun textFieldColorsV3(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark,
    errorContainerColor = SurfaceDark,
    focusedBorderColor = VioletAcentoV3,
    unfocusedBorderColor = if (isError) ErrorRed else Color.Transparent,
    errorBorderColor = ErrorRed,
    cursorColor = VioletAcentoV3,
    errorCursorColor = ErrorRed,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorTextColor = ErrorRed
)