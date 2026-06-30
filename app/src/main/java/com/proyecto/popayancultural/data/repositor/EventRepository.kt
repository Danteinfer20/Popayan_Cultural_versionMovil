package com.proyecto.popayancultural.data.repository

import com.proyecto.popayancultural.data.ApiService
import com.proyecto.popayancultural.data.models.EventSummary  // ← era Event
import com.proyecto.popayancultural.data.models.EventResponse
import retrofit2.Response

class EventRepository(
    private val apiService: ApiService
) {

    suspend fun getEvents(page: Int = 1): Result<List<EventSummary>> {  // ← era List<Event>
        return try {
            val response: Response<EventResponse> = apiService.getEvents(limit = null)

            if (response.isSuccessful) {
                val eventResponse = response.body()
                if (eventResponse != null) {
                    Result.success(eventResponse.data)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error en la respuesta: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}