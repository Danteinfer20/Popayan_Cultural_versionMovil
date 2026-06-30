package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage

// ─── Design Tokens ────────────────────────────
private val Bg         = Color(0xFF080808)
private val Card       = Color(0xFF111115)
private val CardAlt    = Color(0xFF16161A)
private val Stroke     = Color(0xFF222228)
private val Violet     = Color(0xFFA855F7)
private val VioletSoft = Color(0xFF2D1A4A)
private val TextHigh   = Color(0xFFFFFFFF)
private val TextMid    = Color(0xFF9CA3AF)
private val TextLow    = Color(0xFF4B5563)
private val Red        = Color(0xFFEF4444)
// ──────────────────────────────────────────────

@Composable
fun DetalleObraScreen(
    slug        : String,
    onBack      : () -> Unit,
    onAutorClick: (String) -> Unit = {},
    isLoggedIn  : Boolean = false,
    viewModel   : DetalleObraViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val acciones    by viewModel.acciones.collectAsState()
    val comentarios by viewModel.comentarios.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context     = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar obra al entrar
    LaunchedEffect(slug) {
        viewModel.cargar(slug)
    }

    // Mostrar errores del ViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when (val s = uiState) {
            is DetalleObraUiState.Loading ->
                ObraShimmer()
            is DetalleObraUiState.Error ->
                ObraError(s.message, onBack)
            is DetalleObraUiState.Success ->
                ObraBody(
                    obra         = s.obra,
                    acciones     = acciones,
                    comentarios  = comentarios,
                    isLoggedIn   = isLoggedIn,
                    onBack       = onBack,
                    onAutorClick = onAutorClick,
                    onReaccion   = { viewModel.toggleReaccion() },
                    onColeccion  = { viewModel.toggleColeccion() },
                    onRepost     = {
                        viewModel.registrarRepost()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Mira esta obra en Vive Larte: https://vivelarte.com/obra/${viewModel.slugActual}"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir obra"))
                    },
                    onTexto   = { viewModel.onTextoComentario(it) },
                    onEnviar  = { viewModel.enviarComentario() }
                )
        }

        // Snackbar para errores
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ObraBody(
    obra        : com.proyecto.popayancultural.data.models.Post,
    acciones    : AccionesState,
    comentarios : ComentariosState,
    isLoggedIn  : Boolean,
    onBack      : () -> Unit,
    onAutorClick: (String) -> Unit,
    onReaccion  : () -> Unit,
    onColeccion : () -> Unit,
    onRepost    : () -> Unit,
    onTexto     : (String) -> Unit,
    onEnviar    : () -> Unit
) {
    var descripcionExpandida by remember { mutableStateOf(false) }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            HeroObra(imageUrl = obra.imageUrl, titulo = obra.title, onBack = onBack)
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))

                obra.category?.let { cat ->
                    Surface(
                        shape  = RoundedCornerShape(6.dp),
                        color  = VioletSoft,
                        border = BorderStroke(1.dp, Violet.copy(0.3f))
                    ) {
                        Text(
                            cat.name.uppercase(),
                            fontSize      = 9.sp,
                            color         = Violet,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text(obra.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextHigh, lineHeight = 30.sp)
                Spacer(Modifier.height(12.dp))

                obra.author?.let { autor ->
                    val avatarUrl = autor.avatar?.takeIf { it.isNotBlank() }
                        ?: "https://ui-avatars.com/api/?name=${autor.name.replace(" ", "+")}&background=2D1A4A&color=A855F7&size=64"
                    Row(
                        modifier          = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAutorClick(autor.username) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model              = avatarUrl,
                            contentDescription = autor.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.size(34.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(autor.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextHigh)
                            Text("@${autor.username}", fontSize = 12.sp, color = Violet)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, null, tint = TextLow, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stats
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    StatChip(Icons.Outlined.FavoriteBorder, "${acciones.reacciones}", acciones.reaccionado)
                    StatChip(Icons.Outlined.Visibility, "${acciones.vistas}", false)
                    StatChip(Icons.Outlined.ChatBubbleOutline, "${comentarios.lista.size}", false)
                    StatChip(Icons.Outlined.Share, "${acciones.reposts}", false)
                }

                Spacer(Modifier.height(16.dp))

                // Acciones
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccionBtn(
                        icon     = if (acciones.coleccionado) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        label    = if (acciones.coleccionado) "Guardado" else "Guardar",
                        activo   = acciones.coleccionado,
                        cargando = acciones.cargandoColeccion,
                        modifier = Modifier.weight(1f),
                        onClick  = onColeccion
                    )
                    AccionBtn(
                        icon     = if (acciones.reaccionado) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        label    = if (acciones.reaccionado) "Te gustó" else "Me gusta",
                        activo   = acciones.reaccionado,
                        cargando = acciones.cargandoReaccion,
                        modifier = Modifier.weight(1f),
                        onClick  = onReaccion
                    )
                    AccionBtn(
                        icon     = Icons.Outlined.Share,
                        label    = "Compartir",
                        activo   = false,
                        cargando = acciones.cargandoRepost,
                        modifier = Modifier.weight(1f),
                        onClick  = onRepost
                    )
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Stroke)
                Spacer(Modifier.height(20.dp))
            }
        }

        obra.excerpt?.takeIf { it.isNotBlank() }?.let { texto ->
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card)
                            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .defaultMinSize(minHeight = 48.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Violet)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text      = texto,
                                fontSize  = 14.sp,
                                color     = TextMid,
                                lineHeight = 22.sp,
                                maxLines  = if (descripcionExpandida) Int.MAX_VALUE else 4,
                                overflow  = TextOverflow.Ellipsis
                            )
                            if (texto.length > 200) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text       = if (descripcionExpandida) "Ver menos" else "Leer más",
                                    fontSize   = 13.sp,
                                    color      = Violet,
                                    fontWeight = FontWeight.Medium,
                                    modifier   = Modifier.clickable { descripcionExpandida = !descripcionExpandida }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = Stroke)
                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // Header comentarios
        item {
            Row(
                modifier          = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Violet, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Comentarios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                if (comentarios.lista.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = VioletSoft) {
                        Text(
                            "${comentarios.lista.size}",
                            fontSize   = 11.sp,
                            color      = Violet,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Input
        item {
            if (!isLoggedIn) {
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = Card,
                    border   = BorderStroke(1.dp, Stroke)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Lock, null, tint = TextLow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Inicia sesión para comentar", fontSize = 13.sp, color = TextMid)
                    }
                }
            } else {
                Row(
                    modifier          = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card)
                            .border(
                                1.dp,
                                if (comentarios.texto.isNotBlank()) Violet.copy(0.5f) else Stroke,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (comentarios.texto.isEmpty()) {
                            Text("Escribe un comentario...", fontSize = 14.sp, color = TextLow)
                        }
                        BasicTextField(
                            value         = comentarios.texto,
                            onValueChange = onTexto,
                            textStyle     = TextStyle(fontSize = 14.sp, color = TextHigh, lineHeight = 20.sp),
                            cursorBrush   = SolidColor(Violet),
                            maxLines      = 4,
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (comentarios.texto.isNotBlank()) Violet else Card)
                            .border(1.dp, if (comentarios.texto.isNotBlank()) Violet else Stroke, CircleShape)
                            .clickable(enabled = comentarios.texto.isNotBlank() && !comentarios.enviando) { onEnviar() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (comentarios.enviando) {
                            CircularProgressIndicator(color = TextHigh, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.AutoMirrored.Outlined.Send,
                                null,
                                tint     = if (comentarios.texto.isNotBlank()) TextHigh else TextLow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Lista comentarios
        if (comentarios.lista.isEmpty()) {
            item {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier         = Modifier.size(52.dp).clip(CircleShape).background(VioletSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Violet, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Sé el primero en comentar", fontSize = 14.sp, color = TextMid, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Tu voz enriquece esta obra", fontSize = 12.sp, color = TextLow)
                }
            }
        } else {
            items(comentarios.lista) { comentario ->
                val fallback   = "https://ui-avatars.com/api/?name=${comentario.autorNombre.replace(" ", "+")}&background=2D1A4A&color=A855F7&size=64"
                val avatarModel = comentario.autorAvatar?.takeIf { it.isNotBlank() } ?: fallback
                Row(
                    modifier          = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    AsyncImage(
                        model              = avatarModel,
                        contentDescription = comentario.autorNombre,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.size(34.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(Card)
                            .border(1.dp, Stroke, RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(comentario.autorNombre, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextHigh)
                            Spacer(Modifier.width(8.dp))
                            Text(comentario.fecha, fontSize = 11.sp, color = TextLow)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(comentario.texto, fontSize = 13.sp, color = TextMid, lineHeight = 19.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun HeroObra(imageUrl: String, titulo: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
        SubcomposeAsyncImage(
            model              = imageUrl.takeIf { it.isNotBlank() },
            contentDescription = titulo,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
            loading = { Box(Modifier.fillMaxSize().background(Card)) },
            error   = {
                Box(
                    modifier         = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D0D10)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Palette, null, tint = Violet.copy(0.3f), modifier = Modifier.size(64.dp))
                }
            }
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colorStops = arrayOf(0f to Color.Transparent, 0.45f to Color.Transparent, 0.78f to Bg.copy(0.75f), 1f to Bg))
            )
        )
        IconButton(
            onClick  = onBack,
            modifier = Modifier.padding(16.dp).size(44.dp).clip(CircleShape).background(Color.Black.copy(0.55f)).align(Alignment.TopStart)
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, "Regresar", tint = TextHigh, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String, activo: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (activo) Violet else TextLow, modifier = Modifier.size(16.dp))
        if (label.isNotBlank()) {
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 13.sp, color = if (activo) Violet else TextMid)
        }
    }
}

@Composable
private fun AccionBtn(
    icon    : ImageVector,
    label   : String,
    activo  : Boolean,
    cargando: Boolean,
    modifier: Modifier,
    onClick : () -> Unit
) {
    Surface(
        modifier = modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).clickable(enabled = !cargando) { onClick() },
        shape    = RoundedCornerShape(10.dp),
        color    = if (activo) VioletSoft else Card,
        border   = BorderStroke(1.dp, if (activo) Violet.copy(0.5f) else Stroke)
    ) {
        Row(
            modifier              = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Violet, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(icon, null, tint = if (activo) Violet else TextMid, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, fontSize = 12.sp, color = if (activo) Violet else TextMid, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ObraShimmer() {
    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        Box(modifier = Modifier.fillMaxWidth().height(340.dp).background(Card))
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
            Box(Modifier.fillMaxWidth(0.85f).height(28.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
            Box(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
            Box(Modifier.fillMaxWidth(0.9f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
            Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(CardAlt))
        }
    }
}

@Composable
private fun ObraError(message: String, onBack: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().background(Bg).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier.size(60.dp).clip(CircleShape).background(Red.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ErrorOutline, null, tint = Red, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(message, color = TextMid, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onBack,
            shape   = RoundedCornerShape(12.dp),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Violet),
            border  = BorderStroke(1.dp, Violet.copy(0.4f))
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Regresar", fontWeight = FontWeight.Medium)
        }
    }
}