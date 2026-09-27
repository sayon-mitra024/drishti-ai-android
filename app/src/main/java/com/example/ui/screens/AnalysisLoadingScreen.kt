package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DrishtiDarkBorder
import com.example.ui.theme.DrishtiPrimary
import com.example.ui.theme.DrishtiSecondary

@Composable
fun AnalysisLoadingScreen(
    bitmap: Bitmap?,
    progressText: String,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    // Laser sweep vertical animation
    val infiniteTransition = rememberInfiniteTransition(label = "laserScan")
    val laserYPercent by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    // Pulse animation for optical reticle
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090607))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Scanning Viewport with Drishti Retinal Reticle
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFF050304))
                .border(2.dp, DrishtiPrimary.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Fundus Image
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Scanning Retina",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark vignette overlay with subtle crimson tone
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x33000000),
                                Color(0x8014080B)
                            )
                        )
                    )
            )

            // Crosshair overlay lines (matching Drishti eye logo target)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val reticleRadius = (size.width / 2f) * 0.65f * pulseScale
                val tickLength = 16.dp.toPx()
                val reticleColor = Color(0x99FF8A95)

                // Concentric circles
                drawCircle(
                    color = reticleColor,
                    radius = reticleRadius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0x66FFDADA),
                    radius = reticleRadius * 0.4f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                // 4 Crosshair ticks pointing to center
                // Top
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x, center.y - reticleRadius - tickLength),
                    end = Offset(center.x, center.y - reticleRadius + 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                // Bottom
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x, center.y + reticleRadius - 4.dp.toPx()),
                    end = Offset(center.x, center.y + reticleRadius + tickLength),
                    strokeWidth = 2.dp.toPx()
                )
                // Left
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x - reticleRadius - tickLength, center.y),
                    end = Offset(center.x - reticleRadius + 4.dp.toPx(), center.y),
                    strokeWidth = 2.dp.toPx()
                )
                // Right
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x + reticleRadius - 4.dp.toPx(), center.y),
                    end = Offset(center.x + reticleRadius + tickLength, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Scanning Red Laser Line
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val laserOffsetY = maxHeight * laserYPercent
                Box(
                    modifier = Modifier
                        .offset(y = laserOffsetY)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF8B1E2D),
                                    Color(0xFFFF8A95),
                                    Color(0xFF8B1E2D),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Model & Architecture Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1014),
            border = androidx.compose.foundation.BorderStroke(1.dp, DrishtiDarkBorder),
            contentColor = Color(0xFFFFB4BA)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = DrishtiSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EFFICIENTNET-B0 • ON-DEVICE PIPELINE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Analyzing Retinal Microvasculature",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFCF9F8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = progressText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFC7B6B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.height(44.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Smooth Progress Indicator
        LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = DrishtiPrimary,
            trackColor = Color(0xFF241519)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STANDARDIZED 224×224 INPUT",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8B7173)
            )
            Text(
                text = "${(progressPercent * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB4BA)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clinical Notice
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0x66160D10),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33DEBFBF)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF8B7173),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI-assisted screening support tool • Not a diagnostic medical device",
                    fontSize = 10.sp,
                    color = Color(0xFF9E8A8D),
                    lineHeight = 13.sp
                )
            }
        }
    }
}
