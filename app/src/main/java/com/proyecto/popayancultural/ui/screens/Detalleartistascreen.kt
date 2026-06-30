package com.proyecto.popayancultural.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.Artist
import com.proyecto.popayancultural.data.models.Post
import com.proyecto.popayancultural.data.models.Product
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val Bg         = Color(0xFF080808)
private val Card       = Color(0xFF111115)
private val CardAlt    = Color(0xFF16161A)
private val Stroke     = Color(0xFF222228)
private val Violet     = Color(0xFFA855F7)
private val VioletSoft = Color(0xFF2D1A4A)
private val VioletDeep = Color(0xFF1A0A2E)
private val TextHigh   = Color(0xFFFFFFFF)
private val TextMid    = Color(0xFF9CA3AF)
private val TextLow    = Color(0xFF4B5563)
// ─────────────────────────────────────────────────────────────────────────────

// ─── Estados UI ──────────────────────────────────────────────────────────────
sealed class ArtistaUiState {
    object Loading : ArtistaUiState()
    data class Success(
        val artista  : Artist,
        val obras    : List<Post>,
        val productos: List<Product>
    ) : ArtistaUiState()
    data class Error(val message: String) : ArtistaUiState()
}
// ─────────────────────────────────────────────────────────────────────────────

// ─── ViewModel ───────────────────────────────────────────────────────────────
class DetalleArtistaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistaUiState>(ArtistaUiState.Loading)
    val uiState: StateFlow<ArtistaUiState> = _uiState

    // Estado local de seguimiento (optimistic update)
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    fun cargar(username: String) {
        if (_uiState.value is ArtistaUiState.Success) return
        viewModelScope.launch {
            _uiState.value = ArtistaUiState.Loading
            try {
                val api        = RetrofitClient.apiService
                val artistaRes = api.getArtistByUsername(username)

                if (!artistaRes.isSuccessful || artistaRes.body()?.data == null) {
                    _uiState.value = ArtistaUiState.Error("Artista no encontrado")
                    return@launch
                }

                val artista = artistaRes.body()!!.data!!
                _isFollowing.value = artista.isFollowingByMe

                // ✅ FIX: getPosts sin type= para traer obras Y eventos del usuario
                val obrasDef = async {
                    runCatching {
                        api.getPosts(userId = artista.id, limit = 20)
                    }.getOrNull()
                }
                val productosDef = async {
                    runCatching {
                        api.getProducts(userId = artista.id, limit = 20)
                    }.getOrNull()
                }

                val obras     = obrasDef.await()?.body()?.data     ?: emptyList()
                val productos = productosDef.await()?.body()?.data ?: emptyList()

                _uiState.value = ArtistaUiState.Success(artista, obras, productos)

            } catch (e: Exception) {
                _uiState.value = ArtistaUiState.Error("Error al cargar el perfil")
            }
        }
    }

    fun toggleSeguir() {
        _isFollowing.value = !_isFollowing.value
        // TODO: llamar endpoint follow/unfollow cuando esté disponible en el backend
    }
}
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetalleArtistaScreen(
    username       : String,
    onBack         : () -> Unit,
    onObraClick    : (String) -> Unit = {},
    onProductClick : (Int) -> Unit    = {},
    viewModel      : DetalleArtistaViewModel = viewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    LaunchedEffect(username) { viewModel.cargar(username) }

    var tabSeleccionado by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when (val s = uiState) {

            is ArtistaUiState.Loading -> {
                CircularProgressIndicator(
                    color    = Violet,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ArtistaUiState.Error -> {
                Column(
                    modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        null,
                        tint     = Violet.copy(0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(s.message, color = TextMid, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = onBack,
                        shape   = RoundedCornerShape(12.dp),
                        border  = BorderStroke(1.dp, Violet.copy(0.4f)),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Violet)
                    ) { Text("Volver") }
                }
            }

            is ArtistaUiState.Success -> {
                val artista   = s.artista
                val obras     = s.obras
                val productos = s.productos

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // ── Hero: cover + avatar ──────────────────────────────────
                    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {

                        // Cover photo
                        if (!artista.coverPicture.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model              = artista.coverPicture,
                                contentDescription = null,
                                modifier           = Modifier.fillMaxSize(),
                                contentScale       = ContentScale.Crop,
                                loading = {
                                    Box(Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(listOf(VioletDeep, Bg))
                                    ))
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(listOf(VioletDeep, Color(0xFF0D0010), Bg))
                                )
                            )
                        }

                        // Gradiente inferior para legibilidad del avatar
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.6f to Color.Black.copy(0.3f),
                                        1.0f to Bg
                                    )
                                )
                            )
                        )

                        // Botón back
                        IconButton(
                            onClick  = onBack,
                            modifier = Modifier
                                .padding(16.dp)
                                .statusBarsPadding()
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.5f))
                                .align(Alignment.TopStart)
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBackIosNew,
                                "Volver",
                                tint     = TextHigh,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Avatar centrado en la parte baja del hero
                        Box(
                            modifier         = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Anillo exterior violeta
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Violet, Color(0xFF7C3AED)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .size(98.dp)
                                        .clip(CircleShape)
                                        .background(VioletDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!artista.avatar.isNullOrBlank()) {
                                        SubcomposeAsyncImage(
                                            model              = artista.avatar,
                                            contentDescription = artista.name,
                                            modifier           = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale       = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            artista.name.take(2).uppercase(),
                                            color      = Violet,
                                            fontSize   = 30.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Badge verificado
                            if (artista.isVerified) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Violet)
                                        .border(2.dp, Bg, CircleShape)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-2).dp, y = (-2).dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Check,
                                        null,
                                        tint     = TextHigh,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Info del perfil ───────────────────────────────────────
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(12.dp))

                        // Badge de rol
                        Surface(
                            shape  = RoundedCornerShape(20.dp),
                            color  = VioletSoft,
                            border = BorderStroke(1.dp, Violet.copy(0.3f))
                        ) {
                            Text(
                                artista.roleLabel,
                                fontSize      = 9.sp,
                                color         = Violet,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier      = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Nombre
                        Text(
                            artista.name,
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Black,
                            color      = TextHigh,
                            textAlign  = TextAlign.Center
                        )

                        // Username
                        Text(
                            "@${artista.username}",
                            fontSize   = 11.sp,
                            color      = Violet,
                            fontWeight = FontWeight.Bold
                        )

                        // Ubicación
                        if (artista.displayLocation.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    null,
                                    tint     = Violet,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(artista.displayLocation, fontSize = 12.sp, color = TextMid)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Stats
                        Surface(
                            shape  = RoundedCornerShape(14.dp),
                            color  = Card,
                            border = BorderStroke(1.dp, Stroke)
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                StatItem(obras.size.toString(), "Obras")
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Stroke))
                                StatItem(artista.followerCount.toString(), "Seguidores")
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Stroke))
                                StatItem(productos.size.toString(), "Productos")
                            }
                        }

                        // Bio
                        if (!artista.bio.isNullOrBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "\"${artista.bio}\"",
                                fontSize   = 13.sp,
                                color      = TextMid,
                                lineHeight = 20.sp,
                                textAlign  = TextAlign.Center,
                                fontStyle  = FontStyle.Italic
                            )
                        }

                        // Website
                        if (!artista.website.isNullOrBlank()) {
                            val uriHandler = LocalUriHandler.current
                            // Mostrar solo "dominio.com" sin path ni parámetros
                            val websiteLabel = runCatching {
                                artista.website
                                    .removePrefix("https://")
                                    .removePrefix("http://")
                                    .removePrefix("www.")
                                    .substringBefore("/")
                                    .substringBefore("?")
                            }.getOrDefault(artista.website)

                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    runCatching { uriHandler.openUri(artista.website) }
                                }
                            ) {
                                Icon(Icons.Outlined.Link, null, tint = Violet, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(websiteLabel, fontSize = 12.sp, color = Violet)
                            }
                        }

                        // Redes sociales
                        val redes = artista.socialMedia?.filterValues { it.isNotBlank() }
                        if (!redes.isNullOrEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            SocialMediaRow(redes)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botones acción
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Botón Seguir
                            Button(
                                onClick = { viewModel.toggleSeguir() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape  = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) Card else Violet,
                                    contentColor   = if (isFollowing) Violet else TextHigh
                                ),
                                border = if (isFollowing) BorderStroke(1.dp, Violet.copy(0.4f)) else null
                            ) {
                                Icon(
                                    if (isFollowing) Icons.Outlined.PersonRemove else Icons.Outlined.PersonAdd,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (isFollowing) "Siguiendo" else "Seguir",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Botón Compartir
                            OutlinedButton(
                                onClick  = { /* TODO: share */ },
                                modifier = Modifier.size(44.dp),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(1.dp, Stroke),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextMid),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Stroke)
                        Spacer(Modifier.height(16.dp))

                        // Tabs
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Obras maestras", "Pop Store").forEachIndexed { i, label ->
                                val isSelected = tabSeleccionado == i
                                Surface(
                                    onClick  = { tabSeleccionado = i },
                                    shape    = RoundedCornerShape(10.dp),
                                    color    = if (isSelected) Violet else Card,
                                    border   = BorderStroke(1.dp, if (isSelected) Violet else Stroke),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        label,
                                        color      = if (isSelected) TextHigh else TextMid,
                                        fontSize   = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign  = TextAlign.Center,
                                        modifier   = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Grid de contenido
                        val itemsActuales: List<Any> = if (tabSeleccionado == 0) obras else productos

                        if (itemsActuales.isEmpty()) {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Card)
                                    .border(1.dp, Stroke, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (tabSeleccionado == 0) Icons.Outlined.Palette else Icons.Outlined.ShoppingBag,
                                        null,
                                        tint     = Violet.copy(0.3f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (tabSeleccionado == 0) "Sin obras registradas" else "Sin piezas comerciales",
                                        color    = TextLow,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                itemsActuales.chunked(2).forEach { fila ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        fila.forEach { item ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                when (item) {
                                                    is Post    -> MiniObraCard(item)    { onObraClick(item.safeSlug) }
                                                    is Product -> MiniProductCard(item) { onProductClick(item.id) }
                                                }
                                            }
                                        }
                                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

// ─── Redes sociales ───────────────────────────────────────────────────────────
// El backend guarda handles ("Reef-coral") o URLs completas.
// Esta función normaliza ambos casos a una URL apta para abrir.
private fun buildSocialUrl(red: String, value: String): String {
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    val handle = value.trimStart('@')
    return when (red.lowercase()) {
        "instagram" -> "https://instagram.com/$handle"
        "facebook"  -> "https://facebook.com/$handle"
        "twitter", "x" -> "https://x.com/$handle"
        "youtube"   -> "https://youtube.com/@$handle"
        "tiktok"    -> "https://tiktok.com/@$handle"
        "whatsapp"  -> "https://wa.me/${handle.replace(Regex("[^0-9]"), "")}"
        else        -> "https://$value"
    }
}

@Composable
private fun SocialMediaRow(redes: Map<String, String>) {
    val uriHandler = LocalUriHandler.current

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        redes.forEach { (red, value) ->
            val icon = when (red.lowercase()) {
                "instagram"        -> Icons.Outlined.CameraAlt
                "facebook"         -> Icons.Outlined.Groups
                "twitter", "x"     -> Icons.Outlined.Tag
                "youtube"          -> Icons.Outlined.PlayCircle
                "tiktok"           -> Icons.Outlined.MusicNote
                "whatsapp"         -> Icons.Outlined.Chat
                else               -> Icons.Outlined.Link
            }
            val label = when (red.lowercase()) {
                "instagram" -> "IG"
                "facebook"  -> "FB"
                "twitter"   -> "X"
                "x"         -> "X"
                "youtube"   -> "YT"
                "tiktok"    -> "TK"
                "whatsapp"  -> "WA"
                else        -> red.take(2).uppercase()
            }
            val url = buildSocialUrl(red, value)

            Surface(
                onClick = { runCatching { uriHandler.openUri(url) } },
                shape   = RoundedCornerShape(10.dp),
                color   = VioletSoft,
                border  = BorderStroke(1.dp, Violet.copy(0.3f)),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(icon, null, tint = Violet, modifier = Modifier.size(14.dp))
                    Text(label, fontSize = 11.sp, color = Violet, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Sub-componentes ─────────────────────────────────────────────────────────
@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextHigh)
        Text(
            label,
            fontSize      = 9.sp,
            color         = TextMid,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun MiniObraCard(obra: Post, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model              = obra.imageUrl.takeIf { it.isNotBlank() },
            contentDescription = obra.title.orEmpty(),
            modifier           = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(CardAlt)) },
            error   = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Image, null, tint = Violet.copy(0.3f), modifier = Modifier.size(28.dp))
                }
            }
        )
        Text(
            obra.title.orEmpty(),
            color      = TextHigh,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun MiniProductCard(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model              = product.imageUrl.takeIf { it.isNotBlank() },
            contentDescription = product.name,
            modifier           = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(CardAlt)) },
            error   = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ShoppingBag, null, tint = Violet.copy(0.3f), modifier = Modifier.size(28.dp))
                }
            }
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                product.name,
                color      = TextHigh,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Text(product.displayPrice, color = Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}