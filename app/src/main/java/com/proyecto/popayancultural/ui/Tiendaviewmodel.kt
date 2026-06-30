package com.proyecto.popayancultural.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// ─── UI States ────────────────────────────────────────────────────────────────
sealed class TiendaUiState {
    object Loading : TiendaUiState()
    data class Success(val products: List<Product>) : TiendaUiState()
    data class Error(val message: String) : TiendaUiState()
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val order: Order) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class TiendaViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<TiendaUiState>(TiendaUiState.Loading)
    val uiState: StateFlow<TiendaUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var allProducts: List<Product> = emptyList()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.value = TiendaUiState.Loading
            try {
                val response = api.getProducts(limit = 50)
                if (response.isSuccessful) {
                    allProducts = response.body()?.data ?: emptyList()
                    aplicarFiltros()
                } else {
                    _uiState.value = TiendaUiState.Error("Error al cargar productos (${response.code()})")
                }
            } catch (e: IOException) {
                _uiState.value = TiendaUiState.Error("Sin conexión a internet")
            } catch (e: Exception) {
                _uiState.value = TiendaUiState.Error("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        aplicarFiltros()
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val query = _searchQuery.value.trim().lowercase()
        val cat   = _selectedCategory.value
        val filtered = allProducts.filter { product ->
            val matchSearch = query.isBlank() ||
                    product.name.lowercase().contains(query) ||
                    product.description.lowercase().contains(query)
            val matchCategory = cat == null ||
                    product.category?.name?.equals(cat, ignoreCase = true) == true
            matchSearch && matchCategory
        }
        _uiState.value = TiendaUiState.Success(filtered)
    }

    // ─── Lookup por ID (para DetalleProductoScreen) ───────────────────────────
    fun getProductById(id: Int): Product? = allProducts.find { it.id == id }

    // ─── Carrito ──────────────────────────────────────────────────────────────
    fun agregarAlCarrito(product: Product) {
        val current  = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            val item = current[existing]
            if (item.quantity < product.stockQuantity) {
                current[existing] = item.copy(quantity = item.quantity + 1)
            } else {
                _errorMessage.value = "No hay más stock disponible"
                return
            }
        } else {
            current.add(CartItem(product = product))
        }
        _cart.value = current
        _selectedProduct.value = null
    }

    fun quitarDelCarrito(productId: Int) {
        val current  = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == productId }
        if (existing >= 0) {
            val item = current[existing]
            if (item.quantity > 1) current[existing] = item.copy(quantity = item.quantity - 1)
            else current.removeAt(existing)
        }
        _cart.value = current
    }

    fun eliminarDelCarrito(productId: Int) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun vaciarCarrito() {
        _cart.value = emptyList()
    }

    val cartTotal: Double get() = _cart.value.sumOf { it.subtotal }
    val cartCount: Int    get() = _cart.value.sumOf { it.quantity }

    // ─── Checkout ─────────────────────────────────────────────────────────────
    fun procesarOrden() {
        val items = _cart.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            try {
                val request = CreateOrderRequest(
                    items = items.map { cartItem ->
                        OrderItemRequest(
                            id       = cartItem.product.id,
                            cantidad = cartItem.quantity
                        )
                    }
                )
                val response = api.crearOrden(request)
                if (response.isSuccessful) {
                    val order = response.body()?.data
                    if (order != null) {
                        _checkoutState.value = CheckoutState.Success(order)
                        vaciarCarrito()
                    } else {
                        _checkoutState.value = CheckoutState.Error("Respuesta inesperada del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val message = when (response.code()) {
                        400  -> "Stock insuficiente para uno o más productos"
                        401  -> "Debes iniciar sesión para comprar"
                        422  -> "Datos inválidos: $errorBody"
                        else -> "Error al procesar la orden (${response.code()})"
                    }
                    _checkoutState.value = CheckoutState.Error(message)
                }
            } catch (e: IOException) {
                _checkoutState.value = CheckoutState.Error("Sin conexión a internet")
            } catch (e: HttpException) {
                _checkoutState.value = CheckoutState.Error("Error del servidor: ${e.message()}")
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun resetCheckout() {
        _checkoutState.value = CheckoutState.Idle
    }

    // ─── Abrir carrito desde DetalleProductoScreen ────────────────────────────
    private val _openCart = MutableStateFlow(false)
    val openCart: StateFlow<Boolean> = _openCart

    fun agregarYAbrirCarrito(product: Product) {
        agregarAlCarrito(product)
        _openCart.value = true
    }

    fun resetOpenCart() { _openCart.value = false }

    // ─── Detalle (se mantiene para flujos internos del carrito) ───────────────
    fun seleccionarProducto(product: Product) { _selectedProduct.value = product }
    fun cerrarDetalle()                        { _selectedProduct.value = null }

    fun clearError() { _errorMessage.value = null }
}