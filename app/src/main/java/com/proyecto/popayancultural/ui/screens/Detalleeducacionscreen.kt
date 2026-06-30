package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.SubcomposeAsyncImage
import com.proyecto.popayancultural.data.RetrofitClient
import com.proyecto.popayancultural.data.models.Education
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
private val Green      = Color(0xFF22C55E)
private val Amber      = Color(0xFFF59E0B)
private val Rose       = Color(0xFFF43F5E)
// ─────────────────────────────────────────────────────────────────────────────

// ─── ViewModel ───────────────────────────────────────────────────────────────
sealed class EducacionUiState {
    object Loading : EducacionUiState()
    data class Success(val leccion: Education) : EducacionUiState()
    data class Error(val message: String) : EducacionUiState()
}

class DetalleEducacionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EducacionUiState>(EducacionUiState.Loading)
    val uiState: StateFlow<EducacionUiState> = _uiState

    fun cargar(id: Int) {
        if (_uiState.value is EducacionUiState.Success) return
        viewModelScope.launch {
            _uiState.value = EducacionUiState.Loading
            try {
                val response = RetrofitClient.apiService.getEducationById(id)
                val leccion  = response.body()?.data
                if (response.isSuccessful && leccion != null) {
                    _uiState.value = EducacionUiState.Success(leccion)
                } else {
                    _uiState.value = EducacionUiState.Error("Lección no encontrada")
                }
            } catch (e: Exception) {
                _uiState.value = EducacionUiState.Error("Error al cargar la lección")
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────

// ─── Helper: detectar tipo de URL de video ───────────────────────────────────
private fun isYouTubeUrl(url: String) =
    url.contains("youtube.com") || url.contains("youtu.be")

private fun isVimeoUrl(url: String) =
    url.contains("vimeo.com")

private fun isDirectVideo(url: String) =
    url.endsWith(".mp4", ignoreCase = true) ||
            url.endsWith(".webm", ignoreCase = true) ||
            url.endsWith(".mkv", ignoreCase = true) ||
            url.contains("/stream") ||
            url.contains("/video/")

private fun youTubeThumb(url: String): String {
    val id = when {
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        url.contains("v=")        -> url.substringAfter("v=").substringBefore("&")
        else                      -> ""
    }
    return if (id.isNotBlank()) "https://img.youtube.com/vi/$id/hqdefault.jpg" else ""
}
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetalleEducacionScreen(
    id        : Int,
    onBack    : () -> Unit,
    viewModel : DetalleEducacionViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(id) { viewModel.cargar(id) }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when (val s = uiState.value) {

            is EducacionUiState.Loading -> {
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Violet)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cargando lección...",
                        color         = Violet,
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            is EducacionUiState.Error -> {
                Column(
                    modifier            = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = Violet.copy(0.4f), modifier = Modifier.size(48.dp))
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

            is EducacionUiState.Success -> {
                val leccion    = s.leccion
                val autor      = leccion.author
                val meta       = leccion.metadata
                val diffConfig = difficultyConfig(meta?.difficultyLevel)
                val videoUrl   = leccion.videoUrl

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                    // ── Bloque de video o hero imagen ─────────────────────────
                    if (!videoUrl.isNullOrBlank()) {
                        when {
                            // Video directo (.mp4 etc) → ExoPlayer embebido
                            isDirectVideo(videoUrl) -> {
                                VideoPlayer(
                                    url      = videoUrl,
                                    onBack   = onBack
                                )
                            }
                            // YouTube → thumbnail + botón que abre la app/navegador
                            isYouTubeUrl(videoUrl) -> {
                                ExternalVideoHero(
                                    thumbnailUrl = youTubeThumb(videoUrl),
                                    plataforma   = "YouTube",
                                    icon         = Icons.Outlined.PlayCircle,
                                    onBack       = onBack,
                                    onPlay       = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        )
                                    }
                                )
                            }
                            // Vimeo u otro externo → mismo flujo
                            isVimeoUrl(videoUrl) -> {
                                ExternalVideoHero(
                                    thumbnailUrl = leccion.coverImage,
                                    plataforma   = "Vimeo",
                                    icon         = Icons.Outlined.PlayCircle,
                                    onBack       = onBack,
                                    onPlay       = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        )
                                    }
                                )
                            }
                            // URL desconocida → intentamos abrir externamente
                            else -> {
                                ExternalVideoHero(
                                    thumbnailUrl = leccion.coverImage,
                                    plataforma   = "Video",
                                    icon         = Icons.Outlined.PlayCircle,
                                    onBack       = onBack,
                                    onPlay       = {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        // Sin video → hero con imagen estática
                        ImageHero(
                            imageUrl   = leccion.coverImage,
                            title      = leccion.title,
                            diffConfig = diffConfig,
                            onBack     = onBack
                        )
                    }

                    // ── Info ──────────────────────────────────────────────────
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                        // Área de conocimiento
                        if (!meta?.knowledgeArea.isNullOrBlank()) {
                            Surface(
                                shape  = RoundedCornerShape(6.dp),
                                color  = VioletSoft,
                                border = BorderStroke(1.dp, Violet.copy(0.3f))
                            ) {
                                Text(
                                    meta!!.knowledgeArea!!.uppercase(),
                                    fontSize      = 9.sp,
                                    color         = Violet,
                                    fontWeight    = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Título
                        Text(
                            leccion.title,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Black,
                            color      = TextHigh,
                            lineHeight = 28.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        // Duración + dificultad
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, null, tint = Violet, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${meta?.estimatedReadTime ?: 5} min", fontSize = 12.sp, color = TextMid)
                            }
                            Surface(
                                shape  = RoundedCornerShape(6.dp),
                                color  = diffConfig.bg,
                                border = BorderStroke(1.dp, diffConfig.color.copy(0.3f))
                            ) {
                                Text(
                                    diffConfig.label,
                                    fontSize   = 9.sp,
                                    color      = diffConfig.color,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Autor ────────────────────────────────────────────
                        if (autor != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(14.dp),
                                color    = Card,
                                border   = BorderStroke(1.dp, Stroke)
                            ) {
                                Row(
                                    modifier          = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier         = Modifier.size(40.dp).clip(CircleShape).background(VioletSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (autor.avatar != null) {
                                            SubcomposeAsyncImage(
                                                model              = autor.avatar,
                                                contentDescription = autor.name,
                                                modifier           = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale       = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                autor.name?.firstOrNull()?.uppercase() ?: "M",
                                                color      = Violet,
                                                fontSize   = 16.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("CURADURÍA", fontSize = 9.sp, color = Violet, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        Text(autor.name ?: "Maestro Anónimo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        autor.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp).clip(CircleShape)
                                                    .background(Color(0xFF052E16))
                                                    .border(1.dp, Green.copy(0.3f), CircleShape)
                                                    .clickable {
                                                        val clean = phone.replace(Regex("[^0-9]"), "")
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")))
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) { Icon(Icons.Outlined.Chat, null, tint = Green, modifier = Modifier.size(16.dp)) }
                                        }
                                        autor.email?.takeIf { it.isNotBlank() }?.let { email ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp).clip(CircleShape)
                                                    .background(VioletSoft)
                                                    .border(1.dp, Violet.copy(0.3f), CircleShape)
                                                    .clickable {
                                                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) { Icon(Icons.Outlined.Email, null, tint = Violet, modifier = Modifier.size(16.dp)) }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        HorizontalDivider(color = Stroke)
                        Spacer(Modifier.height(16.dp))

                        // ── Extracto ─────────────────────────────────────────
                        if (!leccion.excerpt.isNullOrBlank()) {
                            Text("Sobre esta lección", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Violet, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(leccion.excerpt, fontSize = 14.sp, color = TextMid, lineHeight = 22.sp)
                            Spacer(Modifier.height(16.dp))
                        }

                        // ── Contenido ─────────────────────────────────────────
                        if (!leccion.content.isNullOrBlank()) {
                            Text("Contenido", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Violet, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            val textoLimpio = leccion.content
                                .replace(Regex("<[^>]*>"), " ")
                                .replace(Regex("\\s+"), " ")
                                .trim()
                            Text(textoLimpio, fontSize = 14.sp, color = TextMid, lineHeight = 22.sp)
                            Spacer(Modifier.height(16.dp))
                        }

                        // ── Ficha técnica ─────────────────────────────────────
                        val ficha = listOfNotNull(
                            meta?.knowledgeArea?.let { "Área" to it },
                            meta?.difficultyLevel?.let { "Nivel" to difficultyConfig(it).label },
                            meta?.estimatedReadTime?.let { "Duración" to "$it min" },
                            meta?.historicalPeriod?.let { "Período" to it },
                            meta?.categoryName?.let { "Categoría" to it }
                        )
                        if (ficha.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(14.dp),
                                color    = CardAlt,
                                border   = BorderStroke(1.dp, Stroke)
                            ) {
                                Column(
                                    modifier            = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Ficha técnica", fontSize = 11.sp, color = Violet, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    ficha.forEach { (label, value) ->
                                        Row {
                                            Text(label, fontSize = 12.sp, color = TextMid, modifier = Modifier.width(100.dp))
                                            Text(value, fontSize = 12.sp, color = TextHigh, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        // ── Botón volver ──────────────────────────────────────
                        OutlinedButton(
                            onClick  = onBack,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = BorderStroke(1.dp, Violet.copy(0.4f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Violet)
                        ) {
                            Icon(Icons.Outlined.ArrowBackIosNew, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Volver al archivo", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

// ─── Reproductor ExoPlayer embebido (video directo) ──────────────────────────
@Composable
private fun VideoPlayer(url: String, onBack: () -> Unit) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Botón back encima del player
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .padding(12.dp)
                .statusBarsPadding()
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(0.55f))
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, "Volver", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Hero para YouTube / Vimeo / externos (thumbnail + botón abrir) ──────────
@Composable
private fun ExternalVideoHero(
    thumbnailUrl : String?,
    plataforma   : String,
    icon         : androidx.compose.ui.graphics.vector.ImageVector,
    onBack       : () -> Unit,
    onPlay       : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.Black)
    ) {
        // Thumbnail
        if (!thumbnailUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model              = thumbnailUrl,
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))
                )
            )
        }

        // Overlay oscuro
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f))
        )

        // Botón back
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .padding(12.dp)
                .statusBarsPadding()
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(0.55f))
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, "Volver", tint = Color.White, modifier = Modifier.size(18.dp))
        }

        // Botón play central
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Violet)
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(0.6f)
            ) {
                Text(
                    "Ver en $plataforma",
                    fontSize   = 11.sp,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ─── Hero imagen estática (sin video) ────────────────────────────────────────
@Composable
private fun ImageHero(
    imageUrl   : String?,
    title      : String,
    diffConfig : DiffConfig,
    onBack     : () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        SubcomposeAsyncImage(
            model              = imageUrl,
            contentDescription = title,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(CardAlt)) },
            error   = {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.MenuBook, null, tint = Violet.copy(0.3f), modifier = Modifier.size(64.dp))
                }
            }
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent, Bg.copy(0.9f), Bg))
            )
        )
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(0.55f))
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, "Volver", tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
            shape    = RoundedCornerShape(20.dp),
            color    = diffConfig.bg,
            border   = BorderStroke(1.dp, diffConfig.color.copy(0.4f))
        ) {
            Text(
                diffConfig.label,
                fontSize   = 9.sp,
                color      = diffConfig.color,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

// ─── Helper de dificultad ─────────────────────────────────────────────────────
private data class DiffConfig(val label: String, val color: Color, val bg: Color)

private fun difficultyConfig(level: String?): DiffConfig = when (level?.lowercase()) {
    "beginner"     -> DiffConfig("Básico",     Green, Color(0xFF052E16))
    "intermediate" -> DiffConfig("Intermedio", Amber, Color(0xFF1A1000))
    "advanced"     -> DiffConfig("Avanzado",   Rose,  Color(0xFF1A0010))
    else           -> DiffConfig("General",    Violet, VioletDeep)
}