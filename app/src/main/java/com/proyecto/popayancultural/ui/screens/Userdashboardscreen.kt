package com.proyecto.popayancultural.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*
import com.proyecto.popayancultural.ui.viewmodels.UserProfile

// ─────────────────────────────────────────────────────────────
//  VISITOR DASHBOARD
// ─────────────────────────────────────────────────────────────

@Composable
fun UserDashboardScreen(
    profile: UserProfile,
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onApplyAsCreator: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Animación de entrada escalonada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── HERO: Portada + Avatar ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Portada
                AsyncImage(
                    model = profile.coverPicture.ifEmpty {
                        "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"
                    },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente sobre portada
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, BackgroundDeep),
                                startY = 120f
                            )
                        )
                )
                // Botón de ajustes (top-end)
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .background(CardBackground.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = Color.White)
                }
                // Avatar centrado en el bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                        .size(100.dp)
                        .background(BackgroundDeep, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = profile.profilePicture.ifEmpty {
                            "https://ui-avatars.com/api/?name=${profile.name}&background=111115&color=a855f7"
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── NOMBRE Y ROL ──────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 20 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = profile.name.uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = VioletAcento,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NODO POPAYÁN · VISITANTE",
                            color = VioletAcento,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── CTA: TALLER DE CREADORES ──────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 30 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VioletAcento.copy(alpha = 0.25f), RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Resplandor decorativo
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(200.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                VioletAcento.copy(alpha = 0.08f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Column(modifier = Modifier.padding(24.dp)) {
                                // Badge de estatus
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(VioletAcento.copy(alpha = 0.1f))
                                        .border(1.dp, VioletAcento.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = VioletAcento,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ESTATUS: CIUDADANO CULTURAL",
                                        color = VioletAcento,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "¿LISTO PARA EL TALLER DE CREADORES?",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Si eres artesano, gestor de eventos o educador, formaliza tu presencia y obtén acceso al Taller Creativo.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onApplyAsCreator,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletAcento),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(
                                        text = "INICIAR POSTULACIÓN",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── GRID DE ACCESOS RÁPIDOS ───────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.FavoriteBorder,
                        iconColor = Color(0xFFEF4444),
                        title = "Inspiración",
                        subtitle = "Obras guardadas",
                        onClick = onNavigateToFavorites
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ShoppingBag,
                        iconColor = Color(0xFF10B981),
                        title = "Adquisiciones",
                        subtitle = "Historial de compras",
                        onClick = onNavigateToPurchases
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // espacio para el FAB/navbar
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  COMPONENTE: Tarjeta de acceso rápido
// ─────────────────────────────────────────────────────────────

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDeep)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}