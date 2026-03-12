package com.proyecto.popayancultural.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.proyecto.popayancultural.ui.theme.CardBackground
import com.proyecto.popayancultural.ui.theme.VioletAcento

@Composable
fun ArtCard(
    nombre: String,
    etiqueta: String,
    urlImagen: String,
    onClick: () -> Unit
) {
    // ELIMINADO: val matrix = ColorMatrix().apply { setToSaturation(0f) }
    // Ahora las imágenes brillarán con sus colores reales.

    Column(
        modifier = Modifier
            .width(170.dp)
            .padding(end = 12.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f) // Un poco más vertical para mayor elegancia
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = VioletAcento,
                    spotColor = VioletAcento.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(32.dp)) // Squicles más pronunciados (32dp)
                .background(CardBackground)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
        ) {
            SubcomposeAsyncImage(
                model = urlImagen,
                contentDescription = nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // colorFilter ELIMINADO para habilitar el COLOR
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF111113)))
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color(0xFF2A2A2C), Color(0xFF0A0A0C)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("POP CULTURAL", fontSize = 8.sp, color = Color.White.copy(alpha = 0.2f))
                    }
                }
            )

            // --- DEGRADADO INFERIOR PARA LEGIBILIDAD ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 300f
                        )
                    )
            )

            // --- ETIQUETA FLOTANTE ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(VioletAcento, RoundedCornerShape(8.dp)) // Fondo violeta sólido para resaltar
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = etiqueta.uppercase(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- TEXTOS MONOSPACIADOS Y TIGHT ---
        Text(
            text = nombre,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            fontSize = 15.sp,
            letterSpacing = (-0.5).sp // tracking-tighter
        )

        Text(
            text = "VER RELATO",
            color = VioletAcento,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp // tracking-widest
        )
    }
}