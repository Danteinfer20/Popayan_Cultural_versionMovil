package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.proyecto.popayancultural.data.models.Product

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
private val Red        = Color(0xFFEF4444)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetalleProductoScreen(
    product  : Product,
    onBack   : () -> Unit,
    onAgregar: () -> Unit
) {
    val context    = LocalContext.current
    val imagenes   = remember(product) {
        product.images.filter { it.isNotBlank() }.ifEmpty {
            listOfNotNull(product.mainImage?.takeIf { it.isNotBlank() })
        }
    }
    val pagerState = rememberPagerState(pageCount = { imagenes.size.coerceAtLeast(1) })

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Galería ───────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                if (imagenes.isEmpty()) {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.ShoppingBag, null, tint = Violet.copy(0.3f), modifier = Modifier.size(64.dp)) }
                } else {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        SubcomposeAsyncImage(
                            model              = imagenes[page],
                            contentDescription = product.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize(),
                            loading = { Box(Modifier.fillMaxSize().background(CardAlt)) },
                            error   = {
                                Box(
                                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(VioletDeep, Color(0xFF0D0D10)))),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Outlined.ShoppingBag, null, tint = Violet.copy(0.3f), modifier = Modifier.size(64.dp)) }
                            }
                        )
                    }
                }

                // Gradiente inferior
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Bg.copy(0.8f), Bg))
                    )
                )

                // Botón back
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(0.55f))
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Outlined.ArrowBackIosNew, "Regresar", tint = TextHigh, modifier = Modifier.size(18.dp))
                }

                // Indicadores de página
                if (imagenes.size > 1) {
                    Row(
                        modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        imagenes.indices.forEach { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == i) 20.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (pagerState.currentPage == i) Violet else TextLow)
                            )
                        }
                    }
                }

                // Miniaturas laterales si hay más de 1 imagen
                if (imagenes.size > 1) {
                    Column(
                        modifier              = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(0.5f))
                            .padding(6.dp),
                        verticalArrangement   = Arrangement.spacedBy(6.dp)
                    ) {
                        imagenes.take(4).forEachIndexed { i, url ->
                            val isActive = pagerState.currentPage == i
                            AsyncImage(
                                model              = url,
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.5.dp,
                                        if (isActive) Violet else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        /* scroll pager to i */
                                    }
                            )
                        }
                    }
                }
            }

            // ── Info ──────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                // Categoría + badge
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.category?.let {
                        Surface(shape = RoundedCornerShape(6.dp), color = VioletSoft, border = BorderStroke(1.dp, Violet.copy(0.3f))) {
                            Text(it.name.uppercase(), fontSize = 9.sp, color = Violet, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    if (product.isFeatured) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1A1A0A), border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(0.4f))) {
                            Text("DESTACADO", fontSize = 9.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(product.name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextHigh, lineHeight = 30.sp)
                Spacer(Modifier.height(10.dp))

                // Precio
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(product.displayPrice, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Violet)
                    product.salePrice?.let { sale ->
                        Spacer(Modifier.width(10.dp))
                        Text("$ ${String.format("%,.0f", sale)}", fontSize = 14.sp, color = TextLow, textDecoration = TextDecoration.LineThrough)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Stock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (product.isAvailable) Green else Red))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (product.isAvailable) "${product.stockQuantity} unidades disponibles" else "Sin stock",
                        fontSize = 12.sp,
                        color    = if (product.isAvailable) Green else Red
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Artesano
                product.author?.let { autor ->
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
                                Text(
                                    autor.name.firstOrNull()?.uppercase() ?: "A",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color      = Violet
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MAESTRO CREADOR", fontSize = 9.sp, color = Violet, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(autor.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                            }
                            // WhatsApp si tiene teléfono
                            autor.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF052E16))
                                        .border(1.dp, Green.copy(0.3f), CircleShape)
                                        .clickable {
                                            val clean = phone.replace(Regex("[^0-9]"), "")
                                            val uri   = Uri.parse("https://wa.me/$clean")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Chat, null, tint = Green, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Stroke)
                Spacer(Modifier.height(16.dp))

                // Descripción
                if (product.description.isNotBlank()) {
                    Text("Relato de la obra", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Violet, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(product.description, fontSize = 14.sp, color = TextMid, lineHeight = 22.sp)
                    Spacer(Modifier.height(16.dp))
                }

                // Specs
                val specs = listOfNotNull(
                    product.specs?.materials?.let { "Técnica / Material" to it },
                    product.specs?.dimensions?.let { "Dimensiones" to it },
                    product.specs?.weight?.let { "Peso" to it }
                )
                if (specs.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        color    = CardAlt,
                        border   = BorderStroke(1.dp, Stroke)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            specs.forEach { (label, value) ->
                                Row {
                                    Text(label, fontSize = 12.sp, color = TextMid, modifier = Modifier.width(130.dp))
                                    Text(value, fontSize = 12.sp, color = TextHigh, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Garantías
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GarantiaItem(Icons.Outlined.VerifiedUser, "Venta\nAutenticada")
                    GarantiaItem(Icons.Outlined.LocalShipping, "Logística\nP2P")
                    GarantiaItem(Icons.Outlined.Handshake, "Pago\nDirecto")
                }

                Spacer(Modifier.height(24.dp))

                // Botón principal
                Button(
                    onClick  = onAgregar,
                    enabled  = product.isAvailable,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Violet,
                        disabledContainerColor = CardAlt
                    )
                ) {
                    Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (product.isAvailable) "Agregar al carrito" else "Sin stock",
                        fontWeight = FontWeight.Black,
                        fontSize   = 16.sp
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun GarantiaItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(VioletSoft).border(1.dp, Violet.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Violet, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 9.sp, color = TextMid, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}