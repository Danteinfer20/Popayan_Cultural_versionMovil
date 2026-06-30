package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.ArtistDashboardViewModel
import com.proyecto.popayancultural.ui.viewmodels.RecentWork
import java.text.NumberFormat
import java.util.Locale

private val ArtistRed     = Color(0xFFEF4444)
private val ArtistRedDim  = Color(0xFFEF4444).copy(alpha = 0.12f)

// ─────────────────────────────────────────────────────────────
//  ARTIST DASHBOARD SCREEN
// ─────────────────────────────────────────────────────────────

@Composable
fun ArtistDashboardScreen(
    artistName           : String = "Artista",
    artistAvatar         : String = "",
    onNavigateToCreate   : () -> Unit = {},
    onNavigateToStore    : () -> Unit = {},
    onNavigateToGallery  : () -> Unit = {},
    onNavigateToSales    : () -> Unit = {},
    onNavigateToSettings : () -> Unit = {},
    viewModel            : ArtistDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    // Snackbar para errores
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = BackgroundDeep,
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Text(
                        text     = data.visuals.message,
                        color    = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── HEADER ───────────────────────────────────────────
            DashboardHeader(
                name     = artistName,
                avatar   = artistAvatar,
                onSettings = onNavigateToSettings
            )

            Spacer(Modifier.height(28.dp))

            // ── KPI CARDS ────────────────────────────────────────
            SectionLabel(text = "MÉTRICAS")
            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                KpiSkeleton()
            } else {
                KpiGrid(kpis = uiState.kpis)
            }

            Spacer(Modifier.height(28.dp))

            // ── ACCIONES RÁPIDAS ─────────────────────────────────
            SectionLabel(text = "OPERACIONES")
            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Acción principal
                Button(
                    onClick  = onNavigateToCreate,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = ArtistRed)
                ) {
                    Icon(Icons.Outlined.AddCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("SUBIR NUEVA OBRA", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                }

                // Acciones secundarias
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryAction(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Outlined.Store,
                        label     = "MI TIENDA",
                        onClick   = onNavigateToStore
                    )
                    SecondaryAction(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Outlined.Image,
                        label     = "MIS OBRAS",
                        onClick   = onNavigateToGallery
                    )
                    SecondaryAction(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Outlined.BarChart,
                        label     = "VENTAS",
                        onClick   = onNavigateToSales
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── ÚLTIMOS MOVIMIENTOS ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                SectionLabel(text = "ÚLTIMOS MOVIMIENTOS")
                TextButton(onClick = onNavigateToGallery) {
                    Text(
                        "VER TODO",
                        color      = ArtistRed,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        Icons.Outlined.ChevronRight,
                        null,
                        tint     = ArtistRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            if (uiState.isLoading) {
                WorksSkeleton()
            } else if (uiState.recentWorks.isEmpty()) {
                EmptyWorks(onNavigateToCreate)
            } else {
                RecentWorksList(works = uiState.recentWorks)
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    name      : String,
    avatar    : String,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0808), BackgroundDeep)
                )
            )
    ) {
        // Glow decorativo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(ArtistRed.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .border(2.dp, ArtistRed.copy(alpha = 0.35f), CircleShape)
            ) {
                AsyncImage(
                    model = avatar.ifEmpty {
                        "https://ui-avatars.com/api/?name=$name&background=1A0808&color=ef4444"
                    },
                    contentDescription = null,
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Badge rol
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArtistRedDim)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(5.dp).background(ArtistRed, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "ARTISTA VERIFICADO",
                        color     = ArtistRed,
                        fontSize  = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = "Hola, $name",
                    color      = Color.White,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle  = FontStyle.Italic
                )
                Text(
                    text     = "Panel del Maestro Artesano",
                    color    = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón settings
            IconButton(
                onClick  = onSettings,
                modifier = Modifier
                    .size(42.dp)
                    .background(CardBackground.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    null,
                    tint     = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  KPI GRID
// ─────────────────────────────────────────────────────────────

@Composable
private fun KpiGrid(kpis: com.proyecto.popayancultural.ui.viewmodels.ArtistKpis) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Outlined.AutoAwesome,
                label    = "Obras",
                value    = "${kpis.totalWorks}"
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Outlined.FavoriteBorder,
                label    = "Comunidad",
                value    = "${kpis.featuredWorks}"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Outlined.ShoppingBag,
                label    = "Ventas",
                value    = "${kpis.salesCount}"
            )
            KpiCard(
                modifier    = Modifier.weight(1f),
                icon        = Icons.Outlined.TrendingUp,
                label       = "Recaudación",
                value       = formatCOP(kpis.totalRevenue),
                valueColor  = ArtistRed
            )
        }
    }
}

@Composable
private fun KpiCard(
    modifier   : Modifier,
    icon       : ImageVector,
    label      : String,
    value      : String,
    valueColor : Color = Color.White
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBackground),
        border   = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ArtistRedDim),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = ArtistRed, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text      = label,
                color     = Color.Gray,
                fontSize  = 10.sp,
                letterSpacing = 0.3.sp
            )
            Text(
                text       = value,
                color      = valueColor,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ACCIÓN SECUNDARIA
// ─────────────────────────────────────────────────────────────

@Composable
private fun SecondaryAction(
    modifier : Modifier,
    icon     : ImageVector,
    label    : String,
    onClick  : () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(52.dp),
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = ArtistRed, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(3.dp))
            Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  LISTA DE OBRAS RECIENTES
// ─────────────────────────────────────────────────────────────

@Composable
private fun RecentWorksList(works: List<RecentWork>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {
            works.forEachIndexed { idx, work ->
                RecentWorkRow(work = work)
                if (idx < works.lastIndex) {
                    HorizontalDivider(
                        color     = Color.White.copy(alpha = 0.04f),
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentWorkRow(work: RecentWork) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Miniatura
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BackgroundDeep),
            contentAlignment = Alignment.Center
        ) {
            if (work.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model        = work.imageUrl,
                    contentDescription = null,
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Image,
                    null,
                    tint     = Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = work.title,
                color      = Color.White,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = work.createdAt,
                color    = Color.Gray,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        // Badge estado
        val (label, bg, tc) = when (work.status) {
            "published" -> Triple("PUBLICADO", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
            "draft"     -> Triple("BORRADOR",  Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
            else        -> Triple(work.status.uppercase(), Color.Gray.copy(alpha = 0.1f), Color.Gray)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(label, color = tc, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyWorks(onNavigateToCreate: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ArtistRedDim),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Palette,
                        null,
                        tint     = ArtistRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "SIN OBRAS AÚN",
                    color     = Color.White,
                    fontSize  = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Sube tu primera obra y empieza\na construir tu legado cultural",
                    color    = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToCreate,
                    shape   = RoundedCornerShape(24.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = ArtistRed),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SUBIR OBRA", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SKELETONS (loading)
// ─────────────────────────────────────────────────────────────

@Composable
private fun KpiSkeleton() {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue   = 0.3f,
        targetValue    = 0.7f,
        animationSpec  = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label          = "alpha"
    )
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardBackground.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun WorksSkeleton() {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "alpha"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundDeep.copy(alpha = alpha))
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text      = text,
        color     = Color.Gray,
        fontSize  = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier  = Modifier.padding(horizontal = 16.dp)
    )
}

private fun formatCOP(amount: Double): String =
    "\$${NumberFormat.getNumberInstance(Locale("es", "CO")).format(amount.toLong())}"