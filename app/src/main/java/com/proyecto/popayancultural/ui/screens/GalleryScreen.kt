package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- MODELO CON DESCRIPCIÓN Y COMENTARIOS SIMULADOS ---
data class GalleryPost(
    val id: Int,
    val imageUrl: String,
    val category: String,
    val authorName: String,
    val description: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isAiRecommended: Boolean = false,
    val topComments: List<Pair<String, String>> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDetail by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<GalleryPost?>(null) }

    val tagsDB = listOf("Todos", "Semana Santa", "Arquitectura", "Artesanía", "Vida Local")
    var tagActive by remember { mutableStateOf("Todos") }

    val descBase = "Una pieza que captura la esencia inquebrantable del Cauca. Sus texturas y sombras revelan años de tradición conservada intacta ante el paso del tiempo."
    val comentariosMuestra = listOf(
        Pair("Ana P.", "Increíble cómo capturaste esa luz."),
        Pair("Carlos G.", "Me recuerda a mi infancia en el centro."),
        Pair("Luz M.", "Una obra digna de exposición nacional.")
    )

    val dbPosts = remember {
        listOf(
            GalleryPost(1, "https://i.pinimg.com/1200x/17/d6/9f/17d69f8fae014ee87261ecb20a4702c2.jpg", "Semana Santa", "Maestro Solarte", descBase, 124, 12, true, comentariosMuestra),
            GalleryPost(2, "https://i.pinimg.com/736x/48/b1/9c/48b19cbac47db10be6e71d1fe4849b9a.jpg", "Arquitectura", "Ana López", "Fachadas que guardan el eco de siglos pasados.", 89, 4, false, comentariosMuestra.take(1)),
            GalleryPost(3, "https://i.pinimg.com/1200x/0f/41/9c/0f419c38373d6a579e7dfeb8fe09cfb1.jpg", "Artesanía", "Taller Chamba", "El barro hecho poesía. Trabajo manual de más de 40 horas.", 342, 45, true, comentariosMuestra),
            GalleryPost(4, "https://i.pinimg.com/736x/a0/40/9d/a0409d3b3de331b2c4c45cb2943db8ec.jpg", "Arquitectura", "Carlos Ruiz", descBase, 56, 2),
            GalleryPost(5, "https://i.pinimg.com/1200x/f0/e4/ab/f0e4abce10532cb8fd7943db5ff2bd40.jpg", "Semana Santa", "Cofradía", "El silencio absoluto de la noche procesional.", 210, 18, false, comentariosMuestra.take(2)),
            GalleryPost(6, "https://i.pinimg.com/1200x/bd/c9/91/bdc99164ee0dd710585ddc73cf2308b2.jpg", "Vida Local", "Foto Popayán", descBase, 15, 0),
            GalleryPost(7, "https://i.pinimg.com/736x/1c/40/d0/1c40d04ec412ee8b7269fd16e9462b96.jpg", "Arquitectura", "Ana López", descBase, 430, 67, true, comentariosMuestra),
            GalleryPost(8, "https://i.pinimg.com/1200x/61/29/2f/61292f01e939938b2b0103115100295e.jpg", "Artesanía", "Maestro Solarte", "Filigrana que imita la naturaleza del macizo.", 112, 5, false, comentariosMuestra.take(1)),
            GalleryPost(9, "https://i.pinimg.com/1200x/02/c7/9e/02c79ea838bd9506a93beff15f95ed7a.jpg", "Semana Santa", "Archivo Histórico", descBase, 500, 120, false, comentariosMuestra),
            GalleryPost(10, "https://i.pinimg.com/736x/c7/64/1d/c7641d938201e25293d85cd9f93d2cb4.jpg", "Arquitectura", "Carlos Ruiz", descBase, 88, 3),
            GalleryPost(11, "https://i.pinimg.com/1200x/a3/73/8c/a3738c1cfba8c83c3a92ed67aeb5119e.jpg", "Artesanía", "Taller Chamba", descBase, 25, 1),
            GalleryPost(12, "https://i.pinimg.com/1200x/09/e7/6b/09e76b993810b9ed224dc945184616c1.jpg", "Vida Local", "Foto Popayán", descBase, 67, 8, true),
            GalleryPost(13, "https://i.pinimg.com/736x/8b/3b/59/8b3b5933e3ff1f0220dafcaaac71a834.jpg", "Arquitectura", "Ana López", descBase, 32, 0),
            GalleryPost(14, "https://i.pinimg.com/1200x/ee/9f/7f/ee9f7fb6e91ac7f5952dce61fed8840f.jpg", "Semana Santa", "Cofradía", descBase, 190, 22, false, comentariosMuestra),
            GalleryPost(15, "https://i.pinimg.com/736x/9d/6a/11/9d6a11059dc7d6b30f6bb991f6018c2d.jpg", "Vida Local", "Foto Popayán", descBase, 45, 2),
            GalleryPost(16, "https://i.pinimg.com/1200x/ae/33/c3/ae33c3125a84013394a19b8b500dd19a.jpg", "Artesanía", "Maestro Solarte", descBase, 78, 4),
            GalleryPost(17, "https://i.pinimg.com/1200x/9c/a2/b8/9ca2b82a285189bd28f0d4ee8fdabdd5.jpg", "Arquitectura", "Carlos Ruiz", descBase, 150, 15, true, comentariosMuestra.take(2)),
            GalleryPost(18, "https://i.pinimg.com/1200x/d5/6d/84/d56d84c8cfa1bec6cfdae6f2035757bf.jpg", "Semana Santa", "Archivo Histórico", descBase, 340, 50),
            GalleryPost(19, "https://i.pinimg.com/736x/11/1d/2c/111d2cb145cd0349a272b423f59d255a.jpg", "Vida Local", "Ana López", descBase, 21, 1),
            GalleryPost(20, "https://i.pinimg.com/1200x/37/38/25/37382553cfd6220c0fd840b2284f1c23.jpg", "Artesanía", "Taller Chamba", descBase, 99, 9)
        )
    }

    val postsFiltrados = if (tagActive == "Todos") dbPosts else dbPosts.filter { it.category == tagActive }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ARCHIVO PATRIMONIAL", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp)
                    Text("GALERÍA", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = (-2).sp)
                }
                Text("${postsFiltrados.size} OBRAS", color = Color.White.copy(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // --- FILTROS ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tagsDB) { tag ->
                    val isSelected = tagActive == tag
                    Surface(
                        modifier = Modifier.clickable { tagActive = tag }.clip(RoundedCornerShape(12.dp)),
                        color = if (isSelected) VioletAcento else CardBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- GRILLA ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp
            ) {
                itemsIndexed(postsFiltrados, key = { _, post -> post.id }) { index, post ->
                    GlassGalleryCard(post = post, index = index) {
                        selectedPost = post
                        showDetail = true
                    }
                }
            }
        }

        // --- BOTTOM SHEET INTERACTIVO ---
        if (showDetail && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetail = false },
                sheetState = sheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                PostDetailSheet(selectedPost!!)
            }
        }
    }
}

// --- CARD: "POLAROID GLASSMÓRFICA" ---
@Composable
fun GlassGalleryCard(post: GalleryPost, index: Int, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var showHeartAnim by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(index * 40L)
        visible = true
    }

    val scaleState by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "Press")

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 50 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleState)
                .clip(RoundedCornerShape(30.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() },
                        onDoubleTap = {
                            coroutineScope.launch {
                                showHeartAnim = true
                                delay(800)
                                showHeartAnim = false
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            if (post.isAiRecommended) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(VioletAcento, RoundedCornerShape(6.dp))) {
                    Text("★ PARA TI", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }

            AnimatedVisibility(
                visible = showHeartAnim,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioHighBouncy)),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f))))
                    .padding(12.dp)
            ) {
                Column {
                    Text(post.authorName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = VioletAcento, modifier = Modifier.size(10.dp))
                        Text(" ${post.likesCount}", color = Color.White.copy(0.7f), fontSize = 9.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(10.dp))
                        Text(" ${post.commentsCount}", color = Color.White.copy(0.7f), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// --- DETALLE: IMPLEMENTACIÓN DE PESTAÑAS (TABS) ---
@Composable
fun PostDetailSheet(post: GalleryPost) {
    var isSaved by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) } // 0 = Relato, 1 = Comunidad

    // Agregamos verticalScroll para que funcione si el contenido es largo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        // IMAGEN DESTACADA
        Box(modifier = Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(30.dp))) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                color = VioletAcento,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(post.category, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACCIONES
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            ActionIconButton(Icons.Default.FavoriteBorder, "Inspirar", Color.White) {}
            ActionIconButton(Icons.Default.Refresh, "Repost", Color.White) {}
            ActionIconButton(
                if (isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                if (isSaved) "Guardado" else "Guardar",
                if (isSaved) VioletAcento else Color.White
            ) { isSaved = !isSaved }
            ActionIconButton(Icons.Default.Share, "Compartir", Color.White) {}
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- SISTEMA DE PESTAÑAS (DESPLAZABLE INTERNO) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.05f), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            CustomTab(
                title = "RELATO VISUAL",
                isSelected = currentTab == 0,
                modifier = Modifier.weight(1f)
            ) { currentTab = 0 }

            CustomTab(
                title = "COMUNIDAD (${post.commentsCount})",
                isSelected = currentTab == 1,
                modifier = Modifier.weight(1f)
            ) { currentTab = 1 }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CONTENIDO DINÁMICO SEGÚN LA PESTAÑA ---
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "TabContent"
        ) { targetTab ->
            if (targetTab == 0) {
                // VISTA: RELATO
                Column {
                    Text("AUTOR: ${post.authorName.uppercase()}", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        post.description,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Impacto: ${post.likesCount} inspiraciones generadas.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // VISTA: COMUNIDAD (COMENTARIOS)
                Column {
                    if (post.topComments.isNotEmpty()) {
                        post.topComments.forEach { (usuario, comentario) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(usuario, color = VioletAcento, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comentario, color = Color.White.copy(0.7f), fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        Text("Aún no hay comentarios. Sé el primero en opinar.", color = Color.Gray, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input de comentario falso
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Escribe un comentario...", color = Color.Gray, fontSize = 14.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(0.1f),
                            focusedBorderColor = VioletAcento,
                            unfocusedContainerColor = CardBackground
                        )
                    )
                }
            }
        }
    }
}

// Botón de Pestaña Personalizado
@Composable
fun CustomTab(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VioletAcento else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ActionIconButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(label, modifier = Modifier.padding(top = 8.dp), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}