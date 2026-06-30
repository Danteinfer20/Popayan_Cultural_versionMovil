package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.proyecto.popayancultural.data.models.Product

// ─── Design Tokens ────────────────────────────────────────────────────────────
// Ajuste leve a negro profundo para máxima elegancia Dark Premium
private val Bg         = Color(0xFF050505)
private val Card       = Color(0xFF0A0A0C)
private val CardAlt    = Color(0xFF16161A)
private val Stroke     = Color(0xFF222228)
private val Violet     = Color(0xFFA855F7)
private val VioletSoft = Color(0xFF2D1A4A)
private val VioletDeep = Color(0xFF1A0A2E)
private val TextHigh   = Color(0xFFFFFFFF)
private val TextMid    = Color(0xFF9CA3AF)
private val TextLow    = Color(0xFF4B5563)
private val Green      = Color(0xFF22C55E)
private val Red        = Color(0xFFEF4444)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaScreen(
    isLoggedIn     : Boolean = false,
    onProductClick : (Int) -> Unit = {},
    viewModel      : TiendaViewModel = viewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val selectedCat   by viewModel.selectedCategory.collectAsState()
    val cart          by viewModel.cart.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val errorMessage  by viewModel.errorMessage.collectAsState()
    val snackbarHost  = remember { SnackbarHostState() }

    val openCart      by viewModel.openCart.collectAsState()
    var showCart      by remember { mutableStateOf(false) }
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(openCart) {
        if (openCart) {
            showCart = true
            viewModel.resetOpenCart()
        }
    }

    val categories = remember(uiState) {
        if (uiState is TiendaUiState.Success) {
            listOf(null) + (uiState as TiendaUiState.Success).products
                .mapNotNull { it.category?.name }
                .distinct()
                .sorted()
        } else listOf(null)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("TIENDA", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextHigh, letterSpacing = 0.5.sp)
                    Text("Artesanías y arte local", fontSize = 13.sp, color = TextMid)
                }

                // 🔥 CORRECCIÓN UX: Contenedor del carrito liberado de asfixia
                Box(modifier = Modifier.size(46.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Card) // Fondo siempre limpio
                            .border(1.dp, if (cart.isNotEmpty()) Violet.copy(0.6f) else Stroke, CircleShape)
                            .clickable { showCart = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            null,
                            tint = if (cart.isNotEmpty()) Violet else TextMid,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Badge extruido usando Offset matemático
                    if (cart.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Violet),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${cart.sumOf { it.quantity }}",
                                fontSize = 10.sp,
                                color = TextHigh,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Búsqueda ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Card)
                    .border(1.dp, if (searchQuery.isNotBlank()) Violet.copy(0.5f) else Stroke, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null, tint = if (searchQuery.isNotBlank()) Violet else TextLow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isBlank()) Text("Buscar productos...", fontSize = 14.sp, color = TextLow)
                        BasicTextField(
                            value         = searchQuery,
                            onValueChange = viewModel::onSearch,
                            textStyle     = TextStyle(fontSize = 14.sp, color = TextHigh),
                            cursorBrush   = SolidColor(Violet),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Close, null, tint = TextMid, modifier = Modifier.size(16.dp).clickable { viewModel.onSearch("") })
                    }
                }
            }

            // ── Categorías ────────────────────────────────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCat == cat
                    Surface(
                        onClick = { viewModel.onCategorySelected(cat) },
                        shape   = RoundedCornerShape(20.dp),
                        color   = if (isSelected) Violet else Card,
                        border  = BorderStroke(1.dp, if (isSelected) Violet else Stroke)
                    ) {
                        Text(
                            cat ?: "Todos",
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color      = if (isSelected) TextHigh else TextMid
                        )
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────────────────────
            when (val s = uiState) {
                is TiendaUiState.Loading -> TiendaShimmer()
                is TiendaUiState.Error   -> TiendaError(s.message) { viewModel.cargarProductos() }
                is TiendaUiState.Success -> {
                    if (s.products.isEmpty()) {
                        TiendaEmpty()
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns               = StaggeredGridCells.Fixed(2),
                            modifier              = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding        = PaddingValues(bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing   = 12.dp
                        ) {
                            items(s.products, key = { it.id }) { product ->
                                ProductCard(product) { onProductClick(product.id) }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHost, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }

    // ── Sheet: Carrito ────────────────────────────────────────────────────────
    if (showCart) {
        ModalBottomSheet(
            onDismissRequest = { showCart = false },
            sheetState       = cartSheetState,
            containerColor   = Card,
            dragHandle       = { Box(Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Stroke)) }
        ) {
            CartSheet(
                items           = cart,
                total           = viewModel.cartTotal,
                checkoutState   = checkoutState,
                isLoggedIn      = isLoggedIn,
                onQuitar        = { viewModel.quitarDelCarrito(it) },
                onAgregar       = { id ->
                    val prod = cart.find { it.product.id == id }?.product
                    prod?.let { viewModel.agregarAlCarrito(it) }
                },
                onEliminar      = { viewModel.eliminarDelCarrito(it) },
                onProcesar      = { viewModel.procesarOrden() },
                onResetCheckout = { viewModel.resetCheckout() },
                onCerrar        = { showCart = false }
            )
        }
    }
}

// ─── ProductCard ──────────────────────────────────────────────────────────────
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // 🔥 Borde más sutil, matemático
            .background(Card)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // 🔥 CORRECCIÓN PROPORCIÓN: aspectRatio(1f) = Cuadrado perfecto
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            SubcomposeAsyncImage(
                model              = product.imageUrl.takeIf { it.isNotBlank() },
                contentDescription = product.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().background(CardAlt)) },
                error   = {
                    Box(
                        Modifier.fillMaxSize().background(Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.ShoppingBag, null, tint = Violet.copy(0.4f), modifier = Modifier.size(32.dp)) }
                }
            )
            if (!product.isAvailable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (product.status == "sold_out") "Agotado" else "No disponible",
                        fontSize   = 9.sp,
                        color      = Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(10.dp)) { // 🔥 Padding ajustado
            product.category?.let {
                Text(it.name.uppercase(), fontSize = 9.sp, color = Violet, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
            }
            Text(
                product.name,
                fontSize = 12.sp, // 🔥 Texto contenido
                fontWeight = FontWeight.SemiBold,
                color = TextHigh,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(product.displayPrice, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Violet)
                if (!product.isAvailable) {
                    Spacer(Modifier.width(6.dp))
                    Text("Agotado", fontSize = 10.sp, color = Red)
                }
            }
        }
    }
}

// ─── Estados ──────────────────────────────────────────────────────────────────
@Composable
private fun TiendaEmpty() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(VioletSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ShoppingBag, null, tint = Violet, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Sin productos disponibles", fontSize = 16.sp, color = TextMid, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Intenta con otra categoría", fontSize = 13.sp, color = TextLow)
    }
}

@Composable
private fun TiendaError(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Red.copy(0.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = Red, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(message, fontSize = 14.sp, color = TextMid)
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Violet), border = BorderStroke(1.dp, Violet.copy(0.4f))) {
            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun TiendaShimmer() {
    LazyVerticalStaggeredGrid(
        columns               = StaggeredGridCells.Fixed(2),
        modifier              = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding        = PaddingValues(bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing   = 12.dp
    ) {
        items(6) { i ->
            // Alturas ajustadas para Shimmer coherente con aspectRatio(1f)
            val height = if (i % 3 == 0) 150.dp else if (i % 3 == 1) 160.dp else 140.dp
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Card).border(1.dp, Stroke, RoundedCornerShape(12.dp))) {
                Box(Modifier.fillMaxWidth().height(height).background(CardAlt))
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(CardAlt))
                    Box(Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(CardAlt))
                    Box(Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(CardAlt))
                }
            }
        }
    }
}