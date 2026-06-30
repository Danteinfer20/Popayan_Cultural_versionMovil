package com.proyecto.popayancultural.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class DetalleObraUiState {
    object Loading : DetalleObraUiState()
    data class Success(val obra: Post) : DetalleObraUiState()
    data class Error(val message: String) : DetalleObraUiState()
}

data class AccionesState(
    val reaccionado: Boolean = false,
    val coleccionado: Boolean = false,
    val reacciones: Int = 0,
    val reposts: Int = 0,
    val vistas: Int = 0,
    val cargandoReaccion: Boolean = false,
    val cargandoColeccion: Boolean = false,
    val cargandoRepost: Boolean = false
)

data class CommentUi(
    val id: Int,
    val autorNombre: String,
    val autorAvatar: String?,
    val texto: String,
    val fecha: String
)

data class ComentariosState(
    val lista: List<CommentUi> = emptyList(),
    val cargando: Boolean = false,
    val enviando: Boolean = false,
    val texto: String = "",
    val expandido: Boolean = false
)

class DetalleObraViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<DetalleObraUiState>(DetalleObraUiState.Loading)
    val uiState: StateFlow<DetalleObraUiState> = _uiState

    private val _acciones = MutableStateFlow(AccionesState())
    val acciones: StateFlow<AccionesState> = _acciones

    private val _comentarios = MutableStateFlow(ComentariosState())
    val comentarios: StateFlow<ComentariosState> = _comentarios

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var postId: Int = -1
    private var pollingJob: Job? = null

    var slugActual: String = ""
        private set

    fun cargar(slug: String) {
        slugActual = slug
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                fetchObra()
                delay(30_000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun fetchObra() {
        try {
            val response = api.getPostDetalle(slugActual)
            if (response.isSuccessful) {
                val post = response.body()?.data
                if (post != null) {
                    postId = post.id
                    _uiState.value = DetalleObraUiState.Success(post)

                    // Solo actualizar acciones si no hay operación en curso
                    if (!_acciones.value.cargandoReaccion && !_acciones.value.cargandoColeccion) {
                        _acciones.value = _acciones.value.copy(
                            reacciones  = post.stats?.reactions ?: 0,
                            reposts     = post.stats?.shares ?: 0,
                            vistas      = post.stats?.views ?: 0,
                            reaccionado = post.userInteraction?.hasReacted ?: false,
                            coleccionado = post.userInteraction?.isSaved ?: false
                        )
                    }

                    // Solo actualizar comentarios si el input está vacío (no interrumpir escritura)
                    if (_comentarios.value.texto.isBlank()) {
                        val coms = post.comments?.map { c: Comment ->
                            CommentUi(
                                id          = c.id,
                                autorNombre = c.user?.name.orEmpty(),
                                autorAvatar = c.user?.profilePicture ?: c.user?.avatar,
                                texto       = c.content.orEmpty(),
                                fecha       = c.humanTime ?: c.createdAt.orEmpty()
                            )
                        } ?: emptyList()
                        _comentarios.value = _comentarios.value.copy(lista = coms)
                    }
                } else {
                    if (_uiState.value !is DetalleObraUiState.Success) {
                        _uiState.value = DetalleObraUiState.Error("Obra no encontrada o datos incompletos")
                    }
                }
            } else {
                if (_uiState.value !is DetalleObraUiState.Success) {
                    _uiState.value = DetalleObraUiState.Error("Error al cargar la obra (Código ${response.code()})")
                }
            }
        } catch (e: IOException) {
            if (_uiState.value !is DetalleObraUiState.Success) {
                _uiState.value = DetalleObraUiState.Error("Error de conexión: ${e.localizedMessage ?: "Desconocido"}")
            }
        } catch (e: HttpException) {
            if (_uiState.value !is DetalleObraUiState.Success) {
                _uiState.value = DetalleObraUiState.Error("Error del servidor: ${e.message()}")
            }
        } catch (e: Exception) {
            if (_uiState.value !is DetalleObraUiState.Success) {
                _uiState.value = DetalleObraUiState.Error("Error inesperado: ${e.localizedMessage ?: "Desconocido"}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // FIX: ToggleReactionRequest ahora usa post_id + reaction_type
    //      para coincidir con lo que espera ReactionController@toggle
    // ─────────────────────────────────────────────────────────────────
    fun toggleReaccion() {
        if (_acciones.value.cargandoReaccion || postId == -1) {
            _errorMessage.value = "No se puede dar like: obra no cargada correctamente"
            return
        }
        viewModelScope.launch {
            val prev       = _acciones.value
            val nuevoEstado = !prev.reaccionado
            // Optimistic update
            _acciones.value = prev.copy(
                reaccionado      = nuevoEstado,
                reacciones       = if (nuevoEstado) prev.reacciones + 1 else maxOf(0, prev.reacciones - 1),
                cargandoReaccion = true
            )
            try {
                val response = api.toggleReaccion(
                    ToggleReactionRequest(
                        postId       = postId,
                        reactionType = "like"   // backend acepta: like | love | inspire | interest
                    )
                )
                // El backend devuelve status "added" o "removed" — ambos son éxito
                if (!response.isSuccessful) {
                    // Revertir si el servidor rechazó
                    _acciones.value = prev
                    _errorMessage.value = "Error al dar like (${response.code()})"
                }
            } catch (e: Exception) {
                _acciones.value  = prev
                _errorMessage.value = "Error al dar like: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _acciones.value = _acciones.value.copy(cargandoReaccion = false)
            }
        }
    }

    fun toggleColeccion() {
        if (_acciones.value.cargandoColeccion || postId == -1) {
            _errorMessage.value = "No se puede guardar: obra no cargada correctamente"
            return
        }
        viewModelScope.launch {
            val prev        = _acciones.value
            val nuevoEstado = !prev.coleccionado
            _acciones.value = prev.copy(
                coleccionado      = nuevoEstado,
                cargandoColeccion = true
            )
            try {
                val response = api.toggleSaved(
                    ToggleSavedRequest(
                        saveableType = "post",
                        saveableId   = postId
                    )
                )
                if (!response.isSuccessful) {
                    _acciones.value     = prev
                    _errorMessage.value = "Error al guardar (${response.code()})"
                }
            } catch (e: Exception) {
                _acciones.value     = prev
                _errorMessage.value = "Error al guardar: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _acciones.value = _acciones.value.copy(cargandoColeccion = false)
            }
        }
    }

    fun registrarRepost() {
        if (_acciones.value.cargandoRepost || postId == -1) {
            _errorMessage.value = "No se puede compartir: obra no cargada correctamente"
            return
        }
        viewModelScope.launch {
            val prevReposts = _acciones.value.reposts
            _acciones.value = _acciones.value.copy(
                reposts        = prevReposts + 1,
                cargandoRepost = true
            )
            try {
                val response = api.sharePost(postId)
                if (!response.isSuccessful) {
                    _acciones.value     = _acciones.value.copy(reposts = prevReposts)
                    _errorMessage.value = "Error al compartir (${response.code()})"
                }
            } catch (e: Exception) {
                _acciones.value     = _acciones.value.copy(reposts = prevReposts)
                _errorMessage.value = "Error al compartir: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _acciones.value = _acciones.value.copy(cargandoRepost = false)
            }
        }
    }

    fun onTextoComentario(texto: String) {
        _comentarios.value = _comentarios.value.copy(texto = texto)
    }

    // ─────────────────────────────────────────────────────────────────
    // FIX: Agregar logs de error para debug + manejo correcto de
    //      la respuesta exitosa cuando nuevoComentario?.data es null
    // ─────────────────────────────────────────────────────────────────
    fun enviarComentario() {
        val texto = _comentarios.value.texto.trim()
        if (texto.isBlank() || _comentarios.value.enviando || postId == -1) {
            if (postId == -1) _errorMessage.value = "No se puede comentar: obra no cargada"
            return
        }
        viewModelScope.launch {
            _comentarios.value = _comentarios.value.copy(enviando = true)
            try {
                val response = api.crearComentario(
                    CreateCommentRequest(postId = postId, content = texto)
                )
                if (response.isSuccessful) {
                    val nuevoComentario = response.body()?.data
                    val nuevoUi = if (nuevoComentario != null) {
                        CommentUi(
                            id          = nuevoComentario.id,
                            autorNombre = nuevoComentario.user?.name.orEmpty(),
                            autorAvatar = nuevoComentario.user?.profilePicture ?: nuevoComentario.user?.avatar,
                            texto       = texto,
                            fecha       = nuevoComentario.humanTime ?: "Ahora"
                        )
                    } else {
                        // El backend respondió 201 pero sin body parseable: igual insertamos
                        // el comentario localmente con datos mínimos
                        CommentUi(
                            id          = System.currentTimeMillis().toInt(),
                            autorNombre = "Tú",
                            autorAvatar = null,
                            texto       = texto,
                            fecha       = "Ahora"
                        )
                    }
                    _comentarios.value = _comentarios.value.copy(
                        lista    = listOf(nuevoUi) + _comentarios.value.lista,
                        texto    = "",
                        enviando = false
                    )
                } else {
                    val errorBody = response.errorBody()?.string() ?: "sin detalle"
                    _errorMessage.value = "Error al comentar (${response.code()}): $errorBody"
                    _comentarios.value  = _comentarios.value.copy(enviando = false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al comentar: ${e.localizedMessage ?: "Desconocido"}"
                _comentarios.value  = _comentarios.value.copy(enviando = false)
            }
        }
    }

    fun toggleComentarios() {
        _comentarios.value = _comentarios.value.copy(expandido = !_comentarios.value.expandido)
    }

    fun clearError() {
        _errorMessage.value = null
    }
}