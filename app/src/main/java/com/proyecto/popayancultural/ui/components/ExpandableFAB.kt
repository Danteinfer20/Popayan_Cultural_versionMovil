package com.proyecto.popayancultural.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.popayancultural.ui.theme.*

/**
 * COMPONENTE: EXPANDABLE FAB (LA BURBUJA)
 * Orquestador de navegación secundaria con estética Dark Modern.
 * Ubicación: com.proyecto.popayancultural.ui.components.components
 */
@Composable
fun ExpandableFAB(
    onNavigate: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-menú animado con despliegue vertical
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically() + scaleIn(),
            exit = fadeOut() + shrinkVertically() + scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ORDEN ESTRATÉGICO: El primero en el código aparece ARRIBA del todo.

                // 1. E-Learning (Prioridad 1)
                FabSubItem(label = "Aprende", icon = Icons.Default.School) {
                    expanded = false
                    onNavigate("aprende")
                }

                // 2. Directorio Humano (Prioridad 2)
                FabSubItem(label = "Artesanos", icon = Icons.Default.Person) {
                    expanded = false
                    onNavigate("artesanos")
                }

                // 3. Soporte (Utilidad)
                FabSubItem(label = "Ayuda", icon = Icons.Default.Help) {
                    expanded = false
                    onNavigate("ayuda")
                }

                // 4. Institucional (Base)
                FabSubItem(label = "Acerca de", icon = Icons.Default.Info) {
                    expanded = false
                    onNavigate("acerca")
                }
            }
        }

        // Botón Gatillo Principal (VioletAcento)
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = VioletAcento,
            contentColor = TextPrimary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Expandir menú",
                modifier = Modifier.rotate(if (expanded) 45f else 0f)
            )
        }
    }
}

@Composable
fun FabSubItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Etiqueta flotante con Glassmorphism
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold
            )
        }

        // Nodo circular de la opción
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = CardBackground,
            contentColor = VioletAcento,
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}