package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.proyecto.popayancultural.ui.theme.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen() {
    // --- ESTADOS PARA PANELES (SHEETS) ---
    val detailSheetState = rememberModalBottomSheetState()
    var showDetailSheet by remember { mutableStateOf(false) }

    val cartSheetState = rememberModalBottomSheetState()
    var showCartSheet by remember { mutableStateOf(false) }

    var selectedProduct by remember { mutableStateOf<CulturalItem?>(null) }

    // --- ESTADOS DE INTERACCIÓN ---
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Cerámica", "Textil", "Arte", "Decoración")

    // Lista reactiva para el carrito
    val carritoItems = remember { mutableStateListOf<CulturalItem>() }

    val productosStore = remember {
        listOf(
            CulturalItem(1, "Vasija de Barro", "$48.000", "https://i.pinimg.com/1200x/d0/30/a5/d030a570e8eb20a0a2f9a7dc42c09a67.jpg", "Pieza artesanal moldeada a mano con técnicas de alfarería tradicional.", "Cerámica"),
            CulturalItem(2, "Telar de Cintura", "$95.000", "https://i.pinimg.com/736x/4b/80/9a/4b809a5f59e757ea0848fdad21f65672.jpg", "Tejido en lana de oveja con pigmentos orgánicos del Cauca.", "Textil"),
            CulturalItem(3, "Cúpulas de Popayán", "$150.000", "https://i.pinimg.com/1200x/d5/6d/84/d56d84c8cfa1bec6cfdae6f2035757bf.jpg", "Fotografía artística del sector histórico en formato profesional.", "Arte"),
            CulturalItem(4, "Cuenco Ancestral", "$32.000", "https://i.pinimg.com/1200x/37/38/25/37382553cfd6220c0fd840b2284f1c23.jpg", "Cuenco utilitario de barro oscuro extraído de la zona rural.", "Cerámica"),
            CulturalItem(5, "Escultura de Santo", "$320.000", "https://i.pinimg.com/1200x/77/c8/50/77c85029d8118ae7d17af7e82da42903.jpg", "Talla en madera policromada inspirada en la imaginería religiosa.", "Arte"),
            CulturalItem(6, "Plato de la Ciudad", "$55.000", "https://i.pinimg.com/736x/8f/87/5d/8f875d81f3723f88c15c401f05ec5dce.jpg", "Plato decorativo pintado con motivos de la arquitectura blanca.", "Decoración"),
            CulturalItem(7, "Ventanal Colonial", "$110.000", "https://i.pinimg.com/736x/1c/40/d0/1c40d04ec412ee8b7269fd16e9462b96.jpg", "Ilustración detallada de los ventanales de hierro forjado.", "Arte"),
            CulturalItem(8, "Textura de Tejas", "$80.000", "https://i.pinimg.com/1200x/7c/5a/1c/7c5a1c247354feeca6a36079d59857f9.jpg", "Composición fotográfica de los techos coloniales de la ciudad.", "Arte"),
            CulturalItem(9, "Mochila Auténtica", "$140.000", "https://i.pinimg.com/736x/11/1d/2c/111d2cb145cd0349a272b423f59d255a.jpg", "Mochila tejida a mano con diseño de rombos ancestrales.", "Textil"),
            CulturalItem(10, "Callejón de Noche", "$135.000", "https://i.pinimg.com/736x/c7/64/1d/c7641d938201e25293d85cd9f93d2cb4.jpg", "Captura nocturna del sector histórico con iluminación cálida.", "Arte"),
            CulturalItem(11, "Detalle de Barro", "$42.000", "https://i.pinimg.com/736x/53/62/45/536245c10cd38b467ca38bb982febe38.jpg", "Miniatura artesanal para coleccionistas de arte popular.", "Cerámica")
        )
    }

    // --- LÓGICA DE FILTRADO ---
    val productosFiltrados = productosStore.filter {
        (categoriaSeleccionada == "Todos" || it.categoria == categoriaSeleccionada) &&
                (it.titulo.contains(searchQuery, ignoreCase = true))
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("POPAYÁN CULTURAL", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp)
                    Text("POP STORE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                // Carrito Clickable
                BadgedBox(
                    modifier = Modifier.clickable { showCartSheet = true },
                    badge = {
                        if(carritoItems.isNotEmpty()) {
                            Badge(containerColor = VioletAcento) { Text(carritoItems.size.toString(), color = Color.White) }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, "Ver Carrito", tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }

            // --- 2. BARRA DE BÚSQUEDA REFINADA (Más delgada) ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                placeholder = { Text("Buscar piezas...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = VioletAcento, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedBorderColor = VioletAcento.copy(0.5f),
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // --- 3. CHIPS DE CATEGORÍA ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categorias) { cat ->
                    val isSelected = categoriaSeleccionada == cat
                    Surface(
                        onClick = { categoriaSeleccionada = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) VioletAcento else CardBackground,
                        border = BorderStroke(1.dp, Color.White.copy(0.05f))
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // --- 4. GRILLA ESCALONADA ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp
            ) {
                items(productosFiltrados) { producto ->
                    BoutiqueItem(producto) {
                        selectedProduct = producto
                        showDetailSheet = true
                    }
                }
            }
        }

        // --- BOTTOM SHEET: DETALLE DEL PRODUCTO ---
        if (showDetailSheet && selectedProduct != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetailSheet = false },
                sheetState = detailSheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 40.dp)) {
                    Text(selectedProduct!!.categoria, color = VioletAcento, style = MaterialTheme.typography.labelSmall)
                    Text(selectedProduct!!.titulo, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(selectedProduct!!.info, color = VioletAcento, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(selectedProduct!!.descripcion, color = Color.White.copy(alpha = 0.6f), lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            carritoItems.add(selectedProduct!!)
                            showDetailSheet = false
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAcento),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("AGREGAR AL CARRITO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- BOTTOM SHEET: VISTA DEL CARRITO ---
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = cartSheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 40.dp)) {
                    Text("TU CARRITO", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (carritoItems.isEmpty()) {
                        Text("Aún no has agregado tesoros.", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(carritoItems) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.titulo, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(item.info, color = VioletAcento, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { carritoItems.remove(item) }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.7f))
                                    }
                                }
                                HorizontalDivider(color = Color.White.copy(0.05f))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { /* Lógica de pago */ },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text("FINALIZAR PEDIDO", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoutiqueItem(item: CulturalItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(35.dp))
            .background(CardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(35.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imagenUrl)
                .setHeader("User-Agent", "Mozilla/5.0")
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(35.dp)),
            contentScale = ContentScale.FillWidth
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(item.info, color = VioletAcento, fontSize = 12.sp)
        }
    }
}