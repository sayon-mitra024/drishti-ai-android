package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun RecaptureGuidanceDialog(
    onDismiss: () -> Unit,
    onRetakeNow: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shapeRadius = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Imaging Protocol & Guidance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
                Text(
                    text = "Achieving optimal optical quality for AI screening",
                    fontSize = 12.sp,
                    color = NeoInkSoft
                )

                Spacer(modifier = Modifier.height(16.dp))

                GuidanceStep(
                    icon = Icons.Default.CenterFocusStrong,
                    title = "1. Pupil Alignment & Fixation",
                    description = "Position patient with forehead firmly against headrest. Instruct patient to fixate steadily on the optical center target."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.Lightbulb,
                    title = "2. Illumination & Glare Prevention",
                    description = "Dim ambient examination room lighting. Wait 3-5 minutes for physiological dark adaptation. Re-angle lens slightly if crescent reflections appear."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.RemoveRedEye,
                    title = "3. Focus & Saccade Reduction",
                    description = "Ensure vascular arcades and foveal reflex appear tack-sharp. Ask patient to blink once gently right before camera trigger."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.CameraAlt,
                    title = "4. Coverage & Field of View",
                    description = "Maintain standard 45° macular or disc-centered field. The retinal circular boundary should occupy at least 70% of sensor frame."
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    NeoSecondaryButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    NeoPrimaryButton(
                        text = "Retake Photo",
                        icon = Icons.Default.CameraAlt,
                        onClick = onRetakeNow,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidanceStep(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DrishtiRoseSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DrishtiBurgundy,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = NeoInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = NeoInkSoft,
                lineHeight = 16.sp
            )
        }
    }
}
