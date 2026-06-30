package com.proyecto.popayancultural.data.repository

import com.proyecto.popayancultural.data.ApiService
import com.proyecto.popayancultural.data.models.SavedItem
import com.proyecto.popayancultural.data.models.SavedItemsResponse
import com.proyecto.popayancultural.data.models.ToggleSavedRequest

class FavoritosRepository(
    private val apiService: ApiService
) {

    suspend fun getSavedItems(): Result<List<SavedItem>> {
        return try {
            val response = apiService.getSavedItems()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.data)
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

    suspend fun toggleSaved(postId: Int): Result<Unit> {
        return try {
            val request = ToggleSavedRequest(
                saveableType = "App\\Models\\Post",
                saveableId   = postId
            )
            val response = apiService.toggleSaved(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar favorito: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}