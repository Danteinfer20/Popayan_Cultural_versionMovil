package com.proyecto.popayancultural.ui.screens.agenda

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.proyecto.popayancultural.data.models.EventDetail
import com.proyecto.popayancultural.data.models.UserAttendance
import com.proyecto.popayancultural.ui.AttendUiState
import com.proyecto.popayancultural.ui.EventDetailUiState
import com.proyecto.popayancultural.ui.EventDetailViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import qrcode.QRCode
import java.io.File

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
private val GreenBg    = Color(0xFF052E16)
private val Red        = Color(0xFFEF4444)
// ───────────────────────────────────────────────────────

private fun parseIsoDate(isoStr: String): java.util.Date? = runCatching {
    val clean = isoStr.replace("T", " ").take(19)
    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
        .parse(clean)
}.getOrNull()

private fun formatLongDate(isoStr: String): String = runCatching {
    val date = parseIsoDate(isoStr) ?: return isoStr
    java.text.SimpleDateFormat("d 'de' MMMM 'de' yyyy", java.util.Locale.forLanguageTag("es"))
        .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
        .format(date)
}.getOrDefault(isoStr)

private fun formatShortDate(isoStr: String): String = runCatching {
    val date = parseIsoDate(isoStr) ?: return isoStr
    java.text.SimpleDateFormat("d MMM", java.util.Locale.forLanguageTag("es"))
        .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
        .format(date)
}.getOrDefault(isoStr)

private fun extractTime(isoStr: String): String = runCatching {
    val date = parseIsoDate(isoStr)
    if (date != null) {
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .apply { timeZone = java.util.TimeZone.getTimeZone("America/Bogota") }
            .format(date)
    } else {
        isoStr.take(5)
    }
}.getOrDefault(isoStr.take(5))

@Composable
fun DetalleEventoScreen(
    eventId: Int,
    onBack: () -> Unit,
    onOrganizerClick: (String) -> Unit,
    isLoggedIn: Boolean = true,
    viewModel: EventDetailViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val attendState by viewModel.attendState.collectAsState()
    var showTicketDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }
    LaunchedEffect(attendState) {
        if (attendState is AttendUiState.Success) showTicketDialog = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when (val s = detailState) {
            is EventDetailUiState.Loading ->
                ShimmerLoading()
            is EventDetailUiState.Error   ->
                ErrorState(s.message, onBack)
            is EventDetailUiState.Success ->
                DetailBody(
                    event            = s.event,
                    attendState      = attendState,
                    isLoggedIn       = isLoggedIn,
                    onBack           = onBack,
                    onOrganizerClick = onOrganizerClick,
                    onAttend         = { viewModel.confirmAttendance(eventId, s.event.isFree) }
                )
        }

        if (showTicketDialog && attendState is AttendUiState.Success) {
            TicketDialog(
                attendance = (attendState as AttendUiState.Success).attendance,
                onDismiss  = { showTicketDialog = false; viewModel.resetAttendState() }
            )
        }
    }
}

@Composable
private fun DetailBody(
    event: EventDetail,
    attendState: AttendUiState,
    isLoggedIn: Boolean,
    onBack: () -> Unit,
    onOrganizerClick: (String) -> Unit,
    onAttend: () -> Unit
) {
    val scroll           = rememberScrollState()
    val alreadyAttending = event.userAttendance?.status == "confirmed"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.verticalScroll(scroll)) {
            HeroSection(event = event, onBack = onBack)

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 4.dp)
            ) {
                CategoryPill()
                Spacer(Modifier.height(8.dp))
                Text(
                    text       = event.title,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextHigh,
                    lineHeight = 30.sp
                )

                Spacer(Modifier.height(14.dp))
                ChipsRow(event)
                Spacer(Modifier.height(24.dp))

                event.description?.takeIf { it.isNotBlank() }?.let {
                    DescriptionBlock(it)
                    Spacer(Modifier.height(20.dp))
                }

                DetailsCard(event)
                Spacer(Modifier.height(16.dp))

                AttendanceBlock(
                    event            = event,
                    attendState      = attendState,
                    isLoggedIn       = isLoggedIn,
                    alreadyAttending = alreadyAttending,
                    onAttend         = onAttend
                )
                Spacer(Modifier.height(16.dp))

                event.organizer?.let { org ->
                    OrganizerRow(
                        name     = org.name,
                        username = org.username,
                        photoUrl = org.profilePhoto ?: org.profilePicture,
                        role     = org.role ?: "Organizador",
                        onClick  = { onOrganizerClick(org.username) }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ─── LÓGICA HÍBRIDA DE MAPA ───
                val lat = event.latitude ?: event.location?.latitude
                val lng = event.longitude ?: event.location?.longitude
                val addressStr = event.location?.address ?: ""

                if (lat != null && lng != null) {
                    // Mapa real en coordenadas
                    MapBlock(
                        locationName = event.location?.name ?: "Ubicación del evento",
                        address = addressStr.takeIf { !it.startsWith("http", ignoreCase = true) },
                        lat = lat,
                        lng = lng
                    )
                } else if (addressStr.startsWith("http", ignoreCase = true)) {
                    // Fallback a enlace de Google Maps
                    LinkMapBlock(
                        locationName = event.location?.name ?: "Ubicación del evento",
                        url = addressStr
                    )
                } else {
                    // Sin mapa y sin enlace
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardAlt,
                        border = BorderStroke(1.dp, Stroke)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Map, null, tint = TextMid, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Ubicación no disponible para este evento.",
                                fontSize = 12.sp,
                                color = TextMid,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeroSection(event: EventDetail, onBack: () -> Unit) {
    val finalCoverUrl = remember(event.coverImage) {
        event.coverImage?.takeIf { it.isNotBlank() }
            ?: "https://placehold.co/600x300/0D0D18/A855F7?text="
    }

    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        SubcomposeAsyncImage(
            model              = ImageRequest.Builder(LocalContext.current)
                .data(finalCoverUrl).crossfade(true).build(),
            contentDescription = event.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
            loading = { Box(Modifier.fillMaxSize().background(Card)) },
            error   = {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D0D10)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Image, null,
                        tint     = Violet.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f    to Color.Transparent,
                        0.4f  to Color.Transparent,
                        0.75f to Bg.copy(alpha = 0.7f),
                        1f    to Bg
                    )
                )
            )
        )

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, "Regresar", tint = TextHigh, modifier = Modifier.size(18.dp))
        }

        Surface(
            modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
            shape    = RoundedCornerShape(20.dp),
            color    = Color.Black.copy(alpha = 0.55f)
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.People, null, tint = TextHigh, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("${event.attendanceCount} asistentes", fontSize = 12.sp, color = TextHigh, fontWeight = FontWeight.Medium)
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            PriceBadge(isFree = event.isFree, price = event.price)
        }
    }
}

@Composable
private fun CategoryPill() {
    Surface(
        shape  = RoundedCornerShape(6.dp),
        color  = VioletSoft,
        border = BorderStroke(1.dp, Violet.copy(0.3f))
    ) {
        Text(
            "EVENTO CULTURAL",
            fontSize      = 9.sp,
            color         = Violet,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PriceBadge(isFree: Boolean, price: Double?) {
    val bg    = if (isFree) GreenBg else AmberSoft
    val fg    = if (isFree) Green   else Amber
    val label = if (isFree) "Entrada libre" else "$ ${"%.0f".format(price ?: 0.0)}"
    val icon  = if (isFree) Icons.Outlined.LocalActivity else Icons.Outlined.AttachMoney

    Surface(shape = RoundedCornerShape(10.dp), color = bg, border = BorderStroke(1.dp, fg.copy(0.4f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, fontSize = 13.sp, color = fg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChipsRow(event: EventDetail) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniChip(Icons.Outlined.CalendarToday, formatShortDate(event.startDate))
        val hora = if (event.startDate.contains("T")) extractTime(event.startDate)
        else event.startTime.take(5)
        MiniChip(Icons.Outlined.AccessTime, hora)
        event.location?.let {
            MiniChip(Icons.Outlined.LocationOn, it.name, maxWidth = 130.dp)
        }
    }
}

@Composable
private fun MiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    maxWidth: androidx.compose.ui.unit.Dp = 120.dp
) {
    Surface(shape = RoundedCornerShape(20.dp), color = Card, border = BorderStroke(1.dp, Stroke)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Amber, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, fontSize = 11.sp, color = TextMid, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = maxWidth))
        }
    }
}

@Composable
private fun DescriptionBlock(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, Stroke, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).defaultMinSize(minHeight = 48.dp).clip(RoundedCornerShape(2.dp)).background(Violet))
        Spacer(Modifier.width(14.dp))
        Text(text = text, fontSize = 14.sp, color = TextMid, lineHeight = 22.sp)
    }
}

@Composable
private fun DetailsCard(event: EventDetail) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Stroke)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DETALLES", fontSize = 9.sp, letterSpacing = 1.5.sp, color = TextLow, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            DetailLine(Icons.Outlined.CalendarToday, "Fecha", formatLongDate(event.startDate))
            Divider()

            val horaInicio = if (event.startDate.contains("T")) extractTime(event.startDate)
            else event.startTime.take(5)
            val horaFin = event.endDate?.let { if (it.contains("T")) extractTime(it) else null }
                ?: event.endTime?.take(5)
            val horaLabel = if (horaFin != null) "$horaInicio  →  $horaFin" else horaInicio
            DetailLine(Icons.Outlined.AccessTime, "Hora", horaLabel)

            event.location?.let {
                Divider()
                DetailLine(Icons.Outlined.LocationOn, "Lugar", it.name)
                // Ocultamos la URL cruda si es un enlace, ya que la manejamos visualmente en LinkMapBlock
                it.address?.takeIf { addr -> !addr.startsWith("http", ignoreCase = true) }?.let { addr ->
                    Spacer(Modifier.height(2.dp))
                    Text(addr, fontSize = 11.sp, color = TextLow, modifier = Modifier.padding(start = 32.dp))
                }
            }
            event.capacity?.let { cap ->
                Divider()
                DetailLine(Icons.Outlined.People, "Aforo", "$cap personas")
            }
        }
    }
}

@Composable
private fun DetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, tint = Amber, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 9.sp, color = TextLow, letterSpacing = 0.8.sp)
            Text(value, fontSize = 14.sp, color = TextHigh, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = Stroke, modifier = Modifier.padding(vertical = 10.dp))
}

@Composable
private fun AttendanceBlock(
    event: EventDetail,
    attendState: AttendUiState,
    isLoggedIn: Boolean,
    alreadyAttending: Boolean,
    onAttend: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Card, border = BorderStroke(1.dp, Stroke)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            if (alreadyAttending && event.userAttendance != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Acceso confirmado", color = Green, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(16.dp))
                QrBox(size = 120.dp, iconSize = 90.dp, ticketCode = event.userAttendance.ticketCode ?: event.userAttendance.qrCode)
            } else {
                val isLoading = attendState is AttendUiState.Loading

                Text("¿ASISTIRÁS?", fontSize = 9.sp, letterSpacing = 1.5.sp, color = TextLow, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))

                Button(
                    onClick  = onAttend,
                    enabled  = isLoggedIn && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = if (event.isFree) Violet else Amber,
                        contentColor           = if (event.isFree) TextHigh else Color.Black,
                        disabledContainerColor = (if (event.isFree) Violet else Amber).copy(0.35f),
                        disabledContentColor   = (if (event.isFree) TextHigh else Color.Black).copy(0.4f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = if (event.isFree) TextHigh else Color.Black,
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.ConfirmationNumber, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                !isLoggedIn  -> "Inicia sesión para asistir"
                                event.isFree -> "Obtener mi entrada"
                                else         -> "Comprar entrada"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp
                        )
                    }
                }

                if (attendState is AttendUiState.Error) {
                    Spacer(Modifier.height(10.dp))
                    Text(attendState.message, color = Red, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun OrganizerRow(
    name: String,
    username: String,
    photoUrl: String?,
    role: String,
    onClick: () -> Unit
) {
    val fallbackAvatar = "https://ui-avatars.com/api/?name=${Uri.encode(name)}&background=A855F7&color=fff&size=96"
    val finalPhotoModel = remember(photoUrl) {
        photoUrl?.takeIf { it.isNotBlank() && it.startsWith("http") } ?: fallbackAvatar
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        color    = Card,
        border   = BorderStroke(1.dp, Stroke)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model              = finalPhotoModel,
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.size(46.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("@$username", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = VioletSoft) {
                        Text(
                            "ORGANIZADOR",
                            fontSize      = 8.sp,
                            color         = Violet,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier      = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(role, fontSize = 12.sp, color = TextMid)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = TextLow, modifier = Modifier.size(20.dp))
        }
    }
}

// ───────────────────────────────────────────────────────
//  NUEVO COMPONENTE: TARJETA DE ENLACE A MAPS
// ───────────────────────────────────────────────────────
@Composable
private fun LinkMapBlock(locationName: String, url: String) {
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = Amber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ubicación del evento", fontSize = 14.sp, color = TextHigh, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            color = Card,
            border = BorderStroke(1.dp, Stroke)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(VioletSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Map, null, tint = Violet, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(locationName, fontSize = 15.sp, color = TextHigh, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Toca para abrir en Google Maps", fontSize = 12.sp, color = Violet)
                }
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, tint = TextMid, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ───────────────────────────────────────────────────────
//  COMPONENTE ORIGINAL: MAPA OSMDROID
// ───────────────────────────────────────────────────────
@Composable
private fun MapBlock(locationName: String, address: String?, lat: Double, lng: Double) {
    val context = LocalContext.current

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = Amber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ubicación del evento", fontSize = 14.sp, color = TextHigh, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = {
                val geoUri    = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(locationName)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply { setPackage("com.google.android.apps.maps") }
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    val webUri = Uri.parse("https://www.openstreetmap.org/?mlat=$lat&mlon=$lng#map=17/$lat/$lng")
                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                }
            }) {
                Text("Abrir en Maps", color = Amber, fontSize = 13.sp)
                Spacer(Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, tint = Amber, modifier = Modifier.size(13.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Stroke, RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().apply {
                        userAgentValue   = ctx.applicationContext.packageName
                        osmdroidBasePath = File(ctx.applicationContext.cacheDir, "osmdroid")
                        osmdroidTileCache = File(ctx.applicationContext.cacheDir, "osmdroid/tiles")
                    }
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        controller.setCenter(GeoPoint(lat, lng))
                        isHorizontalMapRepetitionEnabled = false
                        isVerticalMapRepetitionEnabled   = false
                        overlays.add(Marker(this).apply {
                            position = GeoPoint(lat, lng)
                            title    = locationName
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        })
                    }
                },
                update   = { mapView -> mapView.controller.setCenter(GeoPoint(lat, lng)) },
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(locationName, fontSize = 13.sp, color = TextHigh, fontWeight = FontWeight.Medium)
        address?.let { Text(it, fontSize = 12.sp, color = TextMid) }
    }
}

@Composable
private fun TicketDialog(attendance: UserAttendance, onDismiss: () -> Unit) {
    val code = attendance.ticketCode ?: attendance.qrCode

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape    = RoundedCornerShape(24.dp),
            color    = Card,
            border   = BorderStroke(1.dp, Stroke)
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier         = Modifier.size(52.dp).clip(CircleShape).background(GreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Green, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text("¡Asistencia confirmada!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                Spacer(Modifier.height(4.dp))
                Text("Muestra este código en la entrada del evento", fontSize = 13.sp, color = TextMid, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                QrBox(size = 160.dp, iconSize = 130.dp, ticketCode = code)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Violet)
                ) {
                    Icon(Icons.Outlined.ConfirmationNumber, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ver mis tickets", fontWeight = FontWeight.Bold, color = TextHigh)
                }
            }
        }
    }
}

@Composable
private fun QrBox(
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    ticketCode: String?
) {
    val qrBitmap: Bitmap? = remember(ticketCode) {
        ticketCode?.let { code ->
            runCatching {
                QRCode.ofSquares().build(code).render().nativeImage() as Bitmap
            }.getOrNull()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Stroke, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR: $ticketCode",
                    modifier           = Modifier.size(iconSize)
                )
            } else {
                Icon(Icons.Outlined.QrCode2, "QR no disponible", tint = Color.Black, modifier = Modifier.size(iconSize))
            }
        }
        ticketCode?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, fontSize = 12.sp, color = Violet, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun ShimmerLoading() {
    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        Box(modifier = Modifier.fillMaxWidth().height(320.dp).background(Card))
        Column(modifier = Modifier.padding(20.dp)) {
            repeat(5) { i ->
                Box(
                    Modifier
                        .fillMaxWidth(when (i) { 0 -> 0.4f; 1 -> 0.85f; else -> 1f })
                        .height(when (i) { 0 -> 18.dp; 1 -> 26.dp; else -> 14.dp })
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardAlt)
                )
                Spacer(Modifier.height(if (i < 2) 10.dp else 14.dp))
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onBack: () -> Unit) {
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
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = Amber),
            border  = BorderStroke(1.dp, Amber.copy(0.4f))
        ) {
            Icon(Icons.Outlined.ArrowBackIosNew, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Regresar", fontWeight = FontWeight.Medium)
        }
    }
}