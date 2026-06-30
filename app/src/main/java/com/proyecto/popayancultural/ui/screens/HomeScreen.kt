package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.proyecto.popayancultural.data.models.*
import com.proyecto.popayancultural.ui.HomeViewModel
import com.proyecto.popayancultural.ui.components.*

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val BgBase        = Color(0xFF080808)
private val BgCard        = Color(0xFF111115)
private val BgCardBorder  = Color(0xFF2A2A35)
private val Violet        = Color(0xFFA855F7)
private val VioletDark    = Color(0xFF7C3AED)
private val TextPrimary   = Color(0xFFF0F0F0)
private val TextSecondary = Color(0xFF888888)
private val TextMuted     = Color(0xFF555555)

private val ShapeCard   = RoundedCornerShape(12.dp)
private val ShapeAvatar = CircleShape
private val ShapeChip   = RoundedCornerShape(20.dp)

// Dimensiones de imagen — fijas, el texto fluye libre debajo
private val CARD_OBRA_WIDTH      = 120.dp
private val CARD_OBRA_IMAGE_H    = 90.dp

private val CARD_PRODUCT_WIDTH   = 130.dp
private val CARD_PRODUCT_IMAGE_H = 100.dp

private val CARD_COURSE_IMAGE    = 48.dp
private val AVATAR_SIZE          = 50.dp
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun editableImageRequest(url: String?): ImageRequest {
    val context = LocalContext.current
    return ImageRequest.Builder(context)
        .data(url)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .crossfade(true)
        .build()
}

@Composable
fun staticImageRequest(url: String?): ImageRequest {
    val context = LocalContext.current
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()
}

private fun safeDay(dateStr: String?): String = runCatching {
    dateStr?.take(10)?.substring(8, 10) ?: "--"
}.getOrDefault("--")

private fun safeMonth(dateStr: String?): String = runCatching {
    dateStr?.take(10)?.substring(5, 7) ?: "--"
}.getOrDefault("--")

private fun safeTime(dateStr: String?, timeStr: String?): String {
    if (dateStr != null && dateStr.contains("T")) {
        return runCatching { dateStr.substring(11, 16) }.getOrDefault("")
    }
    return timeStr?.take(5).orEmpty()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel      : HomeViewModel    = viewModel(),
    onEventClick   : (Int) -> Unit    = {},
    onObraClick    : (String) -> Unit = {},
    onProductClick : (Int) -> Unit    = {},
    onArtistaClick : (String) -> Unit = {},
    onEducaClick   : (Int) -> Unit    = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val eventos   by viewModel.eventos.collectAsState()
    val obras     by viewModel.obras.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val educacion by viewModel.educacion.collectAsState()
    val artistas  by viewModel.artistas.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        if (isLoading) {
            CircularProgressIndicator(color = Violet, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

                // ── HERO ──────────────────────────────────────────────────────
                item {
                    if (eventos.isNotEmpty()) {
                        val pagerState = rememberPagerState { eventos.size }
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                val ev = eventos[page]
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onEventClick(ev.id) }
                                ) {
                                    SubcomposeAsyncImage(
                                        model              = editableImageRequest(ev.coverImage),
                                        contentDescription = null,
                                        modifier           = Modifier.fillMaxSize(),
                                        contentScale       = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(
                                            Brush.verticalGradient(
                                                0f   to Color.Transparent,
                                                0.5f to Color(0x40000000),
                                                1f   to BgBase
                                            )
                                        )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            "EVENTO DESTACADO",
                                            color         = Violet,
                                            fontSize      = 8.sp,
                                            fontWeight    = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            ev.title.orEmpty().uppercase(),
                                            color      = TextPrimary,
                                            fontSize   = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = 21.sp,
                                            maxLines   = 2,
                                            overflow   = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier              = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(eventos.size) { i ->
                                    val isActive = pagerState.currentPage == i
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(if (isActive) 14.dp else 5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isActive) Violet else TextMuted)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── AGENDA ────────────────────────────────────────────────────
                item {
                    SectionHeader("Próximos eventos", "Agenda cultural", "Ver agenda")
                    Column(
                        modifier            = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        eventos.take(3).forEach { ev ->
                            AgendaCard(
                                day      = safeDay(ev.startDate),
                                month    = safeMonth(ev.startDate),
                                title    = ev.title.orEmpty(),
                                location = ev.location?.name.orEmpty().ifEmpty { "Popayán" },
                                time     = safeTime(ev.startDate, ev.startTime),
                                onClick  = { onEventClick(ev.id) }
                            )
                        }
                    }
                }

                // ── OBRAS ─────────────────────────────────────────────────────
                item {
                    SectionHeader("Patrimonio", "Obras maestras", "Ver galería")
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(obras) { obra ->
                            ArtCard(
                                title    = obra.title.orEmpty(),
                                tag      = "Patrimonio",
                                imageUrl = obra.imageUrl,
                                onClick  = { obra.slug?.let { onObraClick(it) } }
                            )
                        }
                    }
                }

                // ── POP STORE ─────────────────────────────────────────────────
                item {
                    SectionHeader("Certificado", "Pop Store", "Ver tienda")
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productos) { prod ->
                            CompactProductCard(prod) { onProductClick(prod.id) }
                        }
                    }
                }

                // ── APRENDE ───────────────────────────────────────────────────
                item {
                    SectionHeader("Maestros", "Aprende", "Ver cursos")
                    Column(
                        modifier            = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        educacion.take(3).forEach { ed ->
                            CourseCard(
                                title    = ed.title.orEmpty(),
                                duration = "${ed.metadata?.estimatedReadTime ?: 15} min",
                                level    = ed.levelLabel,
                                imageUrl = ed.imageUrl,
                                onClick  = { onEducaClick(ed.id) }
                            )
                        }
                    }
                }

                // ── DIRECTORIO ────────────────────────────────────────────────
                item {
                    SectionHeader("Creadores", "Directorio", "Ver artistas")
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(artistas) { art ->
                            ArtistAvatar(
                                name     = art.name.orEmpty(),
                                location = art.location.orEmpty().ifEmpty { "Popayán" },
                                imageUrl = art.avatar,
                                initials = art.name.orEmpty().take(2).uppercase().ifEmpty { "??" },
                                onClick  = { onArtistaClick(art.username) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Componentes privados ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(eyebrow: String, title: String, linkText: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(eyebrow.uppercase(), color = Violet, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text(linkText, color = Violet, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AgendaCard(
    day: String, month: String, title: String,
    location: String, time: String, onClick: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ShapeCard)
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.width(28.dp)
        ) {
            Text(day.ifEmpty { "--" }, color = Violet, fontSize = 16.sp, fontWeight = FontWeight.Black, lineHeight = 16.sp)
            Text(month.ifEmpty { "--" }, color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(modifier = Modifier.width(1.dp).height(26.dp).background(BgCardBorder))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title.ifEmpty { "Sin título" },
                color      = TextPrimary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(location.ifEmpty { "Popayán" }, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(time.ifEmpty { "--:--" }, color = Violet, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ArtCard — imagen fija, texto fluye libre (sin Box de altura fija)
@Composable
private fun ArtCard(
    title: String, tag: String, imageUrl: String?, onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(CARD_OBRA_WIDTH)
            .clip(ShapeCard)
            .background(BgCard)
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model              = staticImageRequest(imageUrl),
            contentDescription = null,
            modifier           = Modifier
                .fillMaxWidth()
                .height(CARD_OBRA_IMAGE_H),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(Color(0xFF16161A))) }
        )
        // Zona de texto: padding fijo, el texto respira y nunca se corta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                tag.uppercase(),
                color         = Violet,
                fontSize      = 7.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 1.sp,
                maxLines      = 1
            )
            Spacer(Modifier.height(3.dp))
            Text(
                title.ifEmpty { "Sin título" },
                color      = TextPrimary,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }
    }
}

// CompactProductCard — imagen fija, texto fluye libre (sin Box de altura fija)
@Composable
private fun CompactProductCard(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(CARD_PRODUCT_WIDTH)
            .clip(ShapeCard)
            .background(BgCard)
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model              = staticImageRequest(product.imageUrl.takeIf { it.isNotBlank() }),
            contentDescription = product.name,
            modifier           = Modifier
                .fillMaxWidth()
                .height(CARD_PRODUCT_IMAGE_H),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(Color(0xFF16161A))) }
        )
        // Zona de texto: sin altura fija, el contenido define el tamaño
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                product.name,
                color      = TextPrimary,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                product.displayPrice,
                color      = Violet,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CourseCard(
    title: String, duration: String, level: String,
    imageUrl: String?, onClick: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)  // mínimo pero puede crecer
            .clip(ShapeCard)
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SubcomposeAsyncImage(
            model              = editableImageRequest(imageUrl),
            contentDescription = null,
            modifier           = Modifier
                .size(CARD_COURSE_IMAGE)
                .clip(RoundedCornerShape(8.dp)),
            contentScale       = ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize().background(Color(0xFF16161A))) }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title.ifEmpty { "Sin título" },
                color      = TextPrimary,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(duration, level).forEach { chip ->
                    Text(
                        chip.ifEmpty { "-" },
                        color    = TextSecondary,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(ShapeChip)
                            .background(Color(0xFF1E1E2A))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistAvatar(
    name: String, location: String, imageUrl: String?,
    initials: String, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .width(AVATAR_SIZE + 14.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier         = Modifier
                .size(AVATAR_SIZE)
                .clip(ShapeAvatar)
                .background(Brush.linearGradient(listOf(Violet, VioletDark)))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(ShapeAvatar)
                    .background(Color(0xFF1A0A2E)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model              = editableImageRequest(imageUrl),
                        contentDescription = name,
                        modifier           = Modifier.fillMaxSize().clip(ShapeAvatar),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Text(initials.ifEmpty { "??" }, color = Violet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            name.ifEmpty { "Artista" },
            color      = TextPrimary,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.fillMaxWidth()
        )
        Text(
            location.ifEmpty { "Popayán" },
            color    = TextSecondary,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}