package com.proyecto.popayancultural.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.Artist
import com.proyecto.popayancultural.data.models.Education
import com.proyecto.popayancultural.data.models.Post
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ExploraUiState {
    object Loading : ExploraUiState()
    data class Success(
        val artistas : List<Artist>,
        val obras    : List<Post>,
        val educacion: List<Education>
    ) : ExploraUiState()
    data class Error(val message: String) : ExploraUiState()
}

class ExploraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ExploraUiState>(ExploraUiState.Loading)
    val uiState: StateFlow<ExploraUiState> = _uiState

    init { loadExploraData() }

    fun loadExploraData() {
        // Si ya hay datos en memoria no vuelve a llamar la red
        // (por ejemplo al volver del detalle de una obra/artista/lección)
        if (_uiState.value is ExploraUiState.Success) return

        viewModelScope.launch {
            _uiState.value = ExploraUiState.Loading

            val artistasDeferred  = async { runCatching { RetrofitClient.apiService.getArtists() } }
            val obrasDeferred     = async { runCatching { RetrofitClient.apiService.getObras() } }
            val educacionDeferred = async { runCatching { RetrofitClient.apiService.getEducation() } }

            val artistas  = artistasDeferred.await().getOrNull()
                ?.takeIf { it.isSuccessful }?.body()?.data ?: emptyList()
            val obras     = obrasDeferred.await().getOrNull()
                ?.takeIf { it.isSuccessful }?.body()?.data ?: emptyList()
            val educacion = educacionDeferred.await().getOrNull()
                ?.takeIf { it.isSuccessful }?.body()?.data ?: emptyList()

            _uiState.value = if (artistas.isEmpty() && obras.isEmpty() && educacion.isEmpty()) {
                ExploraUiState.Error("No se pudo cargar el contenido")
            } else {
                ExploraUiState.Success(
                    artistas  = artistas,
                    obras     = obras,
                    educacion = educacion
                )
            }
        }
    }
}