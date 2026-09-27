package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DrishtiPrimary

@Composable
fun RecaptureGuidanceDialog(
    onDismiss: () -> Unit,
    onRetakeNow: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Fundus Imaging Protocol & Guidance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ensure reliable AI classification with clinical-grade input",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                GuidanceStep(
                    icon = Icons.Default.CenterFocusStrong,
                    title = "1. Pupil Alignment & Fixation",
                    description = "Position the patient's forehead firmly on the chinrest. Instruct them to fixate steadily on the green internal cross/target. Align the optical objective lens directly with the pupil entrance."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.Lightbulb,
                    title = "2. Illumination & Glare Prevention",
                    description = "Dim ambient examination room lighting. If illumination is insufficient or pupil is small (< 3.5mm), wait 3-5 minutes for dark adaptation. Avoid corneal crescent reflections by centering the working distance."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.RemoveRedEye,
                    title = "3. Focus & Diopter Compensation",
                    description = "Adjust the diopter correction wheel until retinal blood vessels and foveal reflex appear tack-sharp. Prevent patient blinking by asking them to blink once right before image trigger."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStep(
                    icon = Icons.Default.CameraAlt,
                    title = "4. Coverage & Field of View",
                    description = "Ensure standard 45° macular-centered or optic disc-centered field. The circular retina should occupy at least 65-80% of the sensor frame."
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRetakeNow,
                        colors = ButtonDefaults.buttonColors(containerColor = DrishtiPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake Photo")
                    }
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
                .background(DrishtiPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DrishtiPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
