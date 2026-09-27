package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PatientInfo
import com.example.model.QualityCheckResult
import com.example.model.QualityStatus
import com.example.ui.components.QualityMetricCard
import com.example.ui.theme.DrishtiPrimary

@Composable
fun QualityCheckScreen(
    bitmap: Bitmap?,
    qualityResult: QualityCheckResult?,
    patientInfo: PatientInfo,
    onProceed: () -> Unit,
    onRetake: () -> Unit,
    onOpenGuidance: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (qualityResult == null || bitmap == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DrishtiPrimary)
        }
        return
    }

    val overallColor = when (qualityResult.overallStatus) {
        QualityStatus.PASS -> Color(0xFF10B981)
        QualityStatus.WARNING -> Color(0xFFF59E0B)
        QualityStatus.FAIL -> Color(0xFFEF4444)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090607))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Patient & Eye Context Header
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DrishtiPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${patientInfo.fullName} (${patientInfo.patientId})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DrishtiPrimary.copy(alpha = 0.2f),
                    contentColor = DrishtiPrimary
                ) {
                    Text(
                        text = "Eye: ${patientInfo.eyeSide.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Preview & Overall Status Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, overallColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Fundus Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = overallColor.copy(alpha = 0.92f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (qualityResult.overallStatus) {
                                QualityStatus.PASS -> Icons.Default.CheckCircle
                                QualityStatus.WARNING -> Icons.Default.Warning
                                QualityStatus.FAIL -> Icons.Default.Cancel
                            }
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (qualityResult.overallStatus) {
                                    QualityStatus.PASS -> "IMAGE QUALITY PASSED"
                                    QualityStatus.WARNING -> "QUALITY REVIEW ADVISED"
                                    QualityStatus.FAIL -> "QUALITY REJECTED"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Guidance summary text
                Text(
                    text = qualityResult.guidanceMessages.firstOrNull() ?: "Image satisfies clinical screening standards.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality Metrics Breakdown Section
        Text(
            text = "Automated Quality Assessment",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "4-point ophthalmic validation criteria before neural inference",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Focus & Sharpness
        QualityMetricCard(metric = qualityResult.focusMetric)
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Illumination & Contrast
        QualityMetricCard(metric = qualityResult.illuminationMetric)
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Retinal Field Suitability
        QualityMetricCard(metric = qualityResult.suitabilityMetric)
        Spacer(modifier = Modifier.height(8.dp))

        // 4. Resolution
        QualityMetricCard(metric = qualityResult.resolutionMetric)

        Spacer(modifier = Modifier.height(16.dp))

        // Troubleshooting / Imaging Guidance Button
        OutlinedButton(
            onClick = onOpenGuidance,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recapture_guidance_button")
        ) {
            Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Imaging & Recapture Guidance")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Action Controls
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("retake_image_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retake Photo")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onProceed,
                enabled = qualityResult.isAcceptableForAnalysis,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DrishtiPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("proceed_analysis_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (qualityResult.overallStatus == QualityStatus.FAIL) "Quality Too Low" else "Run AI Analysis",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
