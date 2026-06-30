package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.models.Artist
import com.proyecto.popayancultural.data.models.Education
import com.proyecto.popayancultural.data.models.Post
import com.proyecto.popayancultural.ui.ExploraUiState
import com.proyecto.popayancultural.ui.ExploraViewModel

// ─── Design Tokens ─────────────────────────────────────
private val Bg         = Color(0xFF080808)
private val Card       = Color(0xFF111115)
private val CardAlt    = Color(0xFF16161A)
private val Stroke     = Color(0xFF222228)
private val Violet     = Color(0xFFA855F7)
private val VioletSoft = Color(0xFF2D1A4A)
private val Amber      = Color(0xFFF59E0B)
private val AmberSoft  = Color(0xFF2C1F00)
private val TextHigh   = Color(0xFFFFFFFF)
private val TextMid    = Color(0xFF9CA3AF)
private val TextLow    = Color(0xFF4B5563)
private val Green      = Color(0xFF4ADE80)
private val GreenSoft  = Color(0xFF052E16)
// ───────────────────────────────────────────────────────

private enum class ExploraTab(
    val label: String,
    val icon: ImageVector
) {
    OBRAS("Obras", Icons.Outlined.Palette),
    ARTISTAS("Artistas", Icons.Outlined.People),
    EDUCACION("Aprende", Icons.Outlined.School)
}

@Composable
fun ExploraScreen(
    onObraClick: (String) -> Unit = {},
    onArtistaClick: (String) -> Unit = {},
    onEducacionClick: (Int) -> Unit = {},
    viewModel: ExploraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(ExploraTab.OBRAS) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        ExploraHeader(
            selectedTab    = selectedTab,
            onTabSelected  = { selectedTab = it; searchQuery = "" },
            searchQuery    = searchQuery,
            onSearchChange = { searchQuery = it }
        )

        when (val s = uiState) {
            is ExploraUiState.Loading -> ExploraShimmer()
            is ExploraUiState.Error   -> ExploraError(s.message) { viewModel.loadExploraData() }
            is ExploraUiState.Success -> AnimatedContent(
                targetState    = selectedTab,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally { if (targetState.ordinal > initialState.ordinal) 40 else -40 })
                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { if (targetState.ordinal > initialState.ordinal) -40 else 40 })
                },
                label = "explora_tab"
            ) { tab ->
                when (tab) {
                    ExploraTab.OBRAS     -> ObrasTab(
                        obras   = s.obras.filter { it.title.contains(searchQuery, ignoreCase = true) },
                        onClick = onObraClick
                    )
                    ExploraTab.ARTISTAS  -> ArtistasTab(
                        artistas = s.artistas.filter { it.name.contains(searchQuery, ignoreCase = true) },
                        onClick  = onArtistaClick
                    )
                    ExploraTab.EDUCACION -> EducacionTab(
                        items   = s.educacion.filter { it.title.contains(searchQuery, ignoreCase = true) },
                        onClick = onEducacionClick
                    )
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────
//  HEADER
// ───────────────────────────────────────────────────────
@Composable
private fun ExploraHeader(
    selectedTab: ExploraTab,
    onTabSelected: (ExploraTab) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bg)
            .padding(top = 52.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Explorar", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                Text("Descubre el arte y cultura de Popayán", fontSize = 13.sp, color = TextMid)
            }
            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = VioletSoft,
                border = BorderStroke(1.dp, Violet.copy(0.3f))
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Explore, null, tint = Violet, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Popayán", fontSize = 11.sp, color = Violet, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SearchBar(query = searchQuery, onQueryChange = onSearchChange, selectedTab = selectedTab)
        Spacer(Modifier.height(12.dp))
        TabRow(selectedTab = selectedTab, onTabSelected = onTabSelected)
        HorizontalDivider(color = Stroke, thickness = 1.dp)
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedTab: ExploraTab
) {
    val placeholder = when (selectedTab) {
        ExploraTab.OBRAS     -> "Buscar obras..."
        ExploraTab.ARTISTAS  -> "Buscar artistas..."
        ExploraTab.EDUCACION -> "Buscar contenido..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, if (query.isNotEmpty()) Violet.copy(0.5f) else Stroke, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Search, null,
            tint     = if (query.isNotEmpty()) Violet else TextLow,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(placeholder, fontSize = 14.sp, color = TextLow)
            }
            BasicTextField(
                value         = query,
                onValueChange = onQueryChange,
                textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextHigh),
                cursorBrush   = SolidColor(Violet),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Outlined.Close, null, tint = TextMid, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TabRow(
    selectedTab: ExploraTab,
    onTabSelected: (ExploraTab) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExploraTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Surface(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onTabSelected(tab) },
                shape    = RoundedCornerShape(10.dp),
                color    = if (isSelected) VioletSoft else Color.Transparent,
                border   = if (isSelected) BorderStroke(1.dp, Violet.copy(0.4f)) else null
            ) {
                Row(
                    modifier              = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(tab.icon, null, tint = if (isSelected) Violet else TextLow, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        tab.label,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) Violet else TextLow
                    )
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────
//  TAB OBRAS
// ───────────────────────────────────────────────────────
@Composable
private fun ObrasTab(obras: List<Post>, onClick: (String) -> Unit) {
    if (obras.isEmpty()) {
        EmptyState(Icons.Outlined.Palette, "Sin obras por aquí")
        return
    }

    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        modifier              = Modifier.fillMaxSize(),
        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            ObraCardFeatured(obra = obras.first(), onClick = onClick)
        }
        items(obras.drop(1)) { obra ->
            ObraCard(obra = obra, onClick = onClick)
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ObraCardFeatured(obra: Post, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Stroke, RoundedCornerShape(16.dp))
            .clickable { onClick(obra.safeSlug) }  // ✅ FIX
    ) {
        AsyncImage(
            model              = obra.imageUrl.ifBlank { null },
            contentDescription = obra.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.75f)))
            )
        )
        Surface(
            modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
            shape    = RoundedCornerShape(6.dp),
            color    = Violet.copy(0.85f)
        ) {
            Text(
                "DESTACADA",
                fontSize      = 8.sp,
                color         = TextHigh,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            obra.category?.let {
                Text(it.name.uppercase(), fontSize = 9.sp, color = Violet, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            }
            Text(obra.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHigh, maxLines = 2, overflow = TextOverflow.Ellipsis)
            obra.author?.let {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, tint = TextMid, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it.name, fontSize = 11.sp, color = TextMid)
                }
            }
        }
    }
}

@Composable
private fun ObraCard(obra: Post, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Stroke, RoundedCornerShape(14.dp))
            .clickable { onClick(obra.safeSlug) }  // ✅ FIX
    ) {
        AsyncImage(
            model              = obra.imageUrl.ifBlank { null },
            contentDescription = obra.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)))
            )
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            Text(obra.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextHigh, maxLines = 2, overflow = TextOverflow.Ellipsis)
            obra.author?.let {
                Text(it.name, fontSize = 10.sp, color = TextMid, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        obra.stats?.let { stats ->
            if (stats.reactions > 0) {
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    shape    = RoundedCornerShape(20.dp),
                    color    = Color.Black.copy(0.6f)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.FavoriteBorder, null, tint = Violet, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${stats.reactions}", fontSize = 10.sp, color = TextMid)
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────
//  TAB ARTISTAS
// ───────────────────────────────────────────────────────
@Composable
private fun ArtistasTab(artistas: List<Artist>, onClick: (String) -> Unit) {
    if (artistas.isEmpty()) {
        EmptyState(Icons.Outlined.People, "Sin artistas por aquí")
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(artistas) { artista -> ArtistaCard(artista = artista, onClick = onClick) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ArtistaCard(artista: Artist, onClick: (String) -> Unit) {
    val fallbackUrl = "https://ui-avatars.com/api/?name=${artista.name.replace(" ", "+")}&background=2D1A4A&color=A855F7&size=128"

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick(artista.username) },
        shape    = RoundedCornerShape(16.dp),
        color    = Card,
        border   = BorderStroke(1.dp, Stroke)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(58.dp)) {
                AsyncImage(
                    model              = artista.avatar?.takeIf { it.isNotBlank() } ?: fallbackUrl,
                    contentDescription = artista.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp).clip(CircleShape).background(Bg)
                        .padding(2.dp).clip(CircleShape).background(Green)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artista.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                Text("@${artista.username}", fontSize = 12.sp, color = Violet)
                artista.location?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, tint = TextLow, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(it, fontSize = 11.sp, color = TextLow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                artista.bio?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontSize = 12.sp, color = TextMid, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = TextLow, modifier = Modifier.size(20.dp))
        }
    }
}

// ───────────────────────────────────────────────────────
//  TAB EDUCACIÓN
// ───────────────────────────────────────────────────────
@Composable
private fun EducacionTab(items: List<Education>, onClick: (Int) -> Unit) {
    if (items.isEmpty()) {
        EmptyState(Icons.Outlined.School, "Sin contenido educativo aún")
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { edu -> EducacionCard(edu = edu, onClick = onClick) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun EducacionCard(edu: Education, onClick: (Int) -> Unit) {
    val levelColor = when (edu.levelLabel) {
        "Básico"     -> Green
        "Intermedio" -> Amber
        "Avanzado"   -> Color(0xFFEF4444)
        else         -> Green
    }
    val levelBg = when (edu.levelLabel) {
        "Básico"     -> GreenSoft
        "Intermedio" -> AmberSoft
        "Avanzado"   -> Color(0xFF2D0A0A)
        else         -> GreenSoft
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick(edu.id) },
        shape    = RoundedCornerShape(16.dp),
        color    = Card,
        border   = BorderStroke(1.dp, Stroke)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(110.dp).fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            ) {
                if (edu.imageUrl != null) {
                    AsyncImage(
                        model              = edu.imageUrl,
                        contentDescription = edu.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D0D18)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayCircle, null, tint = Violet.copy(0.4f), modifier = Modifier.size(32.dp))
                    }
                }
                if (edu.videoUrl != null) {
                    Box(
                        modifier         = Modifier.size(30.dp).clip(CircleShape)
                            .background(Color.Black.copy(0.65f)).align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, tint = TextHigh, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = levelBg, border = BorderStroke(1.dp, levelColor.copy(0.3f))) {
                        Text(edu.levelLabel, fontSize = 8.sp, color = levelColor, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = CardAlt, border = BorderStroke(1.dp, Stroke)) {
                        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, null, tint = TextLow, modifier = Modifier.size(9.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(edu.durationLabel, fontSize = 8.sp, color = TextLow)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(edu.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextHigh, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
                edu.excerpt?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(5.dp))
                    Text(it, fontSize = 12.sp, color = TextMid, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                }
                edu.metadata?.categoryName?.let { cat ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Tag, null, tint = Violet, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(cat, fontSize = 10.sp, color = Violet)
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────
//  ESTADOS AUXILIARES
// ───────────────────────────────────────────────────────
@Composable
private fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier.size(64.dp).clip(CircleShape).background(VioletSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Violet, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(message, fontSize = 15.sp, color = TextMid, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("Vuelve pronto para descubrir más", fontSize = 13.sp, color = TextLow)
    }
}

@Composable
private fun ExploraError(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF2D0A0A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(message, fontSize = 14.sp, color = TextMid, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onRetry,
            shape   = RoundedCornerShape(12.dp),
            border  = BorderStroke(1.dp, Violet.copy(0.5f)),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Violet)
        ) {
            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reintentar", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ExploraShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Card))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(14.dp)).background(Card))
            Box(Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(14.dp)).background(Card))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(14.dp)).background(Card))
            Box(Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(14.dp)).background(Card))
        }
    }
}