package com.proyecto.popayancultural.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.popayancultural.ui.theme.*

@Composable
fun AcercaDeScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(scrollState)
    ) {
        // 1. Hero Image (City Identity)
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1596401057633-54a8fe8ef647?w=1200", // Popayán image
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient to blend the image with the black background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, BackgroundDeep)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
        ) {
            // 2. Editorial Title
            Text(
                text = "OUR MISSION",
                color = VioletAcento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digitizing the soul of the White City.",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Manifesto
            Text(
                text = """
                    Popayán Cultural was born as a response to the need to preserve the ancestral techniques of our master artisans.
                    
                    Through this multi-platform (Web and Mobile), we seek that the legacy of wrought iron, silk weaving, and sacred cabinetmaking not only survives but thrives in the digital economy.
                """.trimIndent(),
                color = TextPrimary.copy(alpha = 0.8f),
                fontSize = 18.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Objectives (Subtle Cards)
            ObjectiveItem("Visibility", "We bring the artisan's workshop to the palm of your hand.")
            ObjectiveItem("Education", "Knowledge transfer through live workshops.")
            ObjectiveItem("Community", "Uniting creators and enthusiasts of culture.")
        }
    }
}

@Composable
fun ObjectiveItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, color = VioletAcento, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(description, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = BorderSutil, thickness = 0.5.dp)
    }
}