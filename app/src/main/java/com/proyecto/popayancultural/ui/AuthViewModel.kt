package com.proyecto.popayancultural.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.TokenManager
import com.proyecto.popayancultural.data.models.AuthResponse
import com.proyecto.popayancultural.data.models.User
import com.proyecto.popayancultural.data.models.LoginRequest
import com.proyecto.popayancultural.data.models.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ─────────────────────────────────────────────────────────────
//  ROL EFECTIVO  (misma lógica que ProfileViewModel)
//  Se calcula aquí para que el NavGraph no necesite importar
//  ProfileViewModel solo para leer el destino post-login.
// ─────────────────────────────────────────────────────────────

enum class AuthRole { VISITOR, ARTIST, CULTURAL_MANAGER, EDUCATOR, ADMIN }

fun User.effectiveAuthRole(): AuthRole = when {
    user_type == "admin"                                          -> AuthRole.ADMIN
    user_type == "artist"           && isVerified     -> AuthRole.ARTIST
    user_type == "cultural_manager" && isVerified     -> AuthRole.CULTURAL_MANAGER
    user_type == "educator"         && isVerified     -> AuthRole.EDUCATOR
    else                                                          -> AuthRole.VISITOR
}

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application.applicationContext)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ── ROL EFECTIVO POST-LOGIN ───────────────────────────────
    // Derivado de _user: la UI lo observa para decidir a dónde navegar
    private val _effectiveRole = MutableStateFlow<AuthRole?>(null)
    val effectiveRole: StateFlow<AuthRole?> = _effectiveRole.asStateFlow()

    // ── RECUPERACIÓN DE CONTRASEÑA ────────────────────────────
    private val _recoverySuccess = MutableStateFlow(false)
    val recoverySuccess: StateFlow<Boolean> = _recoverySuccess

    // ── INIT: restaurar sesión guardada ───────────────────────
    init {
        val token     = tokenManager.getToken()
        val savedUser = tokenManager.getUser()
        if (token != null && savedUser != null) {
            _user.value              = savedUser
            _effectiveRole.value     = savedUser.effectiveAuthRole()
            _isAuthenticated.value   = true
        }
    }

    // ─────────────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email y contraseña son obligatorios"
            return
        }

        _isLoading.value  = true
        _error.value      = null

        RetrofitClient.apiService.login(LoginRequest(email, password))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call    : Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            body.token?.let { tokenManager.saveToken(it) }
                            body.user?.let { u ->
                                tokenManager.saveUser(u)
                                _user.value          = u
                                _effectiveRole.value = u.effectiveAuthRole()   // ← clave
                            }
                            _isAuthenticated.value = true
                            _error.value           = null
                        } else {
                            _error.value = body?.message ?: "Error en el servidor"
                        }
                    } else {
                        _error.value = "Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value     = t.message ?: "Error de red"
                }
            })
    }

    // ─────────────────────────────────────────────────────────
    //  REGISTRO
    // ─────────────────────────────────────────────────────────
    fun register(
        name                : String,
        email               : String,
        password            : String,
        passwordConfirmation: String,
        userType            : String = "visitor"
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || passwordConfirmation.isBlank()) {
            _error.value = "Todos los campos son obligatorios"
            return
        }
        if (password != passwordConfirmation) {
            _error.value = "Las contraseñas no coinciden"
            return
        }

        _isLoading.value = true
        _error.value     = null

        val request = RegisterRequest(
            name                  = name,
            email                 = email,
            password              = password,
            password_confirmation = passwordConfirmation,
            user_type             = userType
        )

        RetrofitClient.apiService.register(request)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call    : Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            body.token?.let { tokenManager.saveToken(it) }
                            body.user?.let { u ->
                                tokenManager.saveUser(u)
                                _user.value          = u
                                _effectiveRole.value = u.effectiveAuthRole()   // ← idem
                            }
                            _isAuthenticated.value = true
                            _error.value           = null
                        } else {
                            _error.value = body?.message ?: "Error en el servidor"
                        }
                    } else {
                        _error.value = "Error: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value     = t.message ?: "Error de red"
                }
            })
    }

    // ─────────────────────────────────────────────────────────
    //  RECUPERACIÓN DE CONTRASEÑA
    // ─────────────────────────────────────────────────────────
    fun recoverPassword(email: String) {
        if (email.isBlank()) {
            _error.value = "El correo electrónico es obligatorio"
            return
        }
        _isLoading.value       = true
        _error.value           = null
        _recoverySuccess.value = false

        RetrofitClient.apiService.recoverPassword(mapOf("email" to email))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call    : Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _recoverySuccess.value = true
                        _error.value           = null
                    } else {
                        _error.value = "No se pudo procesar la solicitud. Verifica el correo."
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value     = t.message ?: "Error de red"
                }
            })
    }

    // ─────────────────────────────────────────────────────────
    //  UTILIDADES
    // ─────────────────────────────────────────────────────────
    fun clearError()         { _error.value          = null  }
    fun resetRecoveryState() { _recoverySuccess.value = false }

    fun logout() {
        tokenManager.clear()
        _isAuthenticated.value = false
        _user.value            = null
        _effectiveRole.value   = null
    }
}