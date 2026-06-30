package com.proyecto.popayancultural.ui

import com.proyecto.popayancultural.data.models.EventSummary

sealed class AgendaUiState {
    object Loading : AgendaUiState()
    object Empty   : AgendaUiState()
    data class Success(val events: List<EventSummary>) : AgendaUiState()
    data class Error(val message: String) : AgendaUiState()
}