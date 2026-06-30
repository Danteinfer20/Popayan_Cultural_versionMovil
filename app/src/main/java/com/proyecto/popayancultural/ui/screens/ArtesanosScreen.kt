package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*

// 1. MODELO DE DATOS ACTUALIZADO
data class ArtesanoMock(
    val id: Int,
    val nombre: String,
    val username: String,
    val fotoUrl: String,
    val especialidad: String,
    val historia: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtesanosScreen() {
    // --- ESTADOS PARA EL MODAL ---
    var selectedArtesano by remember { mutableStateOf<ArtesanoMock?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // Datos con historias reales para la presentación
    val artesanos = listOf(
        ArtesanoMock(1, "BRAULIO LEDEZMA", "@braulio-forja", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&q=80", "Maestro Forjador", "Guardián de la técnica del hierro forjado en el barrio Pueblillo. Crea los faroles que iluminan las procesiones de Semana Santa."),
        ArtesanoMock(2, "MARÍA TIMBÍO", "@maria-seda", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500&q=80", "Tejedora de Seda", "Líder de artesanas en Timbío. Sus chales de seda natural son pintados con tintes orgánicos de la región."),
        ArtesanoMock(3, "PEDRO RUIZ", "@pedro-colonial", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&q=80", "Ebanista Colonial", "Restaurador de retablos y tallador experto en maderas finas, preservando el estilo barroco de la Ciudad Blanca."),
        ArtesanoMock(4, "LAURA MARTÍNEZ", "@laura-ceramica", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&q=80", "Ceramista", "Especialista en arcilla roja. Sus piezas fusionan iconografía precolombina con técnicas modernas."),
        ArtesanoMock(5, "JULIAN SALINAS", "@julian-cantera", "https://images.unsplash.com/photo-1552058544-f2b08422138a?w=500&q=80", "Tallador de Piedra", "Maestro en el manejo de la cantera blanca, material que define la arquitectura del centro histórico."),
        ArtesanoMock(6, "EDWARD SALINAS", "@edward-plata", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&q=80", "Orfebre", "Experto en filigrana de plata y joyería tradicional payanesa.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // HEADER PREMIUM
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = buildAnnotatedString {
                    append("MAESTROS ")
                    withStyle(style = SpanStyle(
                        brush = Brush.verticalGradient(listOf(VioletAcento, Color(0xFFD8B4FE)))
                    )) {
                        append("ARTESANOS")
                    }
                },
                color = TextPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BUSCADOR
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar artesano o técnica...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = VioletAcento) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardBackground,
                unfocusedBorderColor = BorderSutil
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // GRID DE TARJETAS
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(artesanos) { artesano ->
                ArtesanoCard(
                    artesano = artesano,
                    onClickAvatar = {
                        selectedArtesano = artesano
                        showSheet = true
                    }
                )
            }
        }
    }

    // --- 2. EL MODAL (MODAL BOTTOM SHEET) ---
    if (showSheet && selectedArtesano != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = CardBackground,
            scrimColor = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = selectedArtesano?.fotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, VioletAcento, CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = selectedArtesano?.nombre ?: "",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = selectedArtesano?.especialidad ?: "",
                    color = VioletAcento,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "HISTORIA Y LEGADO",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = selectedArtesano?.historia ?: "",
                    color = TextPrimary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAcento)
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ArtesanoCard(artesano: ArtesanoMock, onClickAvatar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSutil)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = artesano.fotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(2.dp, VioletAcento, CircleShape)
                    .clickable { onClickAvatar() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = artesano.nombre,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = artesano.especialidad,
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}