package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.models.Order
import com.proyecto.popayancultural.data.models.OrderItemDisplay
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.ComprasState
import com.proyecto.popayancultural.ui.viewmodels.EffectiveRole
import com.proyecto.popayancultural.ui.viewmodels.ProfileViewModel
import com.proyecto.popayancultural.ui.viewmodels.UserDashboardViewModel
import com.proyecto.popayancultural.ui.viewmodels.effectiveRole
import kotlinx.coroutines.launch
import qrcode.QRCode
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel                  : ProfileViewModel = viewModel(),
    dashboardViewModel         : UserDashboardViewModel,
    onNavigateToUserDashboard  : () -> Unit = {},
    onNavigateToArtistDashboard: () -> Unit = {},
    onNavigateToSettings       : () -> Unit = {},
    onNavigateToMisObras       : () -> Unit = {},
    onNavigateToMiTienda       : () -> Unit = {},
    onNavigateToCrearObra      : () -> Unit = {},
    onNavigateToVentas         : () -> Unit = {},
    onLogout                   : () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val effectiveRole = profile.effectiveRole()
    val isArtistRole  = effectiveRole == EffectiveRole.ARTIST
            || effectiveRole == EffectiveRole.CULTURAL_MANAGER
            || effectiveRole == EffectiveRole.EDUCATOR

    val onDashboardTap: () -> Unit = if (isArtistRole) onNavigateToArtistDashboard
    else onNavigateToUserDashboard

    val roleAccent: Color = when (effectiveRole) {
        EffectiveRole.ARTIST           -> Color(0xFFEF4444)
        EffectiveRole.CULTURAL_MANAGER -> Color(0xFF10B981)
        EffectiveRole.EDUCATOR         -> Color(0xFFF59E0B)
        EffectiveRole.ADMIN            -> Color(0xFF3B82F6)
        EffectiveRole.VISITOR          -> VioletAcento
    }
    val roleLabel: String = when (effectiveRole) {
        EffectiveRole.ARTIST           -> "ARTISTA"
        EffectiveRole.CULTURAL_MANAGER -> "GESTOR CULTURAL"
        EffectiveRole.EDUCATOR         -> "EDUCADOR"
        EffectiveRole.ADMIN            -> "ADMINISTRADOR"
        EffectiveRole.VISITOR          -> "VISITANTE"
    }

    val tabs = if (isArtistRole)
        listOf("Mis Obras", "Mi Tienda", "Crear Obra", "Ventas")
    else
        listOf("Guardados", "Compras", "Trayectoria")

    var selectedTab by remember { mutableIntStateOf(0) }

    var isCoverPressed by remember { mutableStateOf(false) }
    val coverSat by animateFloatAsState(if (isCoverPressed) 1f else 0f, tween(500), label = "sat")
    val coverMatrix = ColorMatrix().apply { setToSaturation(coverSat) }

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSettingsSheet  by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Guardados state
    val savedItems   by viewModel.savedItems.collectAsStateWithLifecycle()
    val savedLoading by viewModel.savedLoading.collectAsStateWithLifecycle()

    // Compras state
    val comprasState by dashboardViewModel.comprasState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTab) {
        if (!isArtistRole) {
            when (selectedTab) {
                0 -> viewModel.loadSavedItems()
                1 -> dashboardViewModel.cargarCompras()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {

        if (uiState.isLoading && profile.name.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VioletAcento)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = rememberLazyListState()) {

                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onPress = {
                                        isCoverPressed = true; tryAwaitRelease(); isCoverPressed = false
                                    })
                                }
                        ) {
                            AsyncImage(
                                model = profile.coverPicture.ifEmpty {
                                    "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"
                                },
                                contentDescription = null,
                                modifier     = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                colorFilter  = ColorFilter.colorMatrix(coverMatrix)
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, BackgroundDeep.copy(alpha = 0.85f)),
                                        startY = 140f
                                    )
                                )
                            )
                        }
                        IconButton(
                            onClick  = { showSettingsSheet = true },
                            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp)
                                .size(40.dp).background(CardBackground.copy(alpha = 0.75f), CircleShape)
                        ) { Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                        IconButton(
                            onClick  = onDashboardTap,
                            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp)
                                .size(40.dp).background(roleAccent.copy(alpha = 0.18f), CircleShape)
                                .border(1.dp, roleAccent.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (effectiveRole == EffectiveRole.ARTIST) Icons.Outlined.Palette else Icons.Outlined.Dashboard,
                                contentDescription = null, tint = roleAccent, modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter).size(108.dp)
                                .background(BackgroundDeep, CircleShape).padding(5.dp).clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = profile.profilePicture.ifEmpty {
                                    "https://ui-avatars.com/api/?name=${profile.name.ifEmpty{"U"}}&background=111115&color=ffffff"
                                },
                                contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier.align(Alignment.BottomEnd).size(26.dp)
                                    .background(roleAccent, CircleShape).border(2.dp, BackgroundDeep, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (effectiveRole) {
                                        EffectiveRole.ARTIST           -> Icons.Outlined.Palette
                                        EffectiveRole.EDUCATOR         -> Icons.Outlined.School
                                        EffectiveRole.CULTURAL_MANAGER -> Icons.Outlined.Event
                                        EffectiveRole.ADMIN            -> Icons.Outlined.AdminPanelSettings
                                        else                           -> Icons.Outlined.Person
                                    },
                                    contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile.name.uppercase().ifEmpty { "USUARIO" },
                            color = Color.White, fontSize = 28.sp,
                            fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, letterSpacing = (-1).sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(roleAccent.copy(alpha = 0.12f))
                                .border(0.5.dp, roleAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(5.dp).background(roleAccent, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(roleLabel, color = roleAccent, fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        }
                        if (profile.bio.isNotEmpty()) {
                            Text(profile.bio, color = Color.Gray, fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp), maxLines = 2)
                        }
                    }
                }

                if (isArtistRole) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = onNavigateToCrearObra,
                                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = roleAccent)) {
                                Icon(Icons.Outlined.AddCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Nueva obra", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = onNavigateToArtistDashboard,
                                modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, roleAccent.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground)) {
                                Icon(Icons.Outlined.Dashboard, null, tint = roleAccent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mi Taller", color = roleAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab, containerColor = Color.Transparent,
                        edgePadding = 20.dp, divider = { },
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = roleAccent, height = 2.dp
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = selectedTab == i, onClick = { selectedTab = i },
                                text = {
                                    Text(text = title,
                                        color = if (selectedTab == i) Color.White else Color.Gray.copy(alpha = 0.6f),
                                        fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp)
                                }
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 300.dp)) {
                        if (isArtistRole) {
                            ArtistTabContent(
                                tabName = tabs[selectedTab], accentColor = roleAccent,
                                onNavigateToMisObras = onNavigateToMisObras, onNavigateToMiTienda = onNavigateToMiTienda,
                                onNavigateToCrearObra = onNavigateToCrearObra, onNavigateToVentas = onNavigateToVentas
                            )
                        } else {
                            VisitorTabContent(
                                tabName      = tabs[selectedTab],
                                accentColor  = roleAccent,
                                savedItems   = savedItems,
                                savedLoading = savedLoading,
                                comprasState = comprasState
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = settingsSheetState, containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = roleAccent) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                    Text("CONFIGURACIÓN", color = roleAccent,
                        style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                    Spacer(Modifier.height(20.dp))
                    ProfileSettingRow(Icons.Outlined.Edit, "Editar perfil") {
                        scope.launch { settingsSheetState.hide(); showSettingsSheet = false }
                        onNavigateToSettings()
                    }
                    if (isArtistRole) {
                        ProfileSettingRow(Icons.Outlined.Palette, "Mi Taller") {
                            scope.launch { settingsSheetState.hide(); showSettingsSheet = false }
                            onNavigateToArtistDashboard()
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ProfileSettingRow(Icons.Outlined.Logout, "Cerrar sesión", isDestructive = true) { onLogout() }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB ARTISTA
// ─────────────────────────────────────────────────────────────

@Composable
private fun ArtistTabContent(
    tabName               : String,
    accentColor           : Color,
    onNavigateToMisObras  : () -> Unit,
    onNavigateToMiTienda  : () -> Unit,
    onNavigateToCrearObra : () -> Unit,
    onNavigateToVentas    : () -> Unit
) {
    val items = listOf(
        ArtistQuickItem(Icons.Outlined.Image,     "Mis Obras",         "Gestiona tu catálogo publicado",   null,    onNavigateToMisObras),
        ArtistQuickItem(Icons.Outlined.Store,     "Mi Tienda Pop",     "Productos, stock y precios",       null,    onNavigateToMiTienda),
        ArtistQuickItem(Icons.Outlined.AddCircle, "Subir Nueva Obra",  "Publicar arte, producto o evento", "NUEVO", onNavigateToCrearObra),
        ArtistQuickItem(Icons.Outlined.BarChart,  "Gestión de Ventas", "Órdenes pendientes y confirmadas", null,    onNavigateToVentas)
    )
    val activeIndex = when (tabName) { "Mis Obras" -> 0; "Mi Tienda" -> 1; "Crear Obra" -> 2; "Ventas" -> 3; else -> -1 }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { idx, item ->
            val isActive = idx == activeIndex
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(if (isActive) accentColor.copy(alpha = 0.1f) else CardBackground)
                    .border(1.dp, if (isActive) accentColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
                    .clickable { item.onClick() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (isActive) accentColor.copy(alpha = 0.15f) else BackgroundDeep),
                    contentAlignment = Alignment.Center) {
                    Icon(item.icon, null, tint = if (isActive) accentColor else Color.Gray, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        item.badge?.let {
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(accentColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(it, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                    Text(item.subtitle, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
                Icon(Icons.Outlined.ChevronRight, null,
                    tint = if (isActive) accentColor else Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

private data class ArtistQuickItem(
    val icon    : androidx.compose.ui.graphics.vector.ImageVector,
    val title   : String,
    val subtitle: String,
    val badge   : String?,
    val onClick : () -> Unit
)

// ─────────────────────────────────────────────────────────────
//  TAB VISITANTE
// ─────────────────────────────────────────────────────────────

@Composable
private fun VisitorTabContent(
    tabName      : String,
    accentColor  : Color,
    savedItems   : List<com.proyecto.popayancultural.data.models.SavedItem> = emptyList(),
    savedLoading : Boolean = false,
    comprasState : ComprasState = ComprasState.Loading
) {
    // 🔥 Estado del modal de recibo/ticket — vive aquí para que CompraCard pueda abrirlo
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    when (tabName) {

        "Guardados" -> {
            if (savedLoading) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (savedItems.isEmpty()) {
                EmptyTabPlaceholder("NO HAY GUARDADOS AUN", accentColor)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedItems) { item -> SavedItemCard(item = item, accentColor = accentColor) }
                }
            }
        }

        "Compras" -> {
            when (val s = comprasState) {
                is ComprasState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accentColor)
                    }
                }
                is ComprasState.Error -> EmptyTabPlaceholder("NO HAY COMPRAS AUN", accentColor)
                is ComprasState.Success -> {
                    if (s.orders.isEmpty()) {
                        EmptyTabPlaceholder("NO HAY COMPRAS AUN", accentColor)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.orders) { order ->
                                CompraCard(
                                    order       = order,
                                    accentColor = accentColor,
                                    onClick     = { selectedOrder = order }
                                )
                            }
                        }
                    }
                }
            }
        }

        else -> EmptyTabPlaceholder("NO HAY ${tabName.uppercase()} AUN", accentColor)
    }

    // 🔥 Modal de recibo: QR si es evento, botón PDF si es producto
    selectedOrder?.let { orden ->
        CompraDetalleModal(
            orden     = orden,
            onDismiss = { selectedOrder = null }
        )
    }
}

// ─── Card guardado ────────────────────────────────────────────

@Composable
private fun SavedItemCard(
    item       : com.proyecto.popayancultural.data.models.SavedItem,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(CardBackground).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.coverImage ?: item.savable?.imageUrl ?: "",
            contentDescription = null,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(item.author?.name ?: "", color = Color.Gray, fontSize = 11.sp, maxLines = 1)
        }
        Icon(Icons.Outlined.Bookmark, null, tint = accentColor, modifier = Modifier.size(18.dp))
    }
}

// ─── Card compra ──────────────────────────────────────────────
// 🔥 FIX: ahora es clickable y dispara onClick para abrir el modal de QR/PDF

@Composable
private fun CompraCard(order: Order, accentColor: Color, onClick: () -> Unit) {
    val statusColor = when (order.status) {
        "confirmed" -> Color(0xFF10B981)
        "delivered" -> Color(0xFF6366F1)
        "cancelled" -> Color(0xFFEF4444)
        else        -> Color(0xFFF59E0B)
    }
    val esEvento = order.orderType == "event"

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(CardBackground).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (esEvento) Icons.Outlined.ConfirmationNumber else Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(order.orderNumber.ifEmpty { "#${order.id}" }, color = Color.White,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f))) {
                Text(order.statusLabel.uppercase(), color = statusColor, fontSize = 8.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(Modifier.height(8.dp))
        order.displayItems.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${item.quantity}x ${item.name}", color = Color.Gray,
                    fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("$${String.format("%.0f", item.subtotal)}", color = Color.White,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("TOTAL", color = Color.Gray, fontSize = 9.sp,
                fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("$${String.format("%.0f", order.totalAmount)}", color = accentColor,
                fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (esEvento) Icons.Outlined.QrCode2 else Icons.Outlined.FileDownload,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (esEvento) "Toca para ver tu código QR" else "Toca para descargar el PDF",
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Modal de recibo: QR (evento) o descarga PDF (producto) ──────────────────

@Composable
private fun CompraDetalleModal(orden: Order, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val esEvento = orden.orderType == "event"
    var isGenerating by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "RECIBO DE ADQUISICIÓN",
                        color      = Color.White,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle  = FontStyle.Italic
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar",
                            tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(orden.orderNumber, color = VioletAcento, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))

                orden.displayItems.forEach { item -> OrdenItemRowProfile(item = item) }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("TOTAL", color = Color.Gray, fontSize = 10.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(formatCOPProfile(orden.totalAmount), color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (esEvento) {
                    QrTicketBoxProfile(codigo = orden.orderNumber)
                } else {
                    Button(
                        onClick = {
                            isGenerating = true
                            pdfError = null
                            try {
                                val uri = generarPdfReciboProfile(context, orden)
                                abrirPdfProfile(context, uri)
                            } catch (e: Exception) {
                                pdfError = "No se pudo generar el comprobante."
                            } finally {
                                isGenerating = false
                            }
                        },
                        enabled  = !isGenerating,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = VioletAcento)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.FileDownload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DESCARGAR PDF", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                    pdfError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color(0xFFEF4444), fontSize = 12.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun QrTicketBoxProfile(codigo: String) {
    val qrBitmap: Bitmap? = remember(codigo) {
        runCatching { QRCode.ofSquares().build(codigo).render().nativeImage() as Bitmap }.getOrNull()
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("MUESTRA ESTE CÓDIGO EN LA ENTRADA", color = Color.Gray, fontSize = 9.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier.size(160.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR: $codigo",
                    modifier = Modifier.size(130.dp)
                )
            } else {
                Icon(Icons.Outlined.QrCode2, "QR no disponible", tint = Color.Black, modifier = Modifier.size(60.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(codigo, color = VioletAcento, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
    }
}

private fun generarPdfReciboProfile(context: android.content.Context, orden: Order): android.net.Uri {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply { color = AndroidColor.BLACK; textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val labelPaint = Paint().apply { color = AndroidColor.DKGRAY; textSize = 11f }
    val itemPaint  = Paint().apply { color = AndroidColor.BLACK; textSize = 12f }
    val totalPaint = Paint().apply { color = AndroidColor.BLACK; textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val linePaint  = Paint().apply { color = AndroidColor.LTGRAY; strokeWidth = 1f }

    var y = 50f
    canvas.drawText("POPAYÁN CULTURAL", 40f, y, titlePaint)
    y += 22f
    canvas.drawText("Orden: ${orden.orderNumber}", 40f, y, labelPaint)
    y += 16f
    canvas.drawText("Estado: ${orden.statusLabel}", 40f, y, labelPaint)
    y += 24f
    canvas.drawLine(40f, y, 555f, y, linePaint)
    y += 24f

    orden.displayItems.forEach { item ->
        canvas.drawText(item.name, 40f, y, itemPaint)
        canvas.drawText("x${item.quantity}", 380f, y, itemPaint)
        canvas.drawText(formatCOPProfile(item.subtotal), 460f, y, itemPaint)
        y += 22f
    }

    y += 10f
    canvas.drawLine(40f, y, 555f, y, linePaint)
    y += 28f
    canvas.drawText("TOTAL", 40f, y, totalPaint)
    canvas.drawText(formatCOPProfile(orden.totalAmount), 460f, y, totalPaint)

    pdf.finishPage(page)

    val cacheDir = File(context.cacheDir, "recibos").apply { mkdirs() }
    val file = File(cacheDir, "Recibo_${orden.orderNumber}.pdf")
    FileOutputStream(file).use { pdf.writeTo(it) }
    pdf.close()

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun abrirPdfProfile(context: android.content.Context, uri: android.net.Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Composable
private fun OrdenItemRowProfile(item: OrderItemDisplay) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name.uppercase(), color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text("x${item.quantity}", color = Color.Gray, fontSize = 9.sp)
        }
        Text(formatCOPProfile(item.subtotal), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

private fun formatCOPProfile(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$${format.format(amount)}"
}

// ─── Empty placeholder ────────────────────────────────────────

@Composable
private fun EmptyTabPlaceholder(label: String, accentColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AutoAwesomeMosaic, null,
            tint = Color.White.copy(alpha = 0.05f), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(label, color = Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp, letterSpacing = 3.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Button(onClick = { }, shape = RoundedCornerShape(35.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
            modifier = Modifier.border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(35.dp))) {
            Text("EXPLORAR", color = accentColor, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SETTING ROW
// ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileSettingRow(
    icon         : androidx.compose.ui.graphics.vector.ImageVector,
    title        : String,
    isDestructive: Boolean = false,
    onClick      : () -> Unit
) {
    val color = if (isDestructive) Color(0xFFEF4444) else Color.White
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }.padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (isDestructive) color else VioletAcento, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}