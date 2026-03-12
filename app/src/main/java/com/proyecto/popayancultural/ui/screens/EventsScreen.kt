package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.* import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EventItem(
    val id: Int,
    val title: String,
    val dateDay: String,
    val dateMonth: String,
    val time: String,
    val locationName: String,
    val imageUrl: String,
    val eventType: String,
    val price: String,
    val director: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDetail by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

    // --- ESTADOS DE FILTRO ---
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Almacenamos el día y mes filtrado (Null significa "Mostrar Todos")
    var filterDay by remember { mutableStateOf<String?>(null) }
    var filterMonth by remember { mutableStateOf<String?>(null) }

    // --- 10 DATOS SIMULADOS (Para probar el scroll y los filtros) ---
    val dbEvents = remember {
        listOf(
            EventItem(1, "Taller de Fotografía", "12", "MAR", "09:00", "Parque Caldas", "https://i.pinimg.com/1200x/bd/c9/91/bdc99164ee0dd710585ddc73cf2308b2.jpg", "Taller", "Gratis", "Colectivo Visual"),
            EventItem(2, "Exposición Rostros", "13", "MAR", "14:00", "Museo Negret", "https://i.pinimg.com/736x/11/1d/2c/111d2cb145cd0349a272b423f59d255a.jpg", "Exposición", "Gratis", null),
            EventItem(3, "Procesión del Silencio", "14", "MAR", "20:00", "Sector Histórico", "https://i.pinimg.com/1200x/02/c7/9e/02c79ea838bd9506a93beff15f95ed7a.jpg", "Tradición", "Gratis", "Junta Pro-Semana Santa"),
            EventItem(4, "Concierto Música Sacra", "14", "MAR", "18:30", "Teatro Municipal", "https://i.pinimg.com/736x/a7/a9/9c/a7a99caccdb3ed0d620a53084d7991d5.jpg", "Concierto", "$45.000", "Orquesta Sinfónica del Cauca"),
            EventItem(5, "Taller de Barro Negro", "15", "MAR", "10:00", "Casa de la Cultura", "https://i.pinimg.com/1200x/0f/41/9c/0f419c38373d6a579e7dfeb8fe09cfb1.jpg", "Educación", "$20.000", "Maestro Alfarero"),
            EventItem(6, "Obra: El Viacrucis", "15", "MAR", "19:00", "Teatro Guillermo Valencia", "https://i.pinimg.com/1200x/f0/e4/ab/f0e4abce10532cb8fd7943db5ff2bd40.jpg", "Artes Escénicas", "$15.000", "Compañía Teatral Caucana"),
            EventItem(7, "Noche de Museos", "16", "MAR", "19:00", "Red de Museos", "https://i.pinimg.com/1200x/09/e7/6b/09e76b993810b9ed224dc945184616c1.jpg", "Exposición", "Gratis", null),
            EventItem(8, "Danza Andina Folclórica", "16", "MAR", "16:00", "Rincón Payanés", "https://i.pinimg.com/736x/9d/6a/11/9d6a11059dc7d6b30f6bb991f6018c2d.jpg", "Danza", "Gratis", "Ballet Folclórico"),
            EventItem(9, "Cine bajo las Estrellas", "17", "MAR", "19:30", "Puente Humilladero", "https://i.pinimg.com/1200x/9c/a2/b8/9ca2b82a285189bd28f0d4ee8fdabdd5.jpg", "Cine", "Gratis", null),
            EventItem(10, "Clausura Festival", "18", "MAR", "21:00", "Plaza de Toros", "https://i.pinimg.com/1200x/17/d6/9f/17d69f8fae014ee87261ecb20a4702c2.jpg", "Concierto", "$60.000", "Artistas Varios")
        )
    }

    // --- LÓGICA DE FILTRADO (Buscador + Fecha) ---
    val filteredEvents = dbEvents.filter { event ->
        val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) || event.locationName.contains(searchQuery, ignoreCase = true)
        val matchesDate = if (filterDay != null && filterMonth != null) {
            event.dateDay == filterDay && event.dateMonth == filterMonth
        } else {
            true // Si no hay fecha seleccionada, muestra todos
        }
        matchesSearch && matchesDate
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER PROFESIONAL (Título + Buscador + Calendario) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDeep)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("AGENDA", color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 4.sp)
                Text("CULTURAL", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // BARRA DE BÚSQUEDA
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(30.dp)),
                        placeholder = { Text("Buscar eventos, lugares...", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = VioletAcento) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // BOTÓN DEL CALENDARIO
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .size(56.dp)
                            .background(if (filterDay != null) VioletAcento else CardBackground, CircleShape)
                            .border(1.dp, if (filterDay != null) Color.Transparent else Color.White.copy(0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = Color.White)
                    }
                }

                // CHIP DE FILTRO ACTIVO (Aparece solo si hay fecha seleccionada)
                AnimatedVisibility(visible = filterDay != null) {
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VioletAcento.copy(alpha = 0.2f))
                            .clickable {
                                filterDay = null
                                filterMonth = null
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fecha: $filterDay $filterMonth", color = VioletAcento, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Close, contentDescription = "Quitar filtro", tint = VioletAcento, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // --- LISTA DE TICKETS ANIMADOS ---
            if (filteredEvents.isEmpty()) {
                // ESTADO VACÍO
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White.copy(0.1f), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay eventos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("No encontramos programación para esta fecha o búsqueda.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(count = filteredEvents.size) { index ->
                        val event = filteredEvents[index]
                        EventTicketCard(event, index) {
                            selectedEvent = event
                            showDetail = true
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO DEL CALENDARIO (MATERIAL 3) ---
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Ajuste para evitar desfasaje de zona horaria
                            val date = Date(millis + 12 * 60 * 60 * 1000)
                            filterDay = SimpleDateFormat("dd", Locale("es", "ES")).format(date)
                            filterMonth = SimpleDateFormat("MMM", Locale("es", "ES")).format(date).uppercase()
                        }
                        showDatePicker = false
                    }) {
                        Text("Filtrar", color = VioletAcento, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = CardBackground)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        titleContentColor = VioletAcento,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.Gray,
                        dayContentColor = Color.White,
                        selectedDayContainerColor = VioletAcento,
                        selectedDayContentColor = Color.White,
                        todayDateBorderColor = VioletAcento,
                        todayContentColor = VioletAcento
                    )
                )
            }
        }

        // --- BOTTOM SHEET DE DETALLE ---
        if (showDetail && selectedEvent != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetail = false },
                sheetState = sheetState,
                containerColor = CardBackground,
                dragHandle = { BottomSheetDefaults.DragHandle(color = VioletAcento) }
            ) {
                EventDetailSheet(selectedEvent!!)
            }
        }
    }
}

// --- EL TICKET CULTURAL (GRAYSCALE A COLOR) ---
@Composable
fun EventTicketCard(event: EventItem, index: Int, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 60L) // Aceleré un poco la animación para que 10 items carguen más fluido
        visible = true
    }

    val scaleState by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "Scale")
    val saturation by animateFloatAsState(if (isPressed) 1f else 0f, animationSpec = tween(400), label = "Color")
    val matrix = ColorMatrix().apply { setToSaturation(saturation) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 50 })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleState)
                .height(140.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(CardBackground)
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(30.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .width(85.dp)
                    .fillMaxHeight()
                    .background(VioletAcento.copy(alpha = 0.08f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(event.dateDay, color = VioletAcento, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(event.dateMonth, color = Color.White.copy(0.7f), fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(event.time, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(matrix)
                )

                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF111113), Color(0xFF111113).copy(alpha = 0.5f), Color.Transparent))))

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(event.eventType.uppercase(), color = VioletAcento, fontSize = 8.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(event.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.locationName, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).clip(RoundedCornerShape(topStart = 20.dp)),
                    color = VioletAcento
                ) {
                    Text(event.price, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun EventDetailSheet(event: EventItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        AsyncImage(
            model = event.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(30.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.eventType.uppercase(), color = VioletAcento, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Text(event.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, lineHeight = 36.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${event.dateDay} ${event.dateMonth}", color = VioletAcento, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(event.time, color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(0.02f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = VioletAcento, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ubicación", color = Color.Gray, fontSize = 10.sp)
                        Text(event.locationName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (event.director != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(0.05f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = VioletAcento, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Compañía / Director", color = Color.Gray, fontSize = 10.sp)
                            Text(event.director, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioletAcento),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Outlined.ConfirmationNumber, null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text(if(event.price == "Gratis") "CONFIRMAR ASISTENCIA" else "COMPRAR TICKET - ${event.price}", fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}