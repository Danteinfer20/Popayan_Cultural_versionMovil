package com.proyecto.popayancultural.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  MODELOS UI  (lo que consume la pantalla)
// ─────────────────────────────────────────────────────────────

data class ArtistKpis(
    val totalWorks    : Int    = 0,
    val featuredWorks : Int    = 0,
    val salesCount    : Int    = 0,
    val totalRevenue  : Double = 0.0
)

data class RecentWork(
    val id        : Int,
    val title     : String,
    val status    : String,   // "draft" | "published"
    val imageUrl  : String,
    val createdAt : String
)

data class ArtistDashboardUiState(
    val isLoading    : Boolean          = true,
    val kpis         : ArtistKpis       = ArtistKpis(),
    val recentWorks  : List<RecentWork> = emptyList(),
    val errorMessage : String?          = null
)

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class ArtistDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ArtistDashboardUiState())
    val uiState: StateFlow<ArtistDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.apiService.getArtistDashboard()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()!!.data!!

                    val kpis = ArtistKpis(
                        totalWorks   = data.kpis.totalWorks,
                        featuredWorks = 0,           // backend no lo retorna
                        salesCount   = data.kpis.sales,
                        totalRevenue = data.kpis.revenue
                    )

                    val works = data.recentWorks.map { w ->
                        RecentWork(
                            id        = w.id,
                            title     = parseTitle(w.title),
                            status    = w.status,
                            imageUrl  = w.mainImage,
                            createdAt = formatDate(w.createdAt)
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading   = false,
                            kpis        = kpis,
                            recentWorks = works
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "No se pudo cargar el panel")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    // ── Helpers ───────────────────────────────────────────────

    // Parsea título que puede venir como JSON {"es":"Título"} o texto plano
    private fun parseTitle(raw: String): String = try {
        val cleaned = raw.trim()
        if (cleaned.startsWith("{")) {
            org.json.JSONObject(cleaned).optString("es", cleaned)
        } else cleaned
    } catch (e: Exception) { raw }

    // Formatea "2026-06-22T10:00:00.000000Z" → "22/6/2026"
    private fun formatDate(raw: String): String = try {
        val date = raw.substringBefore("T").split("-")
        if (date.size == 3) "${date[2].trimStart('0')}/${date[1].trimStart('0')}/${date[0]}"
        else raw
    } catch (e: Exception) { raw }
}