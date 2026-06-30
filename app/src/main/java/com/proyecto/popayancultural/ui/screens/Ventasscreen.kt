package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.Order
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

data class VentasUiState(
    val isLoading  : Boolean    = true,
    val ordenes    : List<Order> = emptyList(),
    val activeTab  : String     = "pending",
    val procesando : Int?       = null,
    val errorMsg   : String?    = null
)

class VentasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VentasUiState())
    val uiState: StateFlow<VentasUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.apiService.getMySales()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, ordenes = response.body()?.data ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMsg = "Error al cargar ventas") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error de conexión") }
            }
        }
    }

    fun onTabChange(tab: String) = _uiState.update { it.copy(activeTab = tab) }

    fun confirmarPago(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = id) }
            try {
                RetrofitClient.apiService.confirmarOrden(id)
                _uiState.update { state ->
                    state.copy(
                        procesando = null,
                        ordenes = state.ordenes.map { if (it.id == id) it.copy(status = "confirmed") else it }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(procesando = null, errorMsg = "Error al confirmar") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────

private val ArtistRed = Color(0xFFEF4444)

@Composable
fun VentasScreen(
    onBack   : () -> Unit = {},
    viewModel: VentasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    val filteredOrdenes = uiState.ordenes.filter { it.status == uiState.activeTab }
    val formatCOP: (Double) -> String = { amount ->
        "\$${NumberFormat.getNumberInstance(Locale("es", "CO")).format(amount.toLong())}"
    }

    val pendingTotal   = uiState.ordenes.filter { it.status == "pending" }.sumOf { it.totalAmount }
    val confirmedTotal = uiState.ordenes.filter { it.status == "confirmed" }.sumOf { it.totalAmount }

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
            Column {
                Text("GESTIÓN DE VENTAS", color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("Órdenes y recaudación", color = Color.Gray, fontSize = 11.sp)
            }
        }

        // KPIs
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Pendiente",  formatCOP(pendingTotal),   Color(0xFFF59E0B)),
                Triple("Asegurado",  formatCOP(confirmedTotal), Color(0xFF10B981)),
                Triple("Órdenes",    "${uiState.ordenes.size}", Color.White)
            ).forEach { (label, value, color) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(label, color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("pending" to "Reservas", "confirmed" to "Completadas").forEach { (id, label) ->
                val isActive = uiState.activeTab == id
                val count    = uiState.ordenes.count { it.status == id }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) ArtistRed else CardBackground)
                        .clickable { viewModel.onTabChange(id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("$label ($count)",
                        color      = if (isActive) Color.White else Color.Gray,
                        fontSize   = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArtistRed)
            }
            filteredOrdenes.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ShoppingBag, null,
                        tint = Color.White.copy(0.08f), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay órdenes ${if (uiState.activeTab == "pending") "pendientes" else "completadas"}",
                        color = Color.Gray, fontSize = 13.sp)
                }
            }
            else -> LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(filteredOrdenes, key = { it.id }) { orden ->
                    OrdenCard(
                        orden       = orden,
                        formatCOP   = formatCOP,
                        procesando  = uiState.procesando == orden.id,
                        onConfirmar = { viewModel.confirmarPago(orden.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun OrdenCard(
    orden      : Order,
    formatCOP  : (Double) -> String,
    procesando : Boolean,
    onConfirmar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(orden.orderNumber, color = Color.White, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                    Text(orden.createdAt.take(10), color = Color.Gray, fontSize = 10.sp)
                }
                // Badge estado
                val (badgeLabel, badgeColor) = when (orden.status) {
                    "pending"   -> "PENDIENTE"  to Color(0xFFF59E0B)
                    "confirmed" -> "PAGADO"     to Color(0xFF10B981)
                    else        -> orden.status.uppercase() to Color.Gray
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badgeLabel, color = badgeColor, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp),
                color = Color.White.copy(0.05f))

            // Items de la orden
            orden.orderItems.forEach { item ->
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "${item.product?.name ?: "Producto"} x${item.quantity}",
                        color = Color.Gray, fontSize = 11.sp,
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(formatCOP(item.subtotal), color = Color.White, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp),
                color = Color.White.copy(0.05f))

            // Footer
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    orden.contactPhone?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phone, null, tint = Color.Gray,
                                modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                    Text("Total: ${formatCOP(orden.totalAmount)}",
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                if (orden.status == "pending") {
                    Button(
                        onClick  = onConfirmar,
                        enabled  = !procesando,
                        modifier = Modifier.height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(color = Color.White,
                                modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.CheckCircle, null,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Confirmar pago", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}