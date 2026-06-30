package com.proyecto.popayancultural.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.ProfileViewModel
import com.proyecto.popayancultural.ui.viewmodels.SettingsTab

// ─────────────────────────────────────────────────────────────
//  SETTINGS SCREEN  — compartida entre todos los roles
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    val avatarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.onAvatarSelected(it) } }

    val coverLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { viewModel.onCoverSelected(it) } }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Perfil sincronizado")
            viewModel.clearSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("❌ $it")
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = BackgroundDeep,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(data.visuals.message, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── HERO: Portada + Avatar ────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                AsyncImage(
                    model = uiState.pendingCoverUri ?: profile.coverPicture.ifEmpty { null },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, BackgroundDeep), startY = 120f)
                    )
                )
                // Botón atrás
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .background(CardBackground.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                // Botón cambiar portada
                OutlinedButton(
                    onClick = { coverLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackground.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.Image, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PORTADA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                // Avatar editable
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(BackgroundDeep, CircleShape)
                            .padding(5.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = uiState.pendingAvatarUri
                                ?: profile.profilePicture.ifEmpty {
                                    "https://ui-avatars.com/api/?name=${profile.name}&background=111115&color=a855f7"
                                },
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(
                        onClick = { avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .background(VioletAcento, CircleShape)
                    ) {
                        Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // ── NOMBRE ───────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = profile.name.uppercase().ifEmpty { "MI PERFIL" },
                    color = Color.White, fontSize = 22.sp,
                    fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "@${profile.username}",
                    color = VioletAcento, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── TAB ROW (4 tabs) ─────────────────────────────────
            SettingsTabRow(
                selected = uiState.activeSettingsTab,
                onSelect = { viewModel.onTabSelected(it) }
            )

            Spacer(Modifier.height(24.dp))

            // ── CONTENIDO DINÁMICO ────────────────────────────────
            AnimatedContent(
                targetState = uiState.activeSettingsTab,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { 40 })
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    SettingsTab.IDENTITY -> IdentityTab(
                        name         = profile.name,
                        phone        = profile.phone,
                        city         = profile.city,
                        neighborhood = profile.neighborhood,
                        bio          = profile.bio,
                        onNameChange         = viewModel::onNameChange,
                        onPhoneChange        = viewModel::onPhoneChange,
                        onCityChange         = viewModel::onCityChange,
                        onNeighborhoodChange = viewModel::onNeighborhoodChange,
                        onBioChange          = viewModel::onBioChange
                    )
                    SettingsTab.CONTACT -> ContactTab(
                        website   = profile.website,
                        instagram = profile.socialMedia.instagram,
                        facebook  = profile.socialMedia.facebook,
                        whatsapp  = profile.socialMedia.whatsapp,
                        twitter   = profile.socialMedia.twitter,
                        onWebsiteChange   = viewModel::onWebsiteChange,
                        onInstagramChange = viewModel::onInstagramChange,
                        onFacebookChange  = viewModel::onFacebookChange,
                        onWhatsappChange  = viewModel::onWhatsappChange,
                        onTwitterChange   = viewModel::onTwitterChange
                    )
                    SettingsTab.PRIVACY -> PrivacyTab(
                        publicProfile      = profile.settings.publicProfile,
                        emailNotifications = profile.settings.emailNotifications,
                        nearbyEvents       = profile.settings.nearbyEventsNotify,
                        onPublicProfileChange      = viewModel::onPublicProfileChange,
                        onEmailNotificationsChange = viewModel::onEmailNotificationsChange,
                        onNearbyEventsChange       = viewModel::onNearbyEventsChange
                    )
                    SettingsTab.PREFERENCES -> PreferencesTab(
                        language      = profile.settings.language,
                        visualMode    = profile.settings.visualMode,
                        onLanguageChange   = viewModel::onLanguageChange,
                        onVisualModeChange = viewModel::onVisualModeChange
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── BOTÓN GUARDAR ─────────────────────────────────────
            Button(
                onClick  = { viewModel.saveProfile() },
                enabled  = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = CardBackground,
                    disabledContainerColor = CardBackground.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, VioletAcento.copy(alpha = 0.4f))
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = VioletAcento, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("SINCRONIZANDO...", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                } else {
                    Icon(Icons.Outlined.Save, null, tint = VioletAcento, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("GUARDAR CAMBIOS", color = VioletAcento, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB ROW — 4 tabs en scroll horizontal
// ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsTabRow(
    selected: SettingsTab,
    onSelect: (SettingsTab) -> Unit
) {
    val tabs = listOf(
        SettingsTab.IDENTITY    to "IDENTIDAD",
        SettingsTab.CONTACT     to "CONTACTO",
        SettingsTab.PRIVACY     to "PRIVACIDAD",
        SettingsTab.PREFERENCES to "PREFERENCIAS"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(if (isSelected) VioletAcento else CardBackground)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    color      = if (isSelected) Color.White else Color.Gray,
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB: IDENTIDAD
// ─────────────────────────────────────────────────────────────

@Composable
private fun IdentityTab(
    name: String, phone: String, city: String,
    neighborhood: String, bio: String,
    onNameChange: (String) -> Unit, onPhoneChange: (String) -> Unit,
    onCityChange: (String) -> Unit, onNeighborhoodChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsTextField(value = name,         label = "Nombre Real",        icon = Icons.Outlined.Person,     onValueChange = onNameChange)
        SettingsTextField(value = phone,        label = "Teléfono",           icon = Icons.Outlined.Phone,      onValueChange = onPhoneChange)
        SettingsTextField(value = city,         label = "Ciudad",             icon = Icons.Outlined.LocationOn, onValueChange = onCityChange)
        SettingsTextField(value = neighborhood, label = "Barrio / Localidad", icon = null,                      onValueChange = onNeighborhoodChange)
        FieldLabel(text = "Biografía Cultural")
        OutlinedTextField(
            value = bio, onValueChange = onBioChange,
            placeholder = { Text("Cuéntale a Popayán sobre ti...", color = Color.Gray, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = settingsTextFieldColors(),
            minLines = 4, maxLines = 6
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB: CONTACTO
// ─────────────────────────────────────────────────────────────

@Composable
private fun ContactTab(
    website: String, instagram: String, facebook: String,
    whatsapp: String, twitter: String,
    onWebsiteChange: (String) -> Unit, onInstagramChange: (String) -> Unit,
    onFacebookChange: (String) -> Unit, onWhatsappChange: (String) -> Unit,
    onTwitterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsTextField(value = website,   label = "Sitio Web / Portafolio", icon = Icons.Outlined.Link,  placeholder = "https://miportafolio.com", onValueChange = onWebsiteChange)
        SettingsTextField(value = instagram, label = "Instagram",              icon = Icons.Outlined.Tag,   placeholder = "@usuario",                onValueChange = onInstagramChange)
        SettingsTextField(value = facebook,  label = "Facebook",               icon = Icons.Outlined.Facebook, placeholder = "Enlace a tu perfil",   onValueChange = onFacebookChange)
        SettingsTextField(value = whatsapp,  label = "WhatsApp",               icon = Icons.Outlined.Phone, placeholder = "+57 300 000 0000",         onValueChange = onWhatsappChange)
        SettingsTextField(value = twitter,   label = "Twitter / X",            icon = Icons.Outlined.Tag,   placeholder = "@usuario",                onValueChange = onTwitterChange)
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB: PRIVACIDAD
// ─────────────────────────────────────────────────────────────

@Composable
private fun PrivacyTab(
    publicProfile: Boolean, emailNotifications: Boolean, nearbyEvents: Boolean,
    onPublicProfileChange: (Boolean) -> Unit,
    onEmailNotificationsChange: (Boolean) -> Unit,
    onNearbyEventsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrivacyToggleRow(
            icon    = Icons.Outlined.Public,
            title   = "Perfil Público",
            subtitle = "Permite que otros usuarios te encuentren",
            checked = publicProfile, onCheckedChange = onPublicProfileChange
        )
        PrivacyToggleRow(
            icon    = Icons.Outlined.Notifications,
            title   = "Notificaciones de Red",
            subtitle = "Avisos sobre interacciones y actividad",
            checked = emailNotifications, onCheckedChange = onEmailNotificationsChange
        )
        PrivacyToggleRow(
            icon    = Icons.Outlined.NearMe,
            title   = "Eventos Cercanos",
            subtitle = "Notificaciones de eventos en tu zona",
            checked = nearbyEvents, onCheckedChange = onNearbyEventsChange
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  TAB: PREFERENCIAS  (NUEVO)
// ─────────────────────────────────────────────────────────────

@Composable
private fun PreferencesTab(
    language      : String,
    visualMode    : String,
    onLanguageChange   : (String) -> Unit,
    onVisualModeChange : (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Idioma ────────────────────────────────────────────
        PreferenceSelector(
            icon    = Icons.Outlined.Language,
            title   = "Idioma del Sistema",
            options = listOf("es" to "Español", "en" to "English"),
            selected = language,
            onSelect = onLanguageChange
        )
        // ── Modo visual ───────────────────────────────────────
        PreferenceSelector(
            icon    = Icons.Outlined.DarkMode,
            title   = "Entorno Visual",
            options = listOf("dark" to "Oscuro", "light" to "Claro"),
            selected = visualMode,
            onSelect = onVisualModeChange
        )
    }
}

@Composable
private fun PreferenceSelector(
    icon    : ImageVector,
    title   : String,
    options : List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = VioletAcento, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title.uppercase(),
                    color = Color.White, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { (value, label) ->
                    val isSelected = selected == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) VioletAcento else BackgroundDeep)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) VioletAcento else Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            color      = if (isSelected) Color.White else Color.Gray,
                            fontSize   = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TOGGLE ROW
// ─────────────────────────────────────────────────────────────

@Composable
private fun PrivacyToggleRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    checked : Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(BackgroundDeep),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray,  fontSize = 11.sp, lineHeight = 14.sp)
            }
            Switch(
                checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = VioletAcento
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SHARED COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text  = text.uppercase(),
        color = Color.Gray, fontSize = 9.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsTextField(
    value        : String,
    label        : String,
    icon         : ImageVector?,
    placeholder  : String = "",
    onValueChange: (String) -> Unit
) {
    Column {
        FieldLabel(text = label)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder  = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 13.sp) },
            leadingIcon  = icon?.let { { Icon(it, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) } },
            modifier     = Modifier.fillMaxWidth(),
            shape        = RoundedCornerShape(20.dp),
            colors       = settingsTextFieldColors(),
            singleLine   = true
        )
    }
}

@Composable
private fun settingsTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor      = VioletAcento,
    unfocusedBorderColor    = Color.White.copy(alpha = 0.06f),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White
)