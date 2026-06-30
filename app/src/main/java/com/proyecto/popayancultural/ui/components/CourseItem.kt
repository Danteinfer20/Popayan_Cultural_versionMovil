package com.proyecto.popayancultural.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

@Composable
fun CourseItemCard(
    title: String,
    duration: String,
    level: String,
    imageUrl: String,
    isSpecialty: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0A0C))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Miniatura
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111113))
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().background(Color(0xFF111113))) },
                error = { Box(Modifier.fillMaxSize().background(Color(0xFF111113))) }
            )

            // Badge minimalista
            if (isSpecialty) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color(0xFFF59E0B), RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ESPECIALIDAD", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos y Metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = duration, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Text(text = "  •  ", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                Text(text = level, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    }
}