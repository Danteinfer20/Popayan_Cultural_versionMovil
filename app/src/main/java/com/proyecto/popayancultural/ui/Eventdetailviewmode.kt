package com.proyecto.popayancultural.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.EventDetail
import com.proyecto.popayancultural.data.models.UserAttendance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EventDetailUiState {
    object Loading : EventDetailUiState()
    data class Success(val event: EventDetail) : EventDetailUiState()
    data class Error(val message: String) : EventDetailUiState()
}

sealed class AttendUiState {
    object Idle    : AttendUiState()
    object Loading : AttendUiState()
    data class Success(val attendance: UserAttendance) : AttendUiState()
    data class Error(val message: String) : AttendUiState()
}

class EventDetailViewModel : ViewModel() {

    private val _detailState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val detailState: StateFlow<EventDetailUiState> = _detailState.asStateFlow()

    private val _attendState = MutableStateFlow<AttendUiState>(AttendUiState.Idle)
    val attendState: StateFlow<AttendUiState> = _attendState.asStateFlow()

    private var eventIdActual: Int = -1

    fun loadEvent(eventId: Int) {
        eventIdActual = eventId
        iniciarPolling()
    }

    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                fetchEvent()
                delay(30_000L)
            }
        }
    }

    private suspend fun fetchEvent() {
        try {
            val response = RetrofitClient.apiService.getEventDetail(eventIdActual)
            val event: EventDetail? = response.body()?.data
            if (event != null) {
                _detailState.value = EventDetailUiState.Success(event)
            } else if (_detailState.value !is EventDetailUiState.Success) {
                _detailState.value = EventDetailUiState.Error("No se encontró el evento.")
            }
        } catch (e: Exception) {
            if (_detailState.value !is EventDetailUiState.Success) {
                _detailState.value = EventDetailUiState.Error(
                    e.message ?: "Error al sincronizar datos del evento"
                )
            }
            // Fallo silencioso si ya había datos cargados
        }
    }

    fun confirmAttendance(eventId: Int, isFree: Boolean) {
        viewModelScope.launch {
            _attendState.value = AttendUiState.Loading
            try {
                val response = RetrofitClient.apiService.attend(eventId)
                val ticket   = response.body()?.ticket()
                if (ticket != null) {
                    _attendState.value = AttendUiState.Success(ticket)
                } else {
                    _attendState.value = AttendUiState.Error(
                        response.body()?.message?.ifBlank { null } ?: "Error en la respuesta del servidor"
                    )
                }
            } catch (e: Exception) {
                _attendState.value = AttendUiState.Error(
                    e.message ?: "Fallo al procesar asistencia"
                )
            }
        }
    }

    fun resetAttendState() {
        _attendState.value = AttendUiState.Idle
    }
}