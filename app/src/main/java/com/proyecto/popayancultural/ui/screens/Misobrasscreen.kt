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

// ─────────────────────────────────────────────────────────────
//  MODELO LOCAL
// ─────────────────────────────────────────────────────────────

data class ObraItem(
    val id        : Int,
    val title     : String,
    val status    : String,
    val imageUrl  : String,
    val views     : Int,
    val reactions : Int,
    val createdAt : String
)

data class MisObrasUiState(
    val isLoading      : Boolean         = true,
    val obras          : List<ObraItem>  = emptyList(),
    val activeTab      : String          = "published",
    val searchQuery    : String          = "",
    val errorMessage   : String?         = null,
    val kpis           : ObrasKpis       = ObrasKpis(),
    val deleteTargetId : Int?            = null   // id de la obra en confirmación
)

data class ObrasKpis(
    val totalWorks    : Int = 0,
    val featuredWorks : Int = 0,
    val communityReach: Int = 0
)

// ─────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────

class MisObrasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MisObrasUiState())
    val uiState: StateFlow<MisObrasUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.apiService.getMyPosts()
                if (response.isSuccessful) {
                    val posts = response.body()?.data ?: emptyList()
                    val obras = posts.map { post ->
                        ObraItem(
                            id        = post.id,
                            title     = post.title,
                            status    = post.status,
                            imageUrl  = post.imageUrl,
                            views     = post.stats?.views ?: 0,
                            reactions = post.stats?.reactions ?: 0,
                            createdAt = post.publishedAt?.take(10) ?: ""
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            obras     = obras,
                            kpis      = ObrasKpis(
                                totalWorks     = obras.size,
                                featuredWorks  = obras.count { o -> o.status == "published" },
                                communityReach = obras.sumOf { o -> o.views }
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar obras") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de conexión") }
            }
        }
    }

    fun onTabChange(tab: String)       = _uiState.update { it.copy(activeTab = tab) }
    fun onSearchChange(q: String)      = _uiState.update { it.copy(searchQuery = q) }
    fun requestDelete(id: Int)         = _uiState.update { it.copy(deleteTargetId = id) }
    fun cancelDelete()                 = _uiState.update { it.copy(deleteTargetId = null) }

    fun confirmDelete() {
        val id = _uiState.value.deleteTargetId ?: return
        _uiState.update { it.copy(deleteTargetId = null) }
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deletePost(id)
                _uiState.update { it.copy(obras = it.obras.filter { o -> o.id != id }) }
            } catch (e: Exception) { /* ignorar */ }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────

private val ArtistRed = Color(0xFFEF4444)

@Composable
fun MisObrasScreen(
    onBack      : () -> Unit = {},
    onCrearObra : () -> Unit = {},
    onEditarObra: (Int) -> Unit = {},
    viewModel   : MisObrasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    val filteredObras = uiState.obras.filter { obra ->
        obra.status == uiState.activeTab &&
                (uiState.searchQuery.isEmpty() || obra.title.contains(uiState.searchQuery, ignoreCase = true))
    }

    // ── Confirm dialog eliminar ──────────────────────────────
    if (uiState.deleteTargetId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor   = Color(0xFF1A1A22),
            title = {
                Text("¿Eliminar obra?", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text("Esta acción no se puede deshacer.",
                    color = Color.Gray, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = ArtistRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {

        // ── Header ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onBack,
                modifier = Modifier.size(40.dp).background(CardBackground, RoundedCornerShape(12.dp))
            ) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("MIS OBRAS", color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("Catálogo personal", color = Color.Gray, fontSize = 11.sp)
            }
            IconButton(
                onClick  = onCrearObra,
                modifier = Modifier.size(40.dp).background(ArtistRed, RoundedCornerShape(12.dp))
            ) { Icon(Icons.Outlined.Add, null, tint = Color.White) }
        }

        // ── KPI strip ────────────────────────────────────────
        if (!uiState.isLoading) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Total"      to "${uiState.kpis.totalWorks}",
                    "Publicadas" to "${uiState.kpis.featuredWorks}",
                    "Vistas"     to "${uiState.kpis.communityReach}"
                ).forEach { (label, value) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(label, color = Color.Gray, fontSize = 9.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Búsqueda ─────────────────────────────────────────
        OutlinedTextField(
            value         = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder   = { Text("Buscar en el catálogo...", color = Color.Gray, fontSize = 13.sp) },
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

        // ── Tabs ─────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("published" to "Publicadas", "draft" to "Borradores").forEach { (id, label) ->
                val isActive = uiState.activeTab == id
                val count    = uiState.obras.count { it.status == id }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) ArtistRed else CardBackground)
                        .clickable { viewModel.onTabChange(id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "$label ($count)",
                        color      = if (isActive) Color.White else Color.Gray,
                        fontSize   = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Grid ─────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArtistRed)
                }
            }
            filteredObras.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Image, null,
                            tint     = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(60.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Sin obras en esta categoría", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    contentPadding        = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.fillMaxSize()
                ) {
                    items(filteredObras, key = { it.id }) { obra ->
                        ObraCard(
                            obra     = obra,
                            onEdit   = { onEditarObra(obra.id) },
                            onDelete = { viewModel.requestDelete(obra.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun ObraCard(
    obra    : ObraItem,
    onEdit  : () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box {
            if (obra.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model              = obra.imageUrl,
                    contentDescription = null,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale       = ContentScale.Crop
                )
            } else {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(BackgroundDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Image, null,
                        tint     = Color.Gray.copy(0.3f),
                        modifier = Modifier.size(28.dp))
                }
            }

            // Badge estado
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (obra.status == "published") Color(0xFF10B981).copy(0.15f)
                        else Color(0xFFF59E0B).copy(0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    if (obra.status == "published") "PUBLICADO" else "BORRADOR",
                    color         = if (obra.status == "published") Color(0xFF10B981) else Color(0xFFF59E0B),
                    fontSize      = 7.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Botón editar sobre la imagen
            IconButton(
                onClick  = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(30.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Outlined.Edit, null,
                    tint     = Color.White,
                    modifier = Modifier.size(14.dp))
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                obra.title,
                color      = Color.White,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.RemoveRedEye, null,
                            tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("${obra.views}", color = Color.Gray, fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FavoriteBorder, null,
                            tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("${obra.reactions}", color = Color.Gray, fontSize = 10.sp)
                    }
                }
                // Botón eliminar
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, null,
                        tint     = Color(0xFFEF4444).copy(0.6f),
                        modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}