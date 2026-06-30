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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("visitor") }
    var expanded by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val rawError by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val focusManager = LocalFocusManager.current

    // Interceptor UI: Purifica errores del backend
    val displayError = remember(rawError) {
        when {
            rawError == null -> null
            rawError!!.contains("JsonReader") || rawError!!.contains("<html") -> "Error de conexión con el servidor."
            else -> rawError
        }
    }

    val passwordMismatch = passwordConfirmation.isNotEmpty() && password != passwordConfirmation

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onRegisterSuccess()
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
                text = "ÚNETE A LA COMUNIDAD",
                color = VioletAcentoV3,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Regístrate.",
                color = Color.White,
                fontSize = 40.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Cápsula de Error General
            AnimatedVisibility(
                visible = displayError != null,
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

            // Campo: Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; viewModel.clearError() },
                placeholder = { Text("Nombre completo", color = TextSecondaryV3) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = textFieldColorsV3(isError = false),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                placeholder = { Text("Correo electrónico", color = TextSecondaryV3) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = textFieldColorsV3(isError = false),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                placeholder = { Text("Contraseña", color = TextSecondaryV3) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = textFieldColorsV3(isError = false),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Confirmar Contraseña (con validación reactiva)
            OutlinedTextField(
                value = passwordConfirmation,
                onValueChange = { passwordConfirmation = it; viewModel.clearError() },
                placeholder = { Text("Confirmar contraseña", color = TextSecondaryV3) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = textFieldColorsV3(isError = passwordMismatch),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = passwordMismatch,
                supportingText = {
                    if (passwordMismatch) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Error",
                                tint = ErrorRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Las contraseñas no coinciden",
                                color = ErrorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (!passwordMismatch) {
                            viewModel.register(name, email, password, passwordConfirmation, userType)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(if (passwordMismatch) 8.dp else 16.dp))

            // Selector de Rol Estilizado
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when (userType) {
                        "artist" -> "Artista"
                        "educator" -> "Educador"
                        else -> "Espectador / Turista"
                    },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = CircleShape,
                    colors = textFieldColorsV3(isError = false),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    listOf(
                        "visitor" to "Espectador / Turista",
                        "artist" to "Artista",
                        "educator" to "Educador"
                    ).forEach { (value, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                userType = value
                                expanded = false
                                focusManager.clearFocus()
                            },
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Crear Cuenta
            Button(
                onClick = {
                    viewModel.register(name, email, password, passwordConfirmation, userType)
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
                enabled = !isLoading &&
                        name.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        !passwordMismatch
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "CREAR CUENTA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enlace de regreso al Login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    color = TextSecondaryV3,
                    fontSize = 12.sp
                )
                Text(
                    text = "INICIAR SESIÓN",
                    color = VioletAcentoV3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
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