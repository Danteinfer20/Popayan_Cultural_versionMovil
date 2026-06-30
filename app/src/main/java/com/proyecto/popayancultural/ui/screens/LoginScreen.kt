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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.AuthRole
import com.proyecto.popayancultural.ui.AuthViewModel

// ─── Design Tokens (sin cambio) ──────────────────────────────
private val AbsoluteBlack    = Color(0xFF050505)
private val SurfaceDark      = Color(0xFF0A0A0C)
private val VioletAcentoV3   = Color(0xFFA855F7)
private val TextSecondaryV3  = Color(0xFF888888)
private val ErrorRed         = Color(0xFFCF6679)
private val ErrorBackground  = Color(0xFF1E0A0D)
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    // ── CAMBIO CLAVE: el callback ahora entrega el rol ────────
    // El NavGraph usa AuthRole para decidir la ruta de destino,
    // sin necesidad de re-leer TokenManager ni SharedPreferences.
    onLoginSuccess     : (role: AuthRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToRecover : () -> Unit,
    viewModel          : AuthViewModel = viewModel()
) {
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val isLoading       by viewModel.isLoading.collectAsState()
    val rawError        by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val effectiveRole   by viewModel.effectiveRole.collectAsState()
    val focusManager    = LocalFocusManager.current

    // Purifica errores de HTML/Ignition del backend
    val displayError = remember(rawError) {
        when {
            rawError == null                                                    -> null
            rawError!!.contains("JsonReader") || rawError!!.contains("<html") ->
                "Fallo de conexión o credenciales inválidas."
            else -> rawError
        }
    }

    // ── Interceptor de navegación post-login ─────────────────
    // Solo dispara cuando isAuthenticated = true Y ya tenemos
    // el rol calculado (effectiveRole != null).
    LaunchedEffect(isAuthenticated, effectiveRole) {
        if (isAuthenticated && effectiveRole != null) {
            onLoginSuccess(effectiveRole!!)
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
        // ── Cabecera: Full-Bleed Fading ───────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model          = "https://inspiringcolombia.travel/wp-content/uploads/2025/03/Popayan-Centro-Historico.webp",
                contentDescription = "Popayán Noche",
                contentScale   = ContentScale.Crop,
                modifier       = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors  = listOf(Color.Transparent, AbsoluteBlack),
                            startY  = 100f
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
            // ── Títulos ───────────────────────────────────────
            Text(
                text          = "PORTAL OFICIAL",
                color         = VioletAcentoV3,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text       = "Inicia sesión.",
                color      = Color.White,
                fontSize   = 40.sp,
                fontStyle  = FontStyle.Italic,
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Error ─────────────────────────────────────────
            AnimatedVisibility(visible = displayError != null, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(ErrorBackground, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = displayError ?: "", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // ── Campo email ───────────────────────────────────
            OutlinedTextField(
                value          = email,
                onValueChange  = { email = it; viewModel.clearError() },
                placeholder    = { Text("Correo Electrónico", color = TextSecondaryV3) },
                modifier       = Modifier.fillMaxWidth(),
                shape          = CircleShape,
                colors         = textFieldColorsV3(isError = displayError != null),
                singleLine     = true,
                isError        = displayError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo contraseña ──────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; viewModel.clearError() },
                placeholder   = { Text("Contraseña", color = TextSecondaryV3) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = CircleShape,
                colors        = textFieldColorsV3(isError = displayError != null),
                isError       = displayError != null,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    TextButton(
                        onClick         = { showPassword = !showPassword },
                        contentPadding  = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            text          = if (showPassword) "OCULTAR" else "VER",
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = if (displayError != null) ErrorRed else TextSecondaryV3,
                            letterSpacing = 1.sp
                        )
                    }
                },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(email, password)
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── ¿Olvidaste tu contraseña? ─────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text          = "¿OLVIDASTE TU CONTRASEÑA?",
                    color         = VioletAcentoV3,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier      = Modifier.clickable { onNavigateToRecover() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botón ingresar ────────────────────────────────
            Button(
                onClick   = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                },
                modifier  = Modifier.fillMaxWidth().height(56.dp),
                shape     = CircleShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation  = 8.dp,
                    pressedElevation  = 2.dp,
                    disabledElevation = 0.dp
                ),
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = VioletAcentoV3,
                    disabledContainerColor = VioletAcentoV3.copy(alpha = 0.5f)
                ),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text(text = "INGRESAR", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Link a registro ───────────────────────────────
            Text(
                text          = "CREAR UNA CUENTA NUEVA",
                color         = TextSecondaryV3,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier      = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onNavigateToRegister() }
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun textFieldColorsV3(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = SurfaceDark,
    unfocusedContainerColor = SurfaceDark,
    errorContainerColor     = SurfaceDark,
    focusedBorderColor      = VioletAcentoV3,
    unfocusedBorderColor    = if (isError) ErrorRed else Color.Transparent,
    errorBorderColor        = ErrorRed,
    cursorColor             = VioletAcentoV3,
    errorCursorColor        = ErrorRed,
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    errorTextColor          = ErrorRed
)