package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LesionCue
import com.example.ui.GradCamDisplayMode
import com.example.ui.theme.DrishtiPrimary

@Composable
fun GradCamViewer(
    originalBitmap: Bitmap?,
    heatmapBitmap: Bitmap?,
    opacity: Float,
    displayMode: GradCamDisplayMode,
    lesionCues: List<LesionCue>,
    showLesionMarkers: Boolean,
    onOpacityChange: (Float) -> Unit,
    onDisplayModeChange: (GradCamDisplayMode) -> Unit,
    onToggleMarkers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCue by remember { mutableStateOf<LesionCue?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header with mode tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Grad-CAM",
                        tint = DrishtiPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Grad-CAM Explainability",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Toggle Lesion Pins
                FilterChip(
                    selected = showLesionMarkers,
                    onClick = onToggleMarkers,
                    label = { Text("Lesion Pins", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showLesionMarkers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle lesion pins",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Mode Segmented Control
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = displayMode == GradCamDisplayMode.OVERLAY,
                    onClick = { onDisplayModeChange(GradCamDisplayMode.OVERLAY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Overlay", fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = displayMode == GradCamDisplayMode.ORIGINAL,
                    onClick = { onDisplayModeChange(GradCamDisplayMode.ORIGINAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Fundus", fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = displayMode == GradCamDisplayMode.HEATMAP_ONLY,
                    onClick = { onDisplayModeChange(GradCamDisplayMode.HEATMAP_ONLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Heatmap", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Retinal Viewing Canvas with Layering and Lesion Pins
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // 1. Original Fundus Layer
                if (originalBitmap != null && displayMode != GradCamDisplayMode.HEATMAP_ONLY) {
                    Image(
                        bitmap = originalBitmap.asImageBitmap(),
                        contentDescription = "Original Retinal Fundus",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 2. Heatmap Layer
                if (heatmapBitmap != null && displayMode != GradCamDisplayMode.ORIGINAL) {
                    val alpha = if (displayMode == GradCamDisplayMode.HEATMAP_ONLY) 1.0f else opacity
                    Image(
                        bitmap = heatmapBitmap.asImageBitmap(),
                        contentDescription = "Grad-CAM Thermal Heatmap",
                        contentScale = ContentScale.Fit,
                        alpha = alpha,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 3. Lesion Marker Pins
                if (showLesionMarkers && displayMode != GradCamDisplayMode.HEATMAP_ONLY) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = maxWidth
                        val canvasHeight = maxHeight

                        lesionCues.forEach { cue ->
                            val pinX = canvasWidth * cue.xPercent
                            val pinY = canvasHeight * cue.yPercent

                            Box(
                                modifier = Modifier
                                    .offset(x = pinX - 14.dp, y = pinY - 14.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (cue.type) {
                                            "Microaneurysm" -> Color(0xFFEF4444)
                                            "Hemorrhage" -> Color(0xFFDC2626)
                                            "Hard Exudate" -> Color(0xFFFBBF24)
                                            else -> Color(0xFF38BDF8)
                                        }.copy(alpha = 0.85f)
                                    )
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .clickable {
                                        selectedCue = if (selectedCue == cue) null else cue
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PinDrop,
                                    contentDescription = cue.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Floating Cue Details Badge
                if (selectedCue != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                    ) {
                        selectedCue?.let { cue ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xEE0F172A),
                                contentColor = Color.White,
                                border = BorderStroke(1.dp, DrishtiPrimary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cue.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Location: ${cue.region} • Severity: ${cue.severityLevel}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { selectedCue = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Text("✕", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Opacity Slider (visible when in Overlay mode)
            if (displayMode == GradCamDisplayMode.OVERLAY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overlay Opacity:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = opacity,
                        onValueChange = onOpacityChange,
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gradcam_opacity_slider")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Thermal Color Legend Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low Saliency", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0284C7),
                                    Color(0xFF10B981),
                                    Color(0xFFFBBF24),
                                    Color(0xFFEF4444)
                                )
                            )
                        )
                )
                Text("High Attention (Lesion)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
        }
    }
}
