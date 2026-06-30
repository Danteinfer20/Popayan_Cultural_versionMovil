package com.proyecto.popayancultural.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.Post
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostsViewModel : ViewModel() {

    private val _posts     = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error     = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { iniciarPolling() }

    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                fetchPosts()
                delay(30_000L)
            }
        }
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null
            try {
                val response = RetrofitClient.apiService.getObras()
                if (response.isSuccessful) {
                    _posts.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}