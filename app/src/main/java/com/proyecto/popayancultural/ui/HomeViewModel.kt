package com.proyecto.popayancultural.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // isLoading solo se activa en la carga inicial — el polling recarga en silencio
    private val _isLoading   = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorGlobal = MutableStateFlow(false)
    val errorGlobal: StateFlow<Boolean> = _errorGlobal

    private val _obras      = MutableStateFlow<List<Post>>(emptyList())
    val obras: StateFlow<List<Post>> = _obras

    private val _productos  = MutableStateFlow<List<Product>>(emptyList())
    val productos: StateFlow<List<Product>> = _productos

    private val _artistas   = MutableStateFlow<List<Artist>>(emptyList())
    val artistas: StateFlow<List<Artist>> = _artistas

    private val _eventos    = MutableStateFlow<List<EventSummary>>(emptyList())
    val eventos: StateFlow<List<EventSummary>> = _eventos

    private val _educacion  = MutableStateFlow<List<Education>>(emptyList())
    val educacion: StateFlow<List<Education>> = _educacion

    // Indica si es la primera carga — el polling posterior no toca _isLoading
    private var primeraVez = true

    init { iniciarPolling() }

    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                cargarDatosHome(mostrarSpinner = primeraVez)
                if (primeraVez) primeraVez = false
                delay(30_000L)
            }
        }
    }

    fun cargarDatosHome(mostrarSpinner: Boolean = false) {
        viewModelScope.launch {
            if (mostrarSpinner) {
                _isLoading.value   = true
                _errorGlobal.value = false
            }
            try {
                val obrasDef    = async { RetrofitClient.apiService.getObras(limit = 5) }
                val productsDef = async { RetrofitClient.apiService.getProducts(limit = 5) }
                val artistsDef  = async { RetrofitClient.apiService.getArtists(limit = 5) }
                val eventsDef   = async { RetrofitClient.apiService.getEvents(limit = 4) }
                val eduDef      = async { RetrofitClient.apiService.getEducation(limit = 3) }

                val obrasRes    = obrasDef.await()
                val productsRes = productsDef.await()
                val artistsRes  = artistsDef.await()
                val eventsRes   = eventsDef.await()
                val eduRes      = eduDef.await()

                if (obrasRes.isSuccessful)    _obras.value     = obrasRes.body()?.data    ?: emptyList()
                if (productsRes.isSuccessful) _productos.value = productsRes.body()?.data ?: emptyList()
                if (artistsRes.isSuccessful)  _artistas.value  = artistsRes.body()?.data  ?: emptyList()
                if (eventsRes.isSuccessful)   _eventos.value   = eventsRes.body()?.data   ?: emptyList()
                if (eduRes.isSuccessful)      _educacion.value = eduRes.body()?.data       ?: emptyList()

            } catch (e: Exception) {
                if (mostrarSpinner) _errorGlobal.value = true
                // En polling silencioso: fallo de red se ignora, datos anteriores se mantienen
            } finally {
                if (mostrarSpinner) _isLoading.value = false
            }
        }
    }
}