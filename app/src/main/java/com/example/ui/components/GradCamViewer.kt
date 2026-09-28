package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.*

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
    NeoCard(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "AI Attention / CAM",
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Attention / CAM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = DrishtiRoseSubtle,
                border = BorderStroke(1.dp, DrishtiRoseBorder)
            ) {
                Text(
                    text = "EXPLAINABILITY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = DrishtiBurgundy,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segmented Control Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SegmentTab(
                label = "Original",
                isSelected = displayMode == GradCamDisplayMode.ORIGINAL,
                onClick = { onDisplayModeChange(GradCamDisplayMode.ORIGINAL) },
                modifier = Modifier.weight(1f)
            )
            SegmentTab(
                label = "CAM Overlay",
                isSelected = displayMode == GradCamDisplayMode.OVERLAY,
                onClick = { onDisplayModeChange(GradCamDisplayMode.OVERLAY) },
                modifier = Modifier.weight(1f)
            )
            SegmentTab(
                label = "Attention Only",
                isSelected = displayMode == GradCamDisplayMode.HEATMAP_ONLY,
                onClick = { onDisplayModeChange(GradCamDisplayMode.HEATMAP_ONLY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Viewport
        NeoSunkWell(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shapeRadius = 14.dp,
            backgroundColor = Color.Black,
            borderColor = NeoBorderStrong
        ) {
            if (originalBitmap != null && displayMode != GradCamDisplayMode.HEATMAP_ONLY) {
                Image(
                    bitmap = originalBitmap.asImageBitmap(),
                    contentDescription = "Retinal Fundus Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (heatmapBitmap != null && displayMode != GradCamDisplayMode.ORIGINAL) {
                Image(
                    bitmap = heatmapBitmap.asImageBitmap(),
                    contentDescription = "Class Activation Map Thermal Overlay",
                    contentScale = ContentScale.Fit,
                    alpha = if (displayMode == GradCamDisplayMode.HEATMAP_ONLY) 1.0f else opacity,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (heatmapBitmap == null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "Explanation unavailable. Retinal image displayed.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (displayMode == GradCamDisplayMode.OVERLAY && heatmapBitmap != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CAM Opacity:", fontSize = 11.sp, color = NeoInkSoft)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = DrishtiBurgundy,
                        activeTrackColor = DrishtiBurgundy,
                        inactiveTrackColor = NeoBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gradcam_opacity_slider")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${(opacity * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeoInk)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clinical Saliency Explanation Caption
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = NeoInkFaint,
                modifier = Modifier
                    .size(14.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Highlighted regions represent image areas contributing to the model's classification. AI-assisted research visualization, not a proof of clinical lesion.",
                fontSize = 11.sp,
                color = NeoInkSoft,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SegmentTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) DrishtiBurgundy else NeoSurfaceSunk,
        border = BorderStroke(1.dp, if (isSelected) DrishtiBurgundy else NeoBorder),
        modifier = modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else NeoInk
            )
        }
    }
}
