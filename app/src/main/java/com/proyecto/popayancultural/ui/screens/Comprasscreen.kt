package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.proyecto.popayancultural.data.models.Order
import com.proyecto.popayancultural.data.models.OrderItemDisplay
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.ComprasState
import com.proyecto.popayancultural.ui.viewmodels.UserDashboardViewModel
import qrcode.QRCode
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ComprasScreen(
    viewModel: UserDashboardViewModel
) {
    val state by viewModel.comprasState.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarCompras() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TESOROS ADQUIRIDOS.",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when (val s = state) {
                is ComprasState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VioletAcento, strokeWidth = 2.dp)
                    }
                }
                is ComprasState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                is ComprasState.Success -> {
                    if (s.orders.isEmpty()) {
                        ComprasEmptyState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(s.orders, key = { it.id }) { orden ->
                                OrdenCard(
                                    orden   = orden,
                                    onClick = { selectedOrder = orden }
                                )
                            }
                        }
                    }
                }
            }
        }

        selectedOrder?.let { orden ->
            OrdenDetalleModal(
                orden     = orden,
                onDismiss = { selectedOrder = null }
            )
        }
    }
}

// ─── Tarjeta de orden ─────────────────────────────────────────────────────────

@Composable
private fun OrdenCard(orden: Order, onClick: () -> Unit) {
    val primerProducto = orden.displayItems.firstOrNull()?.name ?: "Orden"
    val esEvento = orden.orderType == "event"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (esEvento) Icons.Outlined.ConfirmationNumber else Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = VioletAcento,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text       = orden.orderNumber,
                        color      = VioletAcento,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = primerProducto.uppercase(),
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle  = FontStyle.Italic,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = formatCOP(orden.totalAmount),
                    color      = Color.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                EstadoBadge(estado = orden.status)
            }
        }
    }
}

// ─── Modal de detalle ─────────────────────────────────────────────────────────

@Composable
private fun OrdenDetalleModal(orden: Order, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val esEvento = orden.orderType == "event"
    var isGenerating by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "RECIBO DE ADQUISICIÓN",
                        color      = Color.White,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle  = FontStyle.Italic
                    )
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint     = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text          = orden.orderNumber,
                    color         = VioletAcento,
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))

                orden.displayItems.forEach { item ->
                    OrdenItemRow(item = item)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text          = "TOTAL",
                        color         = Color.Gray,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text       = formatCOP(orden.totalAmount),
                        color      = Color.White,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    EstadoBadge(estado = orden.status, large = true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Acción según tipo de orden ────────────────────────────
                if (esEvento) {
                    QrTicketBox(codigo = orden.orderNumber)
                } else {
                    Button(
                        onClick = {
                            isGenerating = true
                            pdfError = null
                            try {
                                val uri = generarPdfRecibo(context, orden)
                                abrirPdf(context, uri)
                            } catch (e: Exception) {
                                pdfError = "No se pudo generar el comprobante."
                            } finally {
                                isGenerating = false
                            }
                        },
                        enabled  = !isGenerating,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = VioletAcento)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.FileDownload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DESCARGAR PDF", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                    pdfError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color(0xFFEF4444), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

// ─── QR para tickets de evento ─────────────────────────────────────────────────

@Composable
private fun QrTicketBox(codigo: String) {
    val qrBitmap: Bitmap? = remember(codigo) {
        runCatching {
            QRCode.ofSquares().build(codigo).render().nativeImage() as Bitmap
        }.getOrNull()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MUESTRA ESTE CÓDIGO EN LA ENTRADA",
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR: $codigo",
                    modifier           = Modifier.size(130.dp)
                )
            } else {
                Icon(Icons.Outlined.QrCode2, "QR no disponible", tint = Color.Black, modifier = Modifier.size(60.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(codigo, color = VioletAcento, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
    }
}

// ─── Generación de PDF (nativo, sin dependencias nuevas) ──────────────────────

private fun generarPdfRecibo(context: android.content.Context, orden: Order): android.net.Uri {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 @ 72dpi
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val labelPaint = Paint().apply {
        color = AndroidColor.DKGRAY
        textSize = 11f
    }
    val itemPaint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 12f
    }
    val totalPaint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val linePaint = Paint().apply {
        color = AndroidColor.LTGRAY
        strokeWidth = 1f
    }

    var y = 50f
    canvas.drawText("POPAYÁN CULTURAL", 40f, y, titlePaint)
    y += 22f
    canvas.drawText("Orden: ${orden.orderNumber}", 40f, y, labelPaint)
    y += 16f
    canvas.drawText("Estado: ${orden.statusLabel}", 40f, y, labelPaint)
    y += 24f
    canvas.drawLine(40f, y, 555f, y, linePaint)
    y += 24f

    orden.displayItems.forEach { item ->
        canvas.drawText(item.name, 40f, y, itemPaint)
        canvas.drawText("x${item.quantity}", 380f, y, itemPaint)
        canvas.drawText(formatCOP(item.subtotal), 460f, y, itemPaint)
        y += 22f
    }

    y += 10f
    canvas.drawLine(40f, y, 555f, y, linePaint)
    y += 28f
    canvas.drawText("TOTAL", 40f, y, totalPaint)
    canvas.drawText(formatCOP(orden.totalAmount), 460f, y, totalPaint)

    pdf.finishPage(page)

    val cacheDir = File(context.cacheDir, "recibos").apply { mkdirs() }
    val file = File(cacheDir, "Recibo_${orden.orderNumber}.pdf")
    FileOutputStream(file).use { pdf.writeTo(it) }
    pdf.close()

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun abrirPdf(context: android.content.Context, uri: android.net.Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

// ─── Fila de item ─────────────────────────────────────────────────────────────

@Composable
private fun OrdenItemRow(item: OrderItemDisplay) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = item.name.uppercase(),
                color      = Color.White,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = "x${item.quantity}",
                color    = Color.Gray,
                fontSize = 9.sp
            )
        }
        Text(
            text       = formatCOP(item.subtotal),
            color      = Color.White,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// ─── Badge de estado ──────────────────────────────────────────────────────────

@Composable
private fun EstadoBadge(estado: String, large: Boolean = false) {
    val (text, color, bg) = when (estado) {
        "confirmed" -> Triple("CONFIRMADO", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.1f))
        "pending"   -> Triple("PENDIENTE",  Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.1f))
        "delivered" -> Triple("ENTREGADO",  Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.1f))
        "cancelled" -> Triple("CANCELADO",  Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.1f))
        else        -> Triple("EN PROCESO", Color(0xFFA855F7), Color(0xFFA855F7).copy(alpha = 0.1f))
    }

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = bg,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text          = text,
            color         = color,
            fontSize      = if (large) 10.sp else 7.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(
                horizontal = if (large) 14.dp else 10.dp,
                vertical   = if (large) 6.dp  else 4.dp
            )
        )
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
private fun ComprasEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint     = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text       = "SIN ADQUISICIONES",
                color      = Color.White.copy(alpha = 0.3f),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Black,
                fontStyle  = FontStyle.Italic
            )
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun formatCOP(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return "$${format.format(amount)}"
}