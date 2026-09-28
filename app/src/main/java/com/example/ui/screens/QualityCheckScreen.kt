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
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoPrimaryButton
import com.example.ui.components.NeoSecondaryButton
import com.example.ui.components.NeoSunkWell
import com.example.ui.theme.*

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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NeoBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DrishtiBurgundy)
        }
        return
    }

    val overallColor = when (qualityResult.overallStatus) {
        QualityStatus.PASS -> Grade0Color
        QualityStatus.WARNING -> Grade2Color
        QualityStatus.FAIL -> Grade3Color
    }

    val overallBg = when (qualityResult.overallStatus) {
        QualityStatus.PASS -> Grade0Bg
        QualityStatus.WARNING -> Grade2Bg
        QualityStatus.FAIL -> Grade3Bg
    }

    val canProceed = qualityResult.overallStatus != QualityStatus.FAIL

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space16, vertical = Space16)
    ) {
        // Patient Header Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeoSurface,
            border = BorderStroke(1.dp, NeoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space16, vertical = Space12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DrishtiBurgundy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Space8))
                    Text(
                        text = "${patientInfo.fullName} (${patientInfo.patientId})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeoInk
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DrishtiRoseSubtle,
                    border = BorderStroke(1.dp, DrishtiRoseBorder)
                ) {
                    Text(
                        text = "EYE: ${patientInfo.eyeSide.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DrishtiBurgundy,
                        modifier = Modifier.padding(horizontal = Space8, vertical = Space4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Retinal Image Frame Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            NeoSunkWell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shapeRadius = 14.dp,
                backgroundColor = Color.Black,
                borderColor = NeoBorderStrong
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Retinal Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Quality Status Banner Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = overallBg,
            borderColor = overallColor.copy(alpha = 0.5f),
            contentPadding = PaddingValues(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(overallColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (qualityResult.overallStatus) {
                            QualityStatus.PASS -> Icons.Default.CheckCircle
                            QualityStatus.WARNING -> Icons.Default.Warning
                            QualityStatus.FAIL -> Icons.Default.Dangerous
                        },
                        contentDescription = null,
                        tint = overallColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Space12))

                Column {
                    Text(
                        text = "Image Quality: ${qualityResult.overallStatus.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = overallColor
                    )
                    Text(
                        text = when (qualityResult.overallStatus) {
                            QualityStatus.PASS -> "All clinical metrics passed. Image is optimal for inference."
                            QualityStatus.WARNING -> "Minor focus or illumination variance. Acceptable for screening."
                            QualityStatus.FAIL -> "Insufficient focus, lighting, or coverage. Retake required."
                        },
                        fontSize = 11.5.sp,
                        color = NeoInkSoft
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Quality Metrics Breakdown
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            Text(
                text = "Imaging Metrics Validation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeoInk
            )

            Spacer(modifier = Modifier.height(Space8))

            MetricRow(
                label = "Focus & Sharpness",
                score = qualityResult.focusMetric.score.toInt(),
                status = qualityResult.focusMetric.status.name,
                isPass = qualityResult.focusMetric.status == QualityStatus.PASS
            )

            Divider(color = NeoBorder, modifier = Modifier.padding(vertical = Space4))

            MetricRow(
                label = "Illumination & Uniformity",
                score = qualityResult.illuminationMetric.score.toInt(),
                status = qualityResult.illuminationMetric.status.name,
                isPass = qualityResult.illuminationMetric.status == QualityStatus.PASS
            )

            Divider(color = NeoBorder, modifier = Modifier.padding(vertical = Space4))

            MetricRow(
                label = "Retinal Field Suitability",
                score = qualityResult.suitabilityMetric.score.toInt(),
                status = qualityResult.suitabilityMetric.status.name,
                isPass = qualityResult.suitabilityMetric.status == QualityStatus.PASS
            )
        }

        Spacer(modifier = Modifier.height(Space16))

        // Clinical Recommendation Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = NeoSurfaceSunk,
            contentPadding = PaddingValues(Space16)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Space8))
                Text(
                    text = "Recommendation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = NeoInk
                )
            }
            Spacer(modifier = Modifier.height(Space4))
            val recText = if (qualityResult.guidanceMessages.isNotEmpty()) {
                qualityResult.guidanceMessages.joinToString("\n• ", prefix = "• ")
            } else {
                qualityResult.suitabilityMetric.feedback
            }
            Text(
                text = recText,
                fontSize = 12.sp,
                color = NeoInkSoft,
                lineHeight = 16.sp
            )

            if (!canProceed) {
                Spacer(modifier = Modifier.height(Space8))
                TextButton(
                    onClick = onOpenGuidance,
                    modifier = Modifier.testTag("open_guidance_button")
                ) {
                    Text(
                        text = "View Retake & Optical Positioning Guidelines",
                        color = DrishtiBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Space8)
        ) {
            if (canProceed) {
                NeoPrimaryButton(
                    text = "Proceed to AI Analysis",
                    icon = Icons.Default.Check,
                    onClick = onProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proceed_to_analysis_button")
                )
            }

            NeoSecondaryButton(
                text = if (canProceed) "Retake Image" else "Retake Required",
                icon = Icons.Default.Refresh,
                onClick = onRetake,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retake_image_button")
            )
        }

        Spacer(modifier = Modifier.height(Space24))
    }
}

@Composable
private fun MetricRow(label: String, score: Int, status: String, isPass: Boolean) {
    val pillBg = if (isPass) Grade0Bg else Grade2Bg
    val pillColor = if (isPass) Grade0Color else Grade2Color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeoInk)
            Text(text = "Clinical Threshold Score: $score / 100", fontSize = 11.sp, color = NeoInkFaint)
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = pillBg,
            border = BorderStroke(1.dp, pillColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = pillColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
