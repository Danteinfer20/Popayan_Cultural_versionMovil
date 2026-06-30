package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.proyecto.popayancultural.ui.theme.*

// MODELO DE DATOS
data class TallerMock(
    val id: Int,
    val titulo: String,
    val maestro: String,
    val duracion: String,
    val nivel: String,
    val imagenUrl: String,
    val maestroAvatar: String,
    val descripcionTaller: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprendeScreen() {
    var selectedTaller by remember { mutableStateOf<TallerMock?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // INTEGRACIÓN DE TUS 4 URLS EXACTAS
    val talleres = listOf(
        TallerMock(
            id = 1,
            titulo = "Alfarería Ancestral",
            maestro = "Braulio Ledezma",
            duracion = "4 semanas",
            nivel = "Intermedio",
            imagenUrl = "https://i.pinimg.com/1200x/2e/1c/a4/2e1ca46a2b26008083ae6f854679c0a2.jpg",
            maestroAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            descripcionTaller = "Aprende a dominar el barro y el fuego utilizando técnicas heredadas para crear piezas únicas de cerámica."
        ),
        TallerMock(
            id = 2,
            titulo = "Tejeduría en Telar",
            maestro = "María Timbío",
            duracion = "2 semanas",
            nivel = "Básico",
            imagenUrl = "https://i.pinimg.com/736x/c3/51/cd/c351cda408a64968728cb120123181ba.jpg",
            maestroAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200",
            descripcionTaller = "Descubre el proceso de la lana, desde el hilado hasta el uso de tintes orgánicos de la región del Cauca."
        ),
        TallerMock(
            id = 3,
            titulo = "Talla y Madera",
            maestro = "Pedro Ruiz",
            duracion = "6 semanas",
            nivel = "Avanzado",
            imagenUrl = "https://i.pinimg.com/736x/61/ce/c4/61cec4c3c69ebce852a1d04a15373795.jpg",
            maestroAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            descripcionTaller = "Técnicas de restauración y tallado de relieves coloniales enfocados en la arquitectura histórica."
        ),
        TallerMock(
            id = 4,
            titulo = "Orfebrería Fina",
            maestro = "Elena Castro",
            duracion = "3 semanas",
            nivel = "Intermedio",
            imagenUrl = "https://i.pinimg.com/1200x/b8/74/e8/b874e876a596a8495d74a6fc1b22665d.jpg",
            maestroAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
            descripcionTaller = "Introducción al manejo de metales y filigrana, inspirada en las custodias y tesoros de Popayán."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 24.dp) // Espaciado matemático lateral
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // TÍTULO DARK MODERN
        Text(
            text = "TALLERES VIVOS",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 1.sp,
            lineHeight = 36.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "El legado del Cauca en tus manos.",
            color = VioletAcento,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(talleres) { taller ->
                TallerCard(taller) {
                    selectedTaller = taller
                    showSheet = true
                }
            }
        }
    }

    // MODAL BOTTOM SHEET (DETALLE)
    if (showSheet && selectedTaller != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = CardBackground,
            dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
        ) {
            TallerDetailContent(selectedTaller!!)
        }
    }
}

@Composable
fun TallerCard(taller: TallerMock, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) // Ring sutil
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // MOTOR COIL CONFIGURADO PARA REDES EXTERNAS
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(taller.imagenUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // GRADIENTE DE PROFUNDIDAD
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundDeep.copy(alpha = 0.95f)),
                        startY = 100f
                    ))
            )

            // METADATOS
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = taller.titulo,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = VioletAcento, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(taller.duracion, color = Color.LightGray, fontSize = 13.sp)

                    Spacer(modifier = Modifier.width(20.dp))

                    Icon(Icons.Default.Star, null, tint = VioletAcento, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(taller.nivel, color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TallerDetailContent(taller: TallerMock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DETALLE DEL TALLER", color = VioletAcento, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(taller.titulo, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 36.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(taller.descripcionTaller, color = Color.LightGray, textAlign = TextAlign.Center, lineHeight = 24.sp, fontSize = 15.sp)

        Spacer(modifier = Modifier.height(40.dp))

        // NODO DEL MAESTRO
        Text("MAESTRO INSTRUCTOR", color = VioletAcento, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(BackgroundDeep, RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(20.dp))
                .padding(12.dp)
                .padding(end = 16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(taller.maestroAvatar)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(taller.maestro, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // BOTÓN DE ACCIÓN
        Button(
            onClick = { /* Lógica de base de datos */ },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioletAcento)
        ) {
            Text("INSCRIBIRME AL TALLER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
        }
    }
}