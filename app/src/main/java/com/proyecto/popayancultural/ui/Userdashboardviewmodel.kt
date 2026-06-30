package com.proyecto.popayancultural.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.models.Order
import com.proyecto.popayancultural.data.models.SavedItem
import com.proyecto.popayancultural.data.repository.ComprasRepository
import com.proyecto.popayancultural.data.repository.FavoritosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  Estados UI
// ─────────────────────────────────────────────────────────────────────────────

sealed class FavoritosState {
    object Loading : FavoritosState()
    data class Success(val items: List<SavedItem>) : FavoritosState()
    data class Error(val message: String) : FavoritosState()
}

sealed class ComprasState {
    object Loading : ComprasState()
    data class Success(val orders: List<Order>) : ComprasState()
    data class Error(val message: String) : ComprasState()
}

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class UserDashboardViewModel(
    private val favoritosRepository: FavoritosRepository,
    private val comprasRepository  : ComprasRepository
) : ViewModel() {

    private val _favoritosState = MutableStateFlow<FavoritosState>(FavoritosState.Loading)
    val favoritosState: StateFlow<FavoritosState> = _favoritosState

    private val _comprasState = MutableStateFlow<ComprasState>(ComprasState.Loading)
    val comprasState: StateFlow<ComprasState> = _comprasState

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    // ─────────────────────────────────────────────────────────────────────────
    //  FAVORITOS
    // ─────────────────────────────────────────────────────────────────────────

    fun cargarFavoritos() {
        viewModelScope.launch {
            _favoritosState.value = FavoritosState.Loading
            favoritosRepository.getSavedItems()
                .onSuccess { items -> _favoritosState.value = FavoritosState.Success(items) }
                .onFailure { _favoritosState.value = FavoritosState.Error("Error de conexión con el Ledger") }
        }
    }

    fun eliminarFavorito(postId: Int) {
        viewModelScope.launch {
            favoritosRepository.toggleSaved(postId)
                .onSuccess {
                    val current = (_favoritosState.value as? FavoritosState.Success)?.items ?: return@launch
                    _favoritosState.value = FavoritosState.Success(
                        current.filter { it.savableId != postId }
                    )
                    _toastMessage.value = "Registro retirado del archivo"
                }
                .onFailure { _toastMessage.value = "No se pudo actualizar el Ledger" }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  COMPRAS
    // ─────────────────────────────────────────────────────────────────────────

    fun cargarCompras() {
        viewModelScope.launch {
            _comprasState.value = ComprasState.Loading
            comprasRepository.getMisCompras()
                .onSuccess { orders -> _comprasState.value = ComprasState.Success(orders) }
                .onFailure { _comprasState.value = ComprasState.Error("Error de conexión") }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FACTORY — igual al patrón de EventRepository
    // ─────────────────────────────────────────────────────────────────────────

    class Factory(
        private val favoritosRepository: FavoritosRepository,
        private val comprasRepository  : ComprasRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDashboardViewModel(favoritosRepository, comprasRepository) as T
        }
    }
}