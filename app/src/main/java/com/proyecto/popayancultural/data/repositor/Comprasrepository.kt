package com.proyecto.popayancultural.data.repository

import com.proyecto.popayancultural.data.ApiService
import com.proyecto.popayancultural.data.models.Order

class ComprasRepository(
    private val apiService: ApiService
) {

    suspend fun getMisCompras(): Result<List<Order>> {
        return try {
            val response = apiService.misOrdenes()
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

    suspend fun getMisVentas(): Result<List<Order>> {
        return try {
            val response = apiService.getMySales()
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
}