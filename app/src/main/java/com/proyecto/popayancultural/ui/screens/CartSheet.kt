package com.proyecto.popayancultural.ui.screens

import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.models.CartItem
import com.proyecto.popayancultural.data.models.Order
import java.text.NumberFormat
import java.util.Locale

// ─── Design Tokens ────────────────────────────────────────────────────────────
private val Bg         = Color(0xFF080808)
private val Card       = Color(0xFF111115)
private val CardAlt    = Color(0xFF16161A)
private val Stroke     = Color(0xFF222228)
private val Violet     = Color(0xFFA855F7)
private val VioletSoft = Color(0xFF2D1A4A)
private val TextHigh   = Color(0xFFFFFFFF)
private val TextMid    = Color(0xFF9CA3AF)
private val TextLow    = Color(0xFF4B5563)
private val Green      = Color(0xFF22C55E)
private val GreenSoft  = Color(0xFF052E16)
private val Amber      = Color(0xFFF59E0B)
private val RedColor   = Color(0xFFEF4444)
// ─────────────────────────────────────────────────────────────────────────────

private fun formatCOP(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }.format(valor).let { "$ $it" }

@Composable
fun CartSheet(
    items          : List<CartItem>,
    total          : Double,
    checkoutState  : CheckoutState,
    isLoggedIn     : Boolean,
    onQuitar       : (Int) -> Unit,
    onAgregar      : (Int) -> Unit,
    onEliminar     : (Int) -> Unit,
    onProcesar     : () -> Unit,
    onResetCheckout: () -> Unit,
    onCerrar       : () -> Unit
) {
    when (val state = checkoutState) {
        is CheckoutState.Success -> {
            OrderConfirmationSheet(
                order    = state.order,
                items    = items,
                total    = total,
                onCerrar = { onResetCheckout(); onCerrar() }
            )
            return
        }
        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        // Header
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("Carrito", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextHigh)
                if (items.isNotEmpty()) {
                    Text("${items.sumOf { it.quantity }} producto${if (items.sumOf { it.quantity } != 1) "s" else ""}", fontSize = 12.sp, color = TextMid)
                }
            }
            if (items.isNotEmpty()) {
                TextButton(onClick = { items.forEach { onEliminar(it.product.id) } }) {
                    Text("Vaciar", fontSize = 12.sp, color = TextLow)
                }
            }
        }

        if (items.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(VioletSoft), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ShoppingCart, null, tint = Violet, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Tu carrito está vacío", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextMid)
                Spacer(Modifier.height(4.dp))
                Text("Agrega productos desde la tienda", fontSize = 13.sp, color = TextLow)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.product.id }) { item ->
                    CartItemRow(
                        item       = item,
                        onQuitar   = { onQuitar(item.product.id) },
                        onAgregar  = { onAgregar(item.product.id) },
                        onEliminar = { onEliminar(item.product.id) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Resumen
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = CardAlt, border = BorderStroke(1.dp, Stroke)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 13.sp, color = TextMid)
                        Text(formatCOP(total), fontSize = 13.sp, color = TextHigh)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Envío", fontSize = 13.sp, color = TextMid)
                        Text("A coordinar con el artesano", fontSize = 11.sp, color = Amber)
                    }
                    HorizontalDivider(color = Stroke)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextHigh)
                        Text(formatCOP(total), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Violet)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Aviso P2P
            Row(
                modifier          = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(VioletSoft.copy(0.5f)).border(1.dp, Violet.copy(0.2f), RoundedCornerShape(10.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, null, tint = Violet, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Text("Tu reserva se confirma con el artesano directamente vía WhatsApp. Se genera un comprobante PDF.", fontSize = 11.sp, color = TextMid, lineHeight = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Error
            if (checkoutState is CheckoutState.Error) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFF1A0A0A), border = BorderStroke(1.dp, RedColor.copy(0.4f))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = RedColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(checkoutState.message, fontSize = 12.sp, color = RedColor, lineHeight = 17.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            val isLoading = checkoutState is CheckoutState.Loading

            if (!isLoggedIn) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardAlt, border = BorderStroke(1.dp, Stroke)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, null, tint = TextLow, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Inicia sesión para completar tu compra", fontSize = 13.sp, color = TextMid)
                    }
                }
            } else {
                Button(
                    onClick  = onProcesar,
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Violet, disabledContainerColor = VioletSoft)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = TextHigh, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Procesando...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    } else {
                        Icon(Icons.Outlined.ShoppingBag, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generar contrato P2P", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// ─── Fila de item ─────────────────────────────────────────────────────────────
@Composable
fun CartItemRow(
    item      : CartItem,
    onQuitar  : () -> Unit,
    onAgregar : () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardAlt).border(1.dp, Stroke, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model              = item.product.imageUrl.takeIf { it.isNotBlank() },
            contentDescription = item.product.name,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextHigh, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(formatCOP(item.product.price) + " c/u", fontSize = 12.sp, color = Violet)
            Text("Subtotal: ${formatCOP(item.subtotal)}", fontSize = 11.sp, color = TextMid)
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Card).border(1.dp, Stroke, CircleShape).clickable { onQuitar() }, contentAlignment = Alignment.Center) {
                Text("−", fontSize = 14.sp, color = TextHigh, fontWeight = FontWeight.Bold)
            }
            Text("${item.quantity}", fontSize = 14.sp, color = TextHigh, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(VioletSoft).border(1.dp, Violet.copy(0.4f), CircleShape).clickable { onAgregar() }, contentAlignment = Alignment.Center) {
                Text("+", fontSize = 14.sp, color = Violet, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.DeleteOutline, null, tint = TextLow, modifier = Modifier.size(18.dp).clickable { onEliminar() })
    }
}

// ─── Confirmación con WhatsApp + PDF ─────────────────────────────────────────
@Composable
fun OrderConfirmationSheet(
    order   : Order,
    items   : List<CartItem>,
    total   : Double,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current

    // HTML del comprobante para imprimir/PDF
    val htmlComprobante = remember(order) {
        buildString {
            append("""
            <html><head><meta charset="UTF-8">
            <style>
              body { font-family: 'Helvetica', sans-serif; background: #fff; color: #111; margin: 0; padding: 24px; }
              .header { background: #080808; color: white; padding: 20px; border-radius: 12px; text-align: center; margin-bottom: 20px; }
              .header h1 { margin: 0; font-size: 20px; letter-spacing: 3px; }
              .header p { margin: 4px 0 0; color: #a855f7; font-size: 11px; letter-spacing: 2px; }
              .section { margin-bottom: 16px; }
              .label { font-size: 10px; color: #666; text-transform: uppercase; letter-spacing: 1px; }
              .value { font-size: 14px; font-weight: bold; color: #111; }
              table { width: 100%; border-collapse: collapse; margin-top: 12px; }
              th { background: #a855f7; color: white; padding: 8px; font-size: 11px; text-align: left; }
              td { padding: 8px; font-size: 12px; border-bottom: 1px solid #eee; }
              .total-row { font-weight: bold; background: #f9f9f9; }
              .footer { margin-top: 24px; font-size: 9px; color: #999; text-align: center; border-top: 1px solid #eee; padding-top: 12px; }
              .badge { display: inline-block; background: #f0fdf4; color: #16a34a; border: 1px solid #86efac; padding: 4px 12px; border-radius: 20px; font-size: 10px; font-weight: bold; }
            </style></head><body>
            <div class="header"><h1>POPAYÁN CULTURAL</h1><p>COMPROBANTE DE COMPRA P2P</p></div>
            <div class="section">
              <div class="label">Número de Orden</div><div class="value">${order.orderNumber}</div>
            </div>
            <div class="section">
              <div class="label">Total</div><div class="value">${formatCOP(order.totalAmount)}</div>
            </div>
            <div class="section">
              <div class="label">Estado</div><div><span class="badge">RESERVA PENDIENTE DE PAGO</span></div>
            </div>
            <table>
              <tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr>
            """.trimIndent())
            items.forEach { item ->
                append("<tr><td>${item.product.name}</td><td>${item.quantity}</td><td>${formatCOP(item.product.price)}</td><td>${formatCOP(item.subtotal)}</td></tr>")
            }
            append("""
              <tr class="total-row"><td colspan="3">TOTAL</td><td>${formatCOP(total)}</td></tr>
            </table>
            <div class="footer">
              Popayán Cultural actúa como vitrina digital. Este comprobante acredita la separación del inventario.<br>
              El pago y envío se coordinan directamente con el Artesano.
            </div>
            </body></html>
            """.trimIndent())
        }
    }

    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(GreenSoft).border(1.dp, Green.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.CheckCircle, null, tint = Green, modifier = Modifier.size(36.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("¡Reserva creada!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextHigh)
        Spacer(Modifier.height(4.dp))
        Text("El artesano confirmará tu pago pronto", fontSize = 14.sp, color = TextMid, textAlign = TextAlign.Center)

        Spacer(Modifier.height(20.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = CardAlt, border = BorderStroke(1.dp, Stroke)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OrderDetailRow("Nº de orden", order.orderNumber)
                HorizontalDivider(color = Stroke)
                OrderDetailRow("Total", formatCOP(order.totalAmount), valueColor = Violet)
                HorizontalDivider(color = Stroke)
                OrderDetailRow("Estado", order.statusLabel, valueColor = Amber)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botones de acción
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            // PDF / Imprimir
            OutlinedButton(
                onClick = {
                    val webView = WebView(context)
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as PrintManager
                            val jobName      = "Orden_${order.orderNumber}"
                            val printAdapter = view.createPrintDocumentAdapter(jobName)
                            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                        }
                    }
                    webView.loadDataWithBaseURL(null, htmlComprobante, "text/html", "UTF-8", null)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Violet),
                border   = BorderStroke(1.dp, Violet.copy(0.4f))
            ) {
                Icon(Icons.Outlined.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // WhatsApp al artesano
            val phone = items.firstOrNull()?.product?.author?.phone
            if (!phone.isNullOrBlank()) {
                Button(
                    onClick = {
                        val clean   = phone.replace(Regex("[^0-9]"), "")
                        val mensaje = buildString {
                            append("Hola Maestro 👋\nQuiero confirmar el pago de mi orden *${order.orderNumber}* en Popayán Cultural.\n\n*Detalle:*")
                            items.forEach { item -> append("\n- ${item.quantity}x ${item.product.name} (${formatCOP(item.product.price)})") }
                            append("\n\n*Total:* ${formatCOP(order.totalAmount)}\n\nTengo mi comprobante PDF listo. ¿Me confirmas los datos de pago?")
                        }
                        val uri = Uri.parse("https://wa.me/$clean?text=${Uri.encode(mensaje)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextHigh)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick  = onCerrar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Violet)
        ) {
            Text("Volver a la tienda", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun OrderDetailRow(label: String, value: String, valueColor: Color = TextHigh) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = TextMid)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}