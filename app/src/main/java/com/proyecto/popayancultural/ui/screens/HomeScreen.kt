package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.proyecto.popayancultural.ui.components.ArtCard
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<CulturalItem?>(null) }

    // --- BASE DE DATOS MAESTRA (CONSERVADA ÍNTEGRA) ---
    val datosHome = remember {
        listOf(
            // ARTISTAS
            CulturalItem(1, "Maestro Alfarero", "Barro Negro", "https://i.pinimg.com/1200x/0f/41/9c/0f419c38373d6a579e7dfeb8fe09cfb1.jpg", "Técnicas ancestrales de moldeo a mano.", "ARTISTA"),
            CulturalItem(2, "Tejedora Misak", "Telar de Guanga", "https://i.pinimg.com/1200x/2e/dd/0e/2edd0e8d5aaac71c6dedacbdd033ce15.jpg", "Herencia textil en cada hilo de lana virgen.", "ARTISTA"),
            CulturalItem(3, "Talla en Cedro", "Imaginería", "https://i.pinimg.com/736x/a1/19/15/a1191589a4ed6a01bc7cd3c8e24c3af7.jpg", "Escultura sacra con acabados en policromía.", "ARTISTA"),
            CulturalItem(4, "Filigrana Caucana", "Orfebrería", "https://i.pinimg.com/1200x/61/29/2f/61292f01e939938b2b0103115100295e.jpg", "Hilos de plata convertidos en joyas eternas.", "ARTISTA"),
            CulturalItem(5, "Pintura Colonial", "Artes Plásticas", "https://i.pinimg.com/1200x/9f/c3/c7/9fc3c769083ab4010f852953616f1fb8.jpg", "Óleos inspirados en los rincones de la Ciudad Blanca.", "ARTISTA"),
            CulturalItem(6, "Cestería de Iraca", "Tejido Natural", "https://i.pinimg.com/736x/3f/67/a2/3f67a21632c51b410e14983164ef2514.jpg", "Fibra vegetal transformada en canastos y accesorios.", "ARTISTA"),
            CulturalItem(7, "Talabartería", "Trabajo en Cuero", "https://i.pinimg.com/1200x/1b/24/2d/1b242df2db57f6378ee3cfadfbcc2704.jpg", "Repujado artesanal con diseños de época.", "ARTISTA"),
            CulturalItem(8, "Maestra Cerámica", "Técnica Chamba", "https://i.pinimg.com/1200x/a3/73/8c/a3738c1cfba8c83c3a92ed67aeb5119e.jpg", "Guardiana del secreto del barro vidriado.", "ARTISTA"),

            // PRODUCTOS (POP STORE)
            CulturalItem(9, "Mochila Ancestral", "$180k", "https://i.pinimg.com/736x/c4/d5/c0/c4d5c07c95d3ee4960f7fd1ba42a87ef.jpg", "Mochila tejida con simbología caucana.", "PRODUCTO"),
            CulturalItem(10, "Cuenco Rústico", "$45k", "https://i.pinimg.com/474x/99/ca/9b/99ca9bf1f08c3820cb2e4950a66af20d.jpg", "Barro natural ideal para decoración.", "PRODUCTO"),
            CulturalItem(11, "Vaso de Autor", "$35k", "https://i.pinimg.com/736x/fa/d3/01/fad301b0ba5a28bd9b022e1e384d5a82.jpg", "Diseño contemporáneo con alma artesanal.", "PRODUCTO"),
            CulturalItem(12, "Poncho de Lana", "$210k", "https://i.pinimg.com/736x/aa/5d/2f/aa5d2f7e0203d8a3da5d7766e01860f2.jpg", "Tejido térmico de alta durabilidad.", "PRODUCTO"),
            CulturalItem(13, "Plato de Colección", "$55k", "https://i.pinimg.com/736x/c3/b9/b5/c3b9b557711a61893fb5d8cb8cfab034.jpg", "Pintura manual sobre cerámica blanca.", "PRODUCTO"),
            CulturalItem(14, "Dije Filigrana", "$120k", "https://i.pinimg.com/1200x/6d/37/77/6d3777925fb4155292308aee1cdb34ca.jpg", "Plata pura en técnica de filigrana.", "PRODUCTO"),
            CulturalItem(15, "Individuales", "$65k", "https://i.pinimg.com/1200x/a1/34/ee/a134ee0af4bedce028a6523fbf93b92a.jpg", "Tejidos en fibra de palma natural.", "PRODUCTO"),
            CulturalItem(16, "Escultura Mini", "$80k", "https://i.pinimg.com/1200x/ae/33/c3/ae33c3125a84013394a19b8b500dd19a.jpg", "Talla pequeña para espacios modernos.", "PRODUCTO"),

            // EVENTOS & EDUCACIÓN
            CulturalItem(17, "Procesión Sacra", "Tradición", "https://i.pinimg.com/1200x/02/c7/9e/02c79ea838bd9506a93beff15f95ed7a.jpg", "Vivencia única de la Semana Santa payanesa.", "EVENTO"),
            CulturalItem(18, "Noche de Museos", "Cultura", "https://i.pinimg.com/1200x/09/e7/6b/09e76b993810b9ed224dc945184616c1.jpg", "Puertas abiertas a la historia de la ciudad.", "EVENTO"),
            CulturalItem(19, "Gastronomía", "Sabor", "https://i.pinimg.com/736x/8b/3b/59/8b3b5933e3ff1f0220dafcaaac71a834.jpg", "Degustación de pipián y carantanta.", "EVENTO"),
            CulturalItem(20, "Puente Humilladero", "Historia", "https://i.pinimg.com/1200x/bd/c9/91/bdc99164ee0dd710585ddc73cf2308b2.jpg", "Recorrido guiado por el ícono de Popayán.", "EVENTO"),
            CulturalItem(21, "Música Sacra", "Concierto", "https://i.pinimg.com/736x/a7/a9/9c/a7a99caccdb3ed0d620a53084d7991d5.jpg", "Melodías barrocas en las iglesias blancas.", "EVENTO"),
            CulturalItem(22, "Taller de Barro", "Edu", "https://i.pinimg.com/1200x/ee/9f/7f/ee9f7fb6e91ac7f5952dce61fed8840f.jpg", "Clase magistral de modelado tradicional.", "EDUCACION"),
            CulturalItem(23, "Danza Andina", "Evento", "https://i.pinimg.com/736x/9d/6a/11/9d6a11059dc7d6b30f6bb991f6018c2d.jpg", "Presentación de grupos folclóricos regionales.", "EVENTO"),
            CulturalItem(24, "Cine al Parque", "Evento", "https://i.pinimg.com/1200x/9c/a2/b8/9ca2b82a285189bd28f0d4ee8fdabdd5.jpg", "Cine bajo las estrellas del sector histórico.", "EVENTO")
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            CarruselPopayan()

            Spacer(modifier = Modifier.height(32.dp))

            // 1. MAESTROS (Basado en popular_artists view)
            SeccionLayout("Maestros del Cauca", "RAÍCES VIVAS")
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(datosHome.filter { it.categoria == "ARTISTA" }) { item ->
                    ArtCard(item.titulo, item.info, item.imagenUrl) {
                        selectedItem = item
                        showBottomSheet = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 2. POP STORE (Basado en products table)
            SeccionLayout("Pop Store", "LO NUESTRO", mostrarVerTodo = true)
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(datosHome.filter { it.categoria == "PRODUCTO" }) { item ->
                    // Feedback visual: Si el precio es alto, simulamos 'Featured' de la DB
                    ArtCard(item.titulo, item.info, item.imagenUrl) {
                        selectedItem = item
                        showBottomSheet = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. AGENDA & SABER (Basado en upcoming_events y educational_content_view)
            SeccionLayout("Agenda & Saber", "DESCUBRE Y APRENDE")
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(datosHome.filter { it.categoria == "EVENTO" || it.categoria == "EDUCACION" }) { item ->
                    ArtCard(
                        nombre = item.titulo,
                        etiqueta = if(item.categoria == "EDUCACION") "Nivel: Básico" else "A 1.2 km", // Mejora de Location
                        urlImagen = item.imagenUrl
                    ) {
                        selectedItem = item
                        showBottomSheet = true
                    }
                }
            }
        }

        // --- BOTTOM SHEET DE DETALLE ---
        if (showBottomSheet && selectedItem != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 48.dp)) {
                    Text(selectedItem!!.categoria, color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp)
                    Text(selectedItem!!.titulo, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

                    // Mejoras basadas en DB
                    if(selectedItem!!.categoria == "PRODUCTO") {
                        Text("Disponibilidad: En Stock", color = Color.Green.copy(0.7f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(selectedItem!!.descripcion, color = Color.White.copy(alpha = 0.7f), lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAcento),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("CONOCER MÁS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CarruselPopayan() {
    val imagenesPopayan = listOf(
        "https://i.pinimg.com/1200x/17/d6/9f/17d69f8fae014ee87261ecb20a4702c2.jpg",
        "https://i.pinimg.com/736x/48/b1/9c/48b19cbac47db10be6e71d1fe4849b9a.jpg",
        "https://i.pinimg.com/736x/a0/40/9d/a0409d3b3de331b2c4c45cb2943db8ec.jpg",
        "https://i.pinimg.com/1200x/f0/e4/ab/f0e4abce10532cb8fd7943db5ff2bd40.jpg"
    )

    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(5000)
            currentImageIndex = (currentImageIndex + 1) % imagenesPopayan.size
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
        AnimatedContent(
            targetState = currentImageIndex,
            transitionSpec = { fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(1500)) },
            label = "FullCarousel"
        ) { index ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagenesPopayan[index])
                    .setHeader("User-Agent", "Mozilla/5.0")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)), startY = 600f)))

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
            Text("Arte & Cultura", style = TextStyle(color = Color.White.copy(alpha = 0.7f), fontStyle = FontStyle.Italic, fontSize = 18.sp))
            Text("POPAYÁN", style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 48.sp, letterSpacing = (-2).sp))
            Box(modifier = Modifier.width(60.dp).height(4.dp).background(VioletAcento))
        }
    }
}

@Composable
fun SeccionLayout(titulo: String, subtitulo: String, mostrarVerTodo: Boolean = false) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(subtitulo, style = TextStyle(color = Color.White.copy(alpha = 0.3f), letterSpacing = 5.sp, fontSize = 10.sp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(titulo, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            if (mostrarVerTodo) {
                Text(
                    text = "VER TODO",
                    color = VioletAcento,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable { /* Aquí irá la navegación a StoreScreen */ }
                )
            }
        }
    }
}