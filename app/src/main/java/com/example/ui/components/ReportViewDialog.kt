package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.model.DiabeticRetinopathyGrade
import com.example.report.QrCodeGenerator
import com.example.report.QrScreeningPayload
import com.example.report.ReportSecurityManager
import com.example.report.ScreeningReport
import com.example.ui.theme.*

@Composable
fun ReportViewDialog(
    report: ScreeningReport,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val grade = remember(report.predictedGrade) { DiabeticRetinopathyGrade.fromGrade(report.predictedGrade) }

    val gradeColor = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
    }

    // Generate signed payload and QR code offline
    val signedPayload = remember(report) {
        val unsigned = QrScreeningPayload.fromReport(report)
        ReportSecurityManager.signPayload(unsigned)
    }

    val qrBitmap: Bitmap? = remember(signedPayload) {
        QrCodeGenerator.generateQrBitmap(signedPayload.toJsonString(), 512)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shapeRadius = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Screening Report & QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoInk
                        )
                        Text(
                            text = report.reportId,
                            fontSize = 12.sp,
                            color = NeoInkSoft
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Grade0Bg,
                        border = BorderStroke(1.dp, Grade0Color.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Grade0Color,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HMAC Signed",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Grade0Color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = NeoBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Code Display
                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, NeoBorderStrong),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(200.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Screening Verification QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Offline Cryptographic Verification QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeoInkFaint
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DR Classification Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = gradeColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "AI-ASSISTED RETINAL FINDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = gradeColor,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${grade.title} (Grade ${report.predictedGrade})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoInk
                            )
                            Text(
                                text = "Confidence: ${(report.confidence * 100).toInt()}% • Referable DR: ${if (report.isReferable) "YES (Action Required)" else "NO (Routine Follow-up)"}",
                                fontSize = 11.5.sp,
                                color = NeoInkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Examination Details
                    NeoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = NeoSurfaceSunk) {
                        Text(
                            text = "Examination Metadata",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeoInk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Date/Time: ${report.formattedDateTime}", fontSize = 11.sp, color = NeoInkSoft)
                        Text("Patient ID: ${report.patientId} • Age: ${report.patientAge}", fontSize = 11.sp, color = NeoInkSoft)
                        Text("Examined Eye: ${report.eyeSide} (${if (report.eyeSide == "OD") "Right Eye" else "Left Eye"})", fontSize = 11.sp, color = NeoInkSoft)
                        Text("Quality: ${report.qualityStatus} (Focus: ${report.focusScore.toInt()}%, Illum: ${report.illuminationScore.toInt()}%)", fontSize = 11.sp, color = NeoInkSoft)
                        Text("Model: ${report.modelName}", fontSize = 11.sp, color = NeoInkSoft)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5-Class Probabilities
                    NeoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = NeoSurfaceSunk) {
                        Text(
                            text = "Probability Distribution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeoInk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        report.probabilities.toSortedMap().forEach { (g, prob) ->
                            val gObj = DiabeticRetinopathyGrade.fromGrade(g)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Grade $g: ${gObj.title}",
                                    fontSize = 11.sp,
                                    color = if (g == report.predictedGrade) gradeColor else NeoInkSoft,
                                    fontWeight = if (g == report.predictedGrade) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${(prob * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (g == report.predictedGrade) gradeColor else NeoInk
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinician Oversight Status
                    NeoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = NeoSurfaceSunk) {
                        Text(
                            text = "Clinician Oversight",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeoInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Review Status: ${report.reviewStatus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeoInk
                        )
                        if (!report.clinicianName.isNullOrBlank()) {
                            Text("Clinician: ${report.clinicianName}", fontSize = 11.sp, color = NeoInkSoft)
                        }
                        if (!report.clinicianNotes.isNullOrBlank()) {
                            Text("Notes: \"${report.clinicianNotes}\"", fontSize = 11.sp, color = NeoInkSoft)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mandatory Clinical / Research Disclaimer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeoSurfaceSunk,
                        border = BorderStroke(1.dp, NeoBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = report.disclaimer,
                            fontSize = 10.sp,
                            color = NeoInkFaint,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoSecondaryButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("close_report_dialog_button")
                    )

                    NeoPrimaryButton(
                        text = "Share Report & QR",
                        icon = Icons.Default.Share,
                        onClick = { shareReport(context, report, qrBitmap) },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("share_report_dialog_button")
                    )
                }
            }
        }
    }
}

private fun shareReport(context: Context, report: ScreeningReport, qrBitmap: Bitmap?) {
    val reportText = report.toFormattedReportText()
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Drishti AI Screening Report - ${report.reportId}")
        putExtra(Intent.EXTRA_TEXT, reportText)

        if (qrBitmap != null) {
            try {
                val qrFile = QrCodeGenerator.saveQrBitmapToFile(context, qrBitmap)
                val qrUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    qrFile
                )
                putExtra(Intent.EXTRA_STREAM, qrUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Drishti Screening Report"))
}
