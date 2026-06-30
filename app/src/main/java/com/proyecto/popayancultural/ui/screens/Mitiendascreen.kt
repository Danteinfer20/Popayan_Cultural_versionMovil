package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  MODELOS LOCALES
// ─────────────────────────────────────────────────────────────

data class ProductoItem(
    val id           : Int,
    val name         : String,
    val price        : Double,
    val stockQuantity: Int,
    val status       : String,
    val mainImage    : String,
    val categoryName : String,
    val salesCount   : Int
)

data class MiTiendaUiState(
    val isLoading  : Boolean            = true,
    val productos  : List<ProductoItem> = emptyList(),
    val activeTab  : String             = "available",
    val searchQuery: String             = "",
    val errorMsg   : String?            = null,
    val kpis       : TiendaKpis         = TiendaKpis()
)

data class TiendaKpis(
    val totalRevenue: Long = 0L,
    val totalSales  : Int  = 0,
    val totalStock  : Int  = 0
)

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class MiTiendaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MiTiendaUiState())
    val uiState: StateFlow<MiTiendaUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.apiService.getProducts(myProducts = true)
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()

                    val productos = data.map { p ->
                        val effectiveStatus = if (p.stockQuantity <= 0 && p.status == "available")
                            "sold_out" else p.status

                        ProductoItem(
                            id            = p.id,
                            name          = p.name,
                            price         = p.price,
                            stockQuantity = p.stockQuantity,
                            status        = effectiveStatus,
                            mainImage     = p.imageUrl,
                            categoryName  = p.category?.name ?: "General",
                            salesCount    = p.stats?.sales ?: 0  // ✅ stats.sales, no salesCount
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            productos = productos,
                            kpis      = TiendaKpis(
                                totalRevenue = 0L,
                                totalSales   = productos.sumOf { p -> p.salesCount },
                                totalStock   = productos.sumOf { p -> p.stockQuantity }
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMsg = "Error al cargar tienda") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error de conexión") }
            }
        }
    }

    fun onTabChange(tab: String)  = _uiState.update { it.copy(activeTab = tab) }
    fun onSearchChange(q: String) = _uiState.update { it.copy(searchQuery = q) }

    fun toggleStatus(id: Int) {
        viewModelScope.launch {
            val producto = _uiState.value.productos.find { it.id == id } ?: return@launch
            val newStatus = if (producto.status == "paused")
                (if (producto.stockQuantity > 0) "available" else "sold_out")
            else "paused"
            try {
                RetrofitClient.apiService.updateProductStatus(id, mapOf("status" to newStatus))
                _uiState.update { state ->
                    state.copy(productos = state.productos.map {
                        if (it.id == id) it.copy(status = newStatus) else it
                    })
                }
            } catch (e: Exception) { }
        }
    }

    fun deleteProducto(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteProduct(id)
                _uiState.update { it.copy(productos = it.productos.filter { p -> p.id != id }) }
            } catch (e: Exception) { }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────

private val ArtistRed = Color(0xFFEF4444)

@Composable
fun MiTiendaScreen(
    onBack     : () -> Unit = {},
    onCrearObra: () -> Unit = {},
    viewModel  : MiTiendaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    val formatCOP: (Long) -> String = { amount ->
        "\$${NumberFormat.getNumberInstance(Locale("es", "CO")).format(amount)}"
    }

    val filteredProductos = uiState.productos.filter { p ->
        p.status == uiState.activeTab &&
                (uiState.searchQuery.isEmpty() || p.name.contains(uiState.searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(40.dp).background(CardBackground, RoundedCornerShape(12.dp))
            ) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("MI TIENDA POP", color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("Gestión de inventario", color = Color.Gray, fontSize = 11.sp)
            }
            Button(
                onClick  = onCrearObra,
                modifier = Modifier.height(36.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ArtistRed)
            ) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nuevo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // KPIs
        if (!uiState.isLoading) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Ingresos", formatCOP(uiState.kpis.totalRevenue), ArtistRed),
                    Triple("Vendidos", "${uiState.kpis.totalSales} uds", Color.White),
                    Triple("Stock",    "${uiState.kpis.totalStock} uds", Color(0xFF10B981))
                ).forEach { (label, value, color) ->
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(label, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Búsqueda
        OutlinedTextField(
            value         = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder   = { Text("Buscar producto...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Outlined.Search, null, tint = Color.Gray) },
            modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape         = RoundedCornerShape(16.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor      = ArtistRed,
                unfocusedBorderColor    = Color.White.copy(alpha = 0.06f),
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Tabs
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "available" to "Disponibles",
                "paused"    to "Pausados",
                "sold_out"  to "Agotados"
            ).forEach { (id, label) ->
                val isActive = uiState.activeTab == id
                val count    = uiState.productos.count { it.status == id }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) ArtistRed else CardBackground)
                        .clickable { viewModel.onTabChange(id) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("$label ($count)",
                        color      = if (isActive) Color.White else Color.Gray,
                        fontSize   = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Grid
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArtistRed)
            }
            filteredProductos.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Store, null,
                        tint = Color.White.copy(0.08f), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay productos en esta categoría", color = Color.Gray, fontSize = 13.sp)
                }
            }
            else -> LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding        = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement   = Arrangement.spacedBy(10.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                items(filteredProductos, key = { it.id }) { prod ->
                    ProductoCard(
                        producto  = prod,
                        formatCOP = { formatCOP(it.toLong()) },
                        onToggle  = { viewModel.toggleStatus(prod.id) },
                        onDelete  = { viewModel.deleteProducto(prod.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(
    producto : ProductoItem,
    formatCOP: (Double) -> String,
    onToggle : () -> Unit,
    onDelete : () -> Unit
) {
    val isAvailable = producto.status == "available"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box {
            if (producto.mainImage.isNotEmpty()) {
                AsyncImage(
                    model              = producto.mainImage,
                    contentDescription = null,
                    modifier           = Modifier.fillMaxWidth().height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale       = ContentScale.Crop
                )
            } else {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(BackgroundDeep),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Image, null, tint = Color.Gray.copy(0.3f), modifier = Modifier.size(24.dp)) }
            }
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) { Text("${producto.stockQuantity} uds", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(producto.name, color = Color.White, fontSize = 12.sp,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(producto.categoryName, color = Color.Gray, fontSize = 9.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(formatCOP(producto.price), color = ArtistRed, fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text("${producto.salesCount} ventas", color = Color.Gray, fontSize = 9.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick        = onToggle,
                    modifier       = Modifier.weight(1f).height(30.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = BorderStroke(0.5.dp, Color.White.copy(0.1f)),
                    colors         = ButtonDefaults.outlinedButtonColors(containerColor = BackgroundDeep),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (isAvailable) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        null, tint = Color.Gray, modifier = Modifier.size(12.dp)
                    )
                }
                OutlinedButton(
                    onClick        = onDelete,
                    modifier       = Modifier.weight(1f).height(30.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(0.2f)),
                    colors         = ButtonDefaults.outlinedButtonColors(containerColor = BackgroundDeep),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Delete, null,
                        tint = Color(0xFFEF4444).copy(0.7f), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}