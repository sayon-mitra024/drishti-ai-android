package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoSunkWell
import com.example.ui.theme.*

@Composable
fun AnalysisLoadingScreen(
    bitmap: Bitmap?,
    progressText: String,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Retinal Image Frame with subtle neo-skeuomorphic depth
        NeoCard(
            modifier = Modifier.size(190.dp),
            shapeRadius = 24.dp,
            contentPadding = PaddingValues(10.dp)
        ) {
            NeoSunkWell(
                modifier = Modifier.fillMaxSize(),
                shapeRadius = 16.dp,
                backgroundColor = Color.Black,
                borderColor = NeoBorderStrong
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Retinal Image Being Analyzed",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Circular Progress Ring & Percentage
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier.size(72.dp),
                color = DrishtiBurgundy,
                strokeWidth = 6.dp,
                trackColor = NeoSurfaceSunk
            )
            Text(
                text = "${(progressPercent * 100).toInt()}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeoInk
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Analyzing Retinal Microvasculature",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeoInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = progressText.ifBlank { "Grading retinal features across 5 ICDR classes..." },
            fontSize = 12.5.sp,
            color = NeoInkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // On-device AI analysis banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeoSurface,
            border = BorderStroke(1.dp, NeoBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DrishtiRoseSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = DrishtiBurgundy,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "100% On-Device Inference",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NeoInk
                    )
                    Text(
                        text = "EfficientNet-B0 ONNX • Zero cloud transmission",
                        fontSize = 11.sp,
                        color = NeoInkSoft
                    )
                }
            }
        }
    }
}
