package com.proyecto.popayancultural.ui.screens.agenda

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.proyecto.popayancultural.data.models.EventSummary
import com.proyecto.popayancultural.ui.AgendaUiState
import com.proyecto.popayancultural.ui.AgendaViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Design Tokens Premium ───────────────────────────────────────────────────
private val BgPrimary    = Color(0xFF050505) // Negro matemático
private val BgCard       = Color(0xFF0A0A0C) // Profundidad de tarjeta
private val BgCardAlt    = Color(0xFF16161A) // Acento de contraste sutil
private val AccentViolet = Color(0xFFA855F7) // Púrpura principal
private val AccentAmber  = Color(0xFFF59E0B)
private val TextPrimary  = Color(0xFFF9FAFB)
private val TextSecondary= Color(0xFF9CA3AF)
private val TextMuted    = Color(0xFF6B7280)
private val TextDim      = Color(0xFF4B5563)
private val DividerColor = Color.White.copy(alpha = 0.05f) // Efecto cristal
private val BorderCard   = Color.White.copy(alpha = 0.05f) // Borde cristalino
private val GreenConfirm = Color(0xFF4ADE80)
private val GreenBg      = Color(0x99052E16)
private val GreenBorder  = Color(0x334ADE80)
private val VioletBg     = Color(0x1AA855F7) // Púrpura translúcido (10%)
private val VioletText   = Color(0xFFD8B4FE)
private val VioletBorder = AccentViolet.copy(alpha = 0.3f)
private val AmberBg      = Color(0x1AF59E0B)
private val AmberText    = Color(0xFFFBBF24)
private val AmberBorder  = AccentAmber.copy(alpha = 0.3f)
// ─────────────────────────────────────────────────────────────────────────────

private enum class AgendaFilter(val label: String, val icon: ImageVector) {
    ALL("Todos",      Icons.Outlined.GridView),
    MUSIC("Música",   Icons.Outlined.MusicNote),
    ART("Arte",       Icons.Outlined.Palette),
    THEATER("Teatro", Icons.Outlined.TheaterComedy),
    FREE("Gratuitos", Icons.Outlined.ConfirmationNumber)
}

// ─────────────────────────────────────────────────────────────────────────────
//  RAÍZ DE PANTALLA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AgendaScreen(
    onEventClick: (Int) -> Unit,
    viewModel: AgendaViewModel = viewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val selectedDate   by viewModel.selectedDate.collectAsState()
    val daysWithEvents by viewModel.daysWithEvents.collectAsState()

    var activeFilter by remember { mutableStateOf(AgendaFilter.ALL) }
    var searchQuery  by remember { mutableStateOf("") }
    var calExpanded  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        AgendaHeader(
            searchQuery    = searchQuery,
            onSearchChange = { searchQuery = it },
            activeFilter   = activeFilter,
            onFilterChange = { activeFilter = it }
        )

        CollapsibleCalendar(
            selectedDate   = selectedDate,
            daysWithEvents = daysWithEvents,
            uiState        = uiState,
            expanded       = calExpanded,
            onToggle       = { calExpanded = !calExpanded },
            onDateSelected = {
                viewModel.selectDate(it)
                calExpanded = false
            }
        )

        HorizontalDivider(
            color     = DividerColor,
            thickness = 1.dp,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )

        AnimatedContent(
            targetState  = uiState,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label        = "agenda_content"
        ) { state ->
            when (state) {
                is AgendaUiState.Loading -> AgendaLoadingState()
                is AgendaUiState.Empty   -> {
                    val isToday = selectedDate == LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    AgendaEmptyState(isToday = isToday) {
                        viewModel.selectDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                }
                is AgendaUiState.Success -> {
                    val filtered = remember(state.events, activeFilter, searchQuery) {
                        state.events
                            .filter { event ->
                                when (activeFilter) {
                                    AgendaFilter.FREE    -> event.isFree
                                    AgendaFilter.MUSIC   -> event.title.contains("música", ignoreCase = true) ||
                                            event.title.contains("concierto", ignoreCase = true) ||
                                            event.title.contains("musical", ignoreCase = true)
                                    AgendaFilter.ART     -> event.title.contains("arte", ignoreCase = true) ||
                                            event.title.contains("exposición", ignoreCase = true) ||
                                            event.title.contains("pintura", ignoreCase = true)
                                    AgendaFilter.THEATER -> event.title.contains("teatro", ignoreCase = true) ||
                                            event.title.contains("obra", ignoreCase = true) ||
                                            event.title.contains("danza", ignoreCase = true)
                                    AgendaFilter.ALL     -> true
                                }
                            }
                            .filter { event ->
                                searchQuery.isBlank() ||
                                        event.title.contains(searchQuery, ignoreCase = true) ||
                                        event.location?.name?.contains(searchQuery, ignoreCase = true) == true
                            }
                    }
                    EventList(
                        events       = filtered,
                        selectedDate = selectedDate,
                        onEventClick = onEventClick
                    )
                }
                is AgendaUiState.Error -> AgendaErrorState(state.message)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AgendaHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilter: AgendaFilter,
    onFilterChange: (AgendaFilter) -> Unit
) {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentViolet))
            Spacer(Modifier.width(8.dp))
            Text(
                "CARTELERA OFICIAL",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = AccentViolet,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Agenda ",  fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            // Estética editorial aplicada
            Text("Cultural", fontSize = 32.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, color = AccentAmber)
        }

        Text(
            "Descubre los eventos que transforman Popayán",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Surface(
            shape    = RoundedCornerShape(16.dp),
            color    = BgCard,
            border   = BorderStroke(1.dp, BorderCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null,
                    tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text("Buscar eventos, lugares...", fontSize = 14.sp, color = TextDim)
                    }
                    BasicTextField(
                        value         = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine    = true,
                        textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    LazyRow(
        contentPadding        = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier              = Modifier.padding(bottom = 16.dp)
    ) {
        items(AgendaFilter.entries) { filter ->
            FilterChip(
                filter   = filter,
                selected = filter == activeFilter,
                onClick  = { onFilterChange(filter) }
            )
        }
    }
}

@Composable
private fun FilterChip(filter: AgendaFilter, selected: Boolean, onClick: () -> Unit) {
    val bg     = if (selected) AccentViolet else BgCard
    val border = if (selected) AccentViolet else BorderCard
    val tint   = if (selected) Color.White  else TextMuted

    Surface(
        shape    = RoundedCornerShape(24.dp),
        color    = bg,
        border   = BorderStroke(1.dp, border),
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(filter.icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(filter.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CALENDARIO ACORDEÓN (ESTÁNDAR 24dp)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CollapsibleCalendar(
    selectedDate   : String,
    daysWithEvents : Set<String>,
    uiState        : AgendaUiState,
    expanded       : Boolean,
    onToggle       : () -> Unit,
    onDateSelected : (String) -> Unit
) {
    val dateLabel = remember(selectedDate) {
        runCatching {
            val d   = LocalDate.parse(selectedDate)
            val fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
            d.format(fmt).replaceFirstChar { it.uppercase() }
        }.getOrDefault(selectedDate)
    }

    val eventCount = when (uiState) {
        is AgendaUiState.Success -> uiState.events.size
        else -> 0
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "chevron"
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

        Surface(
            shape    = if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(24.dp),
            color    = BgCard,
            border   = BorderStroke(1.dp, if (expanded) VioletBorder else BorderCard),
            modifier = Modifier
                .fillMaxWidth()
                .clip(if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(24.dp))
                .clickable(onClick = onToggle)
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VioletBg)
                        .border(1.dp, VioletBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint     = AccentViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = dateLabel,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                    Text(
                        text     = if (expanded) "Toca para cerrar el calendario" else "Toca para ver el mes",
                        fontSize = 12.sp,
                        color    = TextMuted
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (eventCount > 0) {
                    Surface(
                        shape  = RoundedCornerShape(20.dp),
                        color  = VioletBg,
                        border = BorderStroke(1.dp, VioletBorder)
                    ) {
                        Text(
                            text       = "$eventCount ${if (eventCount == 1) "evento" else "eventos"}",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = VioletText,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = BgCardAlt,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint     = TextSecondary,
                            modifier = Modifier.size(18.dp).rotate(chevronRotation)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically(tween(300)) + fadeIn(tween(200)),
            exit    = shrinkVertically(tween(250)) + fadeOut(tween(150))
        ) {
            MonthlyCalendarGrid(
                selectedDate   = selectedDate,
                daysWithEvents = daysWithEvents,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun MonthlyCalendarGrid(
    selectedDate   : String,
    daysWithEvents : Set<String>,
    onDateSelected : (String) -> Unit
) {
    val today         = LocalDate.now()
    val selectedLocal = runCatching { LocalDate.parse(selectedDate) }.getOrDefault(today)
    var displayMonth  by remember { mutableStateOf(YearMonth.from(selectedLocal)) }

    val monthName = displayMonth
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es")))
        .replaceFirstChar { it.uppercase() }

    Surface(
        shape  = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        color  = BgCard,
        border = BorderStroke(1.dp, VioletBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(monthName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalNavButton(Icons.Outlined.ChevronLeft)  { displayMonth = displayMonth.minusMonths(1) }
                    CalNavButton(Icons.Outlined.ChevronRight) { displayMonth = displayMonth.plusMonths(1) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("D", "L", "M", "X", "J", "V", "S").forEach { label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(label, fontSize = 11.sp, color = TextDim, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val firstDay    = displayMonth.atDay(1)
            val startOffset = firstDay.dayOfWeek.value % 7
            val daysInMonth = displayMonth.lengthOfMonth()
            val rows        = (startOffset + daysInMonth + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayNum  = row * 7 + col - startOffset + 1
                        val isValid = dayNum in 1..daysInMonth
                        val day     = if (isValid) displayMonth.atDay(dayNum) else null
                        val dateStr = day?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: ""

                        Box(
                            modifier         = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValid && day != null) {
                                DayCell(
                                    day        = day,
                                    isSelected = dateStr == selectedDate,
                                    isToday    = day == today,
                                    hasEvents  = dateStr in daysWithEvents,
                                    onClick    = { onDateSelected(dateStr) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalNavButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = BgCardAlt,
        border   = BorderStroke(1.dp, BorderCard),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DayCell(
    day        : LocalDate,
    isSelected : Boolean,
    isToday    : Boolean,
    hasEvents  : Boolean,
    onClick    : () -> Unit
) {
    val bgColor = when {
        isSelected -> AccentViolet
        isToday    -> BgCardAlt
        else       -> Color.Transparent
    }
    val borderColor = when {
        isSelected -> AccentViolet
        isToday    -> BorderCard
        else       -> Color.Transparent
    }
    val numColor = when {
        isSelected || isToday -> Color.White
        hasEvents             -> Color.White
        else                  -> TextMuted
    }
    val pipColor = when {
        isSelected -> Color.White
        isToday    -> AccentViolet
        hasEvents  -> AccentViolet
        else       -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = day.dayOfMonth.toString(),
            fontSize   = 13.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color      = numColor
        )
        Spacer(Modifier.height(2.dp))
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(pipColor))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LISTA DE EVENTOS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EventList(
    events       : List<EventSummary>,
    selectedDate : String,
    onEventClick : (Int) -> Unit
) {
    val displayLabel = remember(selectedDate) {
        runCatching {
            val d     = LocalDate.parse(selectedDate)
            val today = LocalDate.now()
            val fmt   = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
            when (d) {
                today             -> "HOY · ${d.format(fmt).uppercase()}"
                today.plusDays(1) -> "MAÑANA · ${d.format(fmt).uppercase()}"
                else              -> d.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es"))).uppercase()
            }
        }.getOrDefault(selectedDate)
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(displayLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor, thickness = 1.dp)
                Text("${events.size} eventos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentViolet)
            }
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BgCard)
                        .border(1.dp, BorderCard, RoundedCornerShape(24.dp))
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = TextDim, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Sin resultados para este filtro.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                EventCard(event = event, onClick = { onEventClick(event.id) })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA DE EVENTO (ESTÁNDAR 24dp)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EventCard(event: EventSummary, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color  = BgCard,
        shape  = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                AsyncImage(
                    model              = event.coverImage ?: "https://placehold.co/600x300/0D0D18/A855F7?text=",
                    contentDescription = event.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1f to BgCard.copy(alpha = 0.9f)
                        )
                    )
                )

                Row(
                    modifier              = Modifier.align(Alignment.TopStart).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.isFree) {
                        EventBadge("Gratuito", GreenBg, GreenConfirm, GreenBorder)
                    } else {
                        event.price?.let {
                            EventBadge("$ ${"%.0f".format(it)}", AmberBg, AmberText, AmberBorder)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape    = RoundedCornerShape(10.dp),
                    color    = BgPrimary.copy(alpha = 0.9f),
                    border   = BorderStroke(1.dp, BorderCard)
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = AccentViolet, modifier = Modifier.size(13.dp))
                        Text(event.startTime, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text     = event.location?.name ?: "—",
                        fontSize = 12.sp, color = TextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text       = event.title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    lineHeight = 22.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = if (event.isFree) "Entrada libre" else "$ ${event.price?.let { "%.0f".format(it) } ?: "—"}",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (event.isFree) GreenConfirm else AmberText
                    )
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = VioletBg,
                        border   = BorderStroke(1.dp, VioletBorder),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.ArrowForward, contentDescription = "Ver", tint = VioletText, modifier = Modifier.size(16.dp))
                            Text("Ver", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VioletText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventBadge(label: String, bg: Color, textColor: Color, border: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = bg, border = BorderStroke(1.dp, border)) {
        Text(
            text          = label,
            color         = textColor,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ESTADOS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AgendaLoadingState() {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(3) { EventCardSkeleton() } }
}

@Composable
private fun EventCardSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        color    = BgCard,
        shape    = RoundedCornerShape(24.dp),
        border   = BorderStroke(1.dp, BorderCard)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(BgCardAlt))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.fillMaxWidth(0.5f).height(12.dp).background(BgCardAlt, RoundedCornerShape(6.dp)))
                Box(Modifier.fillMaxWidth(0.9f).height(16.dp).background(BgCardAlt, RoundedCornerShape(6.dp)))
                Box(Modifier.fillMaxWidth(0.4f).height(12.dp).background(BgCardAlt, RoundedCornerShape(6.dp)))
            }
        }
    }
}

@Composable
private fun AgendaEmptyState(isToday: Boolean, onGoToday: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
            .border(1.dp, BorderCard, RoundedCornerShape(24.dp))
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextDim, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Sin eventos para esta fecha.", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)

            // Corrección UX: Solo mostrar botón si NO estamos en "Hoy"
            if (!isToday) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = VioletBg,
                    border   = BorderStroke(1.dp, VioletBorder),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onGoToday)
                ) {
                    Text("Ver eventos de hoy", color = VioletText, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun AgendaErrorState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextSecondary, fontSize = 15.sp)
        }
    }
}