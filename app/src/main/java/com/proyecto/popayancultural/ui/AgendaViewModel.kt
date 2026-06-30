package com.proyecto.popayancultural.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.EventSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AgendaViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<AgendaUiState>(AgendaUiState.Loading)
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _daysWithEvents = MutableStateFlow<Set<String>>(emptySet())
    val daysWithEvents: StateFlow<Set<String>> = _daysWithEvents.asStateFlow()

    private var cachedEvents: List<EventSummary> = emptyList()

    init { iniciarPolling() }

    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                loadEvents()
                delay(30_000L)
            }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = AgendaUiState.Loading
            try {
                val response = api.getEvents()
                if (response.isSuccessful) {
                    cachedEvents = response.body()?.data ?: emptyList()
                    _daysWithEvents.value = cachedEvents
                        .map { it.startDate.toDateOnly() }
                        .toSet()
                    filterByDate(_selectedDate.value)
                } else {
                    _uiState.value = AgendaUiState.Error(
                        "Error ${response.code()}: no se pudo cargar la agenda."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AgendaUiState.Error(
                    e.message ?: "Error de red. Verifica tu conexión."
                )
            }
        }
    }

    fun selectDate(dateIso: String) {
        _selectedDate.value = dateIso
        filterByDate(dateIso)
    }

    private fun filterByDate(dateIso: String) {
        val filtered = cachedEvents.filter { it.startDate.toDateOnly() == dateIso }
        _uiState.value = if (filtered.isEmpty()) AgendaUiState.Empty
        else AgendaUiState.Success(filtered)
    }

    private fun String.toDateOnly(): String = this.take(10)
}