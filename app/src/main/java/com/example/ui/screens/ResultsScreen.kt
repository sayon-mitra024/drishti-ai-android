package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.GradCamDisplayMode
import com.example.ui.components.GradCamViewer
import com.example.ui.components.ProbabilityDistributionChart
import com.example.ui.theme.DrishtiPrimary

@Composable
fun ResultsScreen(
    analysisResult: AnalysisResult,
    originalBitmap: Bitmap?,
    patientInfo: PatientInfo,
    gradCamOpacity: Float,
    gradCamDisplayMode: GradCamDisplayMode,
    showLesionMarkers: Boolean,
    clinicianReview: ClinicianReview,
    onOpacityChange: (Float) -> Unit,
    onDisplayModeChange: (GradCamDisplayMode) -> Unit,
    onToggleMarkers: () -> Unit,
    onOpenClinicianReview: () -> Unit,
    onNewScreening: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val grade = clinicianReview.assignedGrade ?: analysisResult.predictedGrade

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090607))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Patient & Exam Summary Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = patientInfo.fullName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "ID: ${patientInfo.patientId} • Age: ${patientInfo.age} yrs • DM: ${patientInfo.diabetesDurationYears} yrs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DrishtiPrimary,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "EYE: ${patientInfo.eyeSide.name}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Result Severity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = grade.severityColor.copy(alpha = 0.12f)
            ),
            border = BorderStroke(2.dp, grade.severityColor.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(grade.severityColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ICDR CLASSIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = grade.severityColor,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Confidence Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = grade.severityColor.copy(alpha = 0.25f),
                        contentColor = grade.severityColor
                    ) {
                        Text(
                            text = "${(analysisResult.confidence * 100).toInt()}% Confidence",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = grade.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = grade.shortName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = grade.severityColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = grade.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Referral Urgency Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (grade.isReferable) Color(0xFFEF4444).copy(alpha = 0.16f) else Color(0xFF10B981).copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        if (grade.isReferable) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (grade.isReferable) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (grade.isReferable) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (grade.isReferable) "REFERABLE DIABETIC RETINOPATHY" else "NON-REFERABLE STATUS",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (grade.isReferable) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                            Text(
                                text = "Guideline Action: ${grade.followUpWindow}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grad-CAM Explainability Viewer
        GradCamViewer(
            originalBitmap = originalBitmap,
            heatmapBitmap = analysisResult.gradCamBitmap,
            opacity = gradCamOpacity,
            displayMode = gradCamDisplayMode,
            lesionCues = analysisResult.lesionCues,
            showLesionMarkers = showLesionMarkers,
            onOpacityChange = onOpacityChange,
            onDisplayModeChange = onDisplayModeChange,
            onToggleMarkers = onToggleMarkers
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Probability Distribution
        ProbabilityDistributionChart(
            probabilities = analysisResult.probabilityDistribution,
            predictedGrade = analysisResult.predictedGrade
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pathological Lesion Cues Card
        if (analysisResult.lesionCues.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Detected Pathological Cues",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Grad-CAM focal activations mapped to anatomical retinal structures",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    analysisResult.lesionCues.forEach { cue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (cue.type) {
                                            "Microaneurysm" -> Color(0xFFEF4444)
                                            "Hemorrhage" -> Color(0xFFDC2626)
                                            "Hard Exudate" -> Color(0xFFFBBF24)
                                            else -> Color(0xFF38BDF8)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cue.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Region: ${cue.region} • Severity: ${cue.severityLevel}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Referral & Clinical Recommendations Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = DrishtiPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clinical Action Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = grade.referralRecommendation,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                grade.clinicalFindings.forEach { finding ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = finding,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clinician Review & Sign-Off Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (clinicianReview.status != ReviewStatus.PENDING) {
                    Color(0xFF0F766E).copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
            ),
            border = BorderStroke(
                1.dp,
                if (clinicianReview.status != ReviewStatus.PENDING) DrishtiPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AssignmentInd,
                            contentDescription = null,
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clinician Oversight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (clinicianReview.status != ReviewStatus.PENDING) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                        contentColor = if (clinicianReview.status != ReviewStatus.PENDING) Color(0xFF10B981) else Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = clinicianReview.status.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (clinicianReview.status != ReviewStatus.PENDING) {
                    Text(
                        text = "Reviewed & Signed by: ${clinicianReview.clinicianName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (clinicianReview.clinicianId.isNotBlank()) {
                        Text(
                            text = "Registration: ${clinicianReview.clinicianId}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (clinicianReview.clinicalNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: \"${clinicianReview.clinicalNotes}\"",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Awaiting verification by a qualified ophthalmologist or retinal screening specialist.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenClinicianReview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (clinicianReview.status != ReviewStatus.PENDING) MaterialTheme.colorScheme.surfaceVariant else DrishtiPrimary,
                        contentColor = if (clinicianReview.status != ReviewStatus.PENDING) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_clinician_review_button")
                ) {
                    Icon(
                        imageVector = if (clinicianReview.status != ReviewStatus.PENDING) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (clinicianReview.status != ReviewStatus.PENDING) "Modify Clinical Review" else "Sign Off as Clinician"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export / Share & Action Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            """
                            DRISHTI AI RETINAL SCREENING REPORT
                            Patient: ${patientInfo.fullName} (ID: ${patientInfo.patientId})
                            Eye Examined: ${patientInfo.eyeSide.name}
                            ICDR Finding: ${grade.title} (${grade.shortName})
                            Confidence: ${(analysisResult.confidence * 100).toInt()}%
                            Referable DR: ${if (grade.isReferable) "YES (Urgent Action)" else "NO (Routine Annual Follow-up)"}
                            Action Plan: ${grade.referralRecommendation}
                            Clinician Status: ${clinicianReview.status.name} (${clinicianReview.clinicianName.ifBlank { "Unsigned" }})
                            
                            *AI-assisted screening support tool, not a diagnostic device. Requires qualified clinical oversight.*
                            """.trimIndent()
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Drishti Screening Report"))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("share_report_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNewScreening,
                colors = ButtonDefaults.buttonColors(containerColor = DrishtiPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("new_screening_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Patient")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("view_all_history_button")
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("View All Stored Screenings in Room Database")
        }
    }
}
