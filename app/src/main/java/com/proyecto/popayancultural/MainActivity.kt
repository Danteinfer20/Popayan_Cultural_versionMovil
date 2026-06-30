package com.proyecto.popayancultural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.ui.AuthRole
import com.proyecto.popayancultural.ui.AuthViewModel
import com.proyecto.popayancultural.ui.screens.LoginScreen
import com.proyecto.popayancultural.ui.screens.MainScreen
import com.proyecto.popayancultural.ui.screens.RecoverPasswordScreen
import com.proyecto.popayancultural.ui.screens.RegisterScreen
import com.proyecto.popayancultural.ui.screens.SplashScreen
import com.proyecto.popayancultural.ui.theme.BackgroundDeep
import com.proyecto.popayancultural.ui.theme.PopayanCulturalTheme

enum class AppState { SPLASH, LOGIN, REGISTER, RECOVER, MAIN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RetrofitClient.initialize(applicationContext)

        setContent {
            PopayanCulturalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundDeep
                ) {
                    var currentAppState by remember { mutableStateOf(AppState.SPLASH) }
                    var currentRole     by remember { mutableStateOf(AuthRole.VISITOR) }

                    val authViewModel   : AuthViewModel = viewModel()
                    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
                    val effectiveRole   by authViewModel.effectiveRole.collectAsState()

                    LaunchedEffect(isAuthenticated, effectiveRole) {
                        if (isAuthenticated && currentAppState != AppState.MAIN) {
                            currentRole     = effectiveRole ?: AuthRole.VISITOR
                            currentAppState = AppState.MAIN
                        }
                    }

                    AnimatedContent(
                        targetState    = currentAppState,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(700, easing = EaseInOutQuart)) +
                                    scaleIn(initialScale = 0.95f, animationSpec = tween(700)))
                                .togetherWith(fadeOut(animationSpec = tween(500)))
                        },
                        label = "Root_Orchestrator"
                    ) { state ->
                        when (state) {

                            AppState.SPLASH -> {
                                SplashScreen(
                                    onFinished = { currentAppState = AppState.LOGIN }
                                )
                            }

                            AppState.LOGIN -> {
                                LoginScreen(
                                    onLoginSuccess = { role ->
                                        currentRole     = role
                                        currentAppState = AppState.MAIN
                                    },
                                    onNavigateToRegister = { currentAppState = AppState.REGISTER },
                                    onNavigateToRecover  = { currentAppState = AppState.RECOVER }
                                )
                            }

                            AppState.REGISTER -> {
                                RegisterScreen(
                                    onRegisterSuccess = { currentAppState = AppState.MAIN },
                                    onNavigateToLogin = { currentAppState = AppState.LOGIN }
                                )
                            }

                            AppState.RECOVER -> {
                                RecoverPasswordScreen(
                                    onNavigateBack = { currentAppState = AppState.LOGIN }
                                )
                            }

                            AppState.MAIN -> {
                                MainScreen(
                                    onLogout = {
                                        authViewModel.logout()
                                        currentRole     = AuthRole.VISITOR
                                        currentAppState = AppState.LOGIN
                                    },
                                    isLoggedIn  = isAuthenticated,
                                    initialRole = currentRole,
                                    apiService  = RetrofitClient.apiService  // ✅ FIX
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}