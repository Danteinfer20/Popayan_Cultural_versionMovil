package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.models.SavedItem
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.FavoritosState
import com.proyecto.popayancultural.ui.viewmodels.UserDashboardViewModel
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
//  FAVORITOS SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FavoritosScreen(
    viewModel       : UserDashboardViewModel,
    onNavigateToObra: (Int) -> Unit = {}
) {
    val state        by viewModel.favoritosState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarFavoritos() }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            FavoritosHeader()
            Spacer(modifier = Modifier.height(24.dp))

            when (val s = state) {
                is FavoritosState.Loading -> FavoritosLoadingGrid()
                is FavoritosState.Error   -> FavoritosEmptyState()
                is FavoritosState.Success -> {
                    if (s.items.isEmpty()) {
                        FavoritosEmptyState()
                    } else {
                        FavoritosGrid(
                            items      = s.items,
                            onEliminar = { viewModel.eliminarFavorito(it) },
                            onVerObra  = onNavigateToObra
                        )
                    }
                }
            }
        }

        // ── TOAST ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = toastMessage != null,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            toastMessage?.let { msg ->
                Surface(
                    shape          = RoundedCornerShape(16.dp),
                    color          = CardBackground,
                    border         = BorderStroke(1.dp, VioletAcento.copy(alpha = 0.3f)),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, null,
                            tint = VioletAcento, modifier = Modifier.size(14.dp))
                        Text(msg.uppercase(), color = Color.White,
                            fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    }
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun FavoritosHeader() {
    Column {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.AutoAwesome, null,
                tint = VioletAcento, modifier = Modifier.size(10.dp))
            Text("ARCHIVO DE CURADURÍA PRIVADA", color = VioletAcento,
                fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("OBRAS GUARDADAS.", color = Color.White, fontSize = 28.sp,
            fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, letterSpacing = (-1).sp)
    }
}

// ─── Grid ────────────────────────────────────────────────────────────────────

@Composable
private fun FavoritosGrid(
    items     : List<SavedItem>,
    onEliminar: (Int) -> Unit,
    onVerObra : (Int) -> Unit
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        contentPadding        = PaddingValues(bottom = 100.dp)
    ) {
        items(items, key = { it.id }) { fav ->
            fav.savable?.let { obra ->
                FavoritoCard(
                    imageUrl   = obra.imageUrl ?: "",
                    title      = obra.title    ?: "",
                    artistName = obra.author?.name ?: "Maestro Caucano",
                    obraId     = obra.id,
                    // FIX: usamos fav.savableId para que toggleSaved encuentre el registro correcto
                    onEliminar = { onEliminar(fav.savableId) },
                    onVer      = { onVerObra(obra.id) }
                )
            }
        }
    }
}

// ─── Tarjeta individual ───────────────────────────────────────────────────────

@Composable
private fun FavoritoCard(
    imageUrl  : String,
    title     : String,
    artistName: String,
    obraId    : Int,
    onEliminar: () -> Unit,
    onVer     : () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVer() },   // FIX: tap en toda la card navega a la obra
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column {
            // ── Imagen ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model              = imageUrl,
                    contentDescription = title,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CardBackground),
                                startY = 60f
                            )
                        )
                )
                // Badge "Curada"
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape    = RoundedCornerShape(20.dp),
                    color    = CardBackground.copy(alpha = 0.85f),
                    border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(Modifier.size(5.dp).background(VioletAcento, CircleShape))
                        Text("CURADA", color = Color.White, fontSize = 6.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                // FIX: botón eliminar — visible y funcional en móvil
                IconButton(
                    onClick  = onEliminar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Outlined.Delete, "Eliminar de favoritos",
                        tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                }
            }

            // ── Info ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Palette, null,
                        tint = VioletAcento, modifier = Modifier.size(8.dp))
                    Text(artistName.uppercase(), color = VioletAcento,
                        fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(4.dp))
                Text(title.uppercase(), color = Color.White, fontSize = 11.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.3).sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("REF-$obraId", color = Color.Gray,
                        fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    // FIX: botón ojo también navega a la obra
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(BackgroundDeep, CircleShape)
                            .border(1.dp, VioletAcento.copy(alpha = 0.3f), CircleShape)
                            .clickable { onVer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.RemoveRedEye, "Ver obra",
                            tint = VioletAcento, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun FavoritosLoadingGrid() {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp)
    ) {
        items(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
            )
        }
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
private fun FavoritosEmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.FavoriteBorder, null,
                tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("BÓVEDA VACÍA", color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        }
    }
}