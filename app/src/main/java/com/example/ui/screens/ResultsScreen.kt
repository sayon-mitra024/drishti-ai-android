package com.example.ui.screens

import android.content.Context
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
import com.example.model.AnalysisResult
import com.example.model.ClinicianReview
import com.example.model.DiabeticRetinopathyGrade
import com.example.model.PatientInfo
import com.example.model.ReviewStatus
import com.example.ui.GradCamDisplayMode
import com.example.ui.components.*
import com.example.ui.theme.*

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
    onViewReport: () -> Unit = {},
    onNewScreening: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val grade = clinicianReview.assignedGrade ?: analysisResult.predictedGrade

    val gradeColor = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
    }

    val gradeBg = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Bg
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Bg
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Bg
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Bg
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Bg
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space16, vertical = Space16)
    ) {
        // Patient Header Strip
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
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
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

        // PRIMARY HERO CARD: Predicted DR Grade & Severity (Visual Focus)
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = gradeBg,
            borderColor = gradeColor.copy(alpha = 0.5f),
            contentPadding = PaddingValues(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI-ASSISTED SCREENING RESULT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = gradeColor,
                    letterSpacing = 0.8.sp
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = gradeColor
                ) {
                    Text(
                        text = "GRADE ${grade.grade}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = grade.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeoInk,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Model Confidence: ${(analysisResult.confidence * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = gradeColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "•", color = NeoInkFaint)
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (grade.isReferable) Grade3Bg else Grade0Bg,
                    border = BorderStroke(1.dp, if (grade.isReferable) Grade3Color.copy(alpha = 0.5f) else Grade0Color.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (grade.isReferable) "REFERABLE DR" else "NON-REFERABLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (grade.isReferable) Grade3Color else Grade0Color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Retinal Image / CAM Attention Viewer
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

        Spacer(modifier = Modifier.height(Space16))

        // 5-Class Probability Distribution
        ProbabilityDistributionChart(
            probabilities = analysisResult.probabilityDistribution,
            predictedGrade = grade
        )

        Spacer(modifier = Modifier.height(Space16))

        // Clinical Action Plan & Referral Recommendation
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Space8))
                Text(
                    text = "Clinical Action Plan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
            }

            Spacer(modifier = Modifier.height(Space8))

            Text(
                text = grade.referralRecommendation,
                fontSize = 12.5.sp,
                color = NeoInk,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(Space8))

            grade.clinicalFindings.forEach { finding ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DrishtiBurgundy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Space8))
                    Text(
                        text = finding,
                        fontSize = 11.5.sp,
                        color = NeoInkSoft
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Clinician Review & Sign-Off Card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (clinicianReview.status != ReviewStatus.PENDING) Grade0Bg else NeoSurfaceSunk,
            borderColor = if (clinicianReview.status != ReviewStatus.PENDING) Grade0Color.copy(alpha = 0.5f) else NeoBorder,
            contentPadding = PaddingValues(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd,
                        contentDescription = null,
                        tint = if (clinicianReview.status != ReviewStatus.PENDING) Grade0Color else DrishtiBurgundy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Space8))
                    Text(
                        text = "Clinician Oversight",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeoInk
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (clinicianReview.status != ReviewStatus.PENDING) Grade0Color else Grade1Color
                ) {
                    Text(
                        text = clinicianReview.status.name,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = Space8, vertical = Space4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space8))

            if (clinicianReview.status != ReviewStatus.PENDING) {
                Text(
                    text = "Reviewed & Signed by: ${clinicianReview.clinicianName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NeoInk
                )
                if (clinicianReview.clinicalNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Space4))
                    Text(
                        text = "Notes: \"${clinicianReview.clinicalNotes}\"",
                        fontSize = 11.5.sp,
                        color = NeoInkSoft
                    )
                }
            } else {
                Text(
                    text = "Awaiting verification and sign-off by a qualified ophthalmologist or optometrist.",
                    fontSize = 11.5.sp,
                    color = NeoInkSoft
                )
            }

            Spacer(modifier = Modifier.height(Space16))

            NeoSecondaryButton(
                text = if (clinicianReview.status != ReviewStatus.PENDING) "Modify Clinical Review" else "Sign Off as Clinician",
                icon = if (clinicianReview.status != ReviewStatus.PENDING) Icons.Default.Edit else Icons.Default.Check,
                onClick = onOpenClinicianReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_clinician_review_button")
            )
        }

        Spacer(modifier = Modifier.height(Space16))

        // Primary Actions
        NeoPrimaryButton(
            text = "View Screening Report & QR",
            icon = Icons.Default.QrCode,
            onClick = onViewReport,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("view_report_qr_button")
        )

        Spacer(modifier = Modifier.height(Space8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space8)
        ) {
            NeoSecondaryButton(
                text = "Share Report",
                icon = Icons.Default.Share,
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
                            Referable DR: ${if (grade.isReferable) "YES (Action Required)" else "NO (Annual Follow-up)"}
                            Action Plan: ${grade.referralRecommendation}
                            Clinician Status: ${clinicianReview.status.name} (${clinicianReview.clinicianName.ifBlank { "Unsigned" }})
                            """.trimIndent()
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Drishti Screening Report"))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("share_report_button")
            )

            NeoSecondaryButton(
                text = "New Patient",
                icon = Icons.Default.Add,
                onClick = onNewScreening,
                modifier = Modifier
                    .weight(1f)
                    .testTag("new_screening_button")
            )
        }

        Spacer(modifier = Modifier.height(Space8))

        TextButton(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("view_all_history_button")
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = DrishtiBurgundy,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(Space8))
            Text(
                text = "View Stored Screenings in Room Database",
                color = DrishtiBurgundy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(Space24))
    }
}
