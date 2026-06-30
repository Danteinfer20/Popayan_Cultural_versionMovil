package com.proyecto.popayancultural.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.popayancultural.ui.theme.*

@Composable
fun AyudaScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Cabecera de Soporte
        Icon(
            imageVector = Icons.Default.SupportAgent,
            contentDescription = null,
            tint = VioletAcento,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Cómo podemos\nayudarte?",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sección de FAQ
        FaqItem(
            "¿Cómo contacto a un artesano?",
            "Puedes contactarlos directamente a través de su perfil en la sección de 'Artesanos'. Allí encontrarás sus redes sociales y ubicación de taller."
        )
        FaqItem(
            "¿Los talleres tienen costo?",
            "Algunos talleres son gratuitos apoyados por la alcaldía, mientras que otros son experiencias pagas que apoyan directamente al maestro artesano."
        )
        FaqItem(
            "¿Dónde puedo ver las obras físicas?",
            "En la sección de 'Galería' puedes ver los puntos de exposición permanentes en el Centro Histórico de Popayán."
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botón de Contacto Directo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VioletAcento),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Aún tienes dudas?", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Escríbenos a soporte@popayancultural.com",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun FaqItem(pregunta: String, respuesta: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (expanded) VioletAcento else BorderSutil)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pregunta,
                    color = if (expanded) VioletAcento else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = respuesta,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}