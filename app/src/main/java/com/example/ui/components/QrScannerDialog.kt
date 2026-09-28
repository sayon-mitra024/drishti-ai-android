package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.engine.ImageUtils
import com.example.model.DiabeticRetinopathyGrade
import com.example.report.QrCodeGenerator
import com.example.report.ReportSecurityManager
import com.example.report.VerificationReport
import com.example.report.VerificationStatus
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var verificationResult by remember { mutableStateOf<VerificationReport?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDecoding by remember { mutableStateOf(false) }
    var manualPayloadInput by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Camera photo capture for QR
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            isDecoding = true
            errorMessage = null
            try {
                val bitmap = ImageUtils.loadBitmapFromUri(context, tempCameraUri!!)
                if (bitmap != null) {
                    val decoded = QrCodeGenerator.decodeQrFromBitmap(bitmap)
                    if (decoded != null) {
                        verificationResult = ReportSecurityManager.verifyRawJson(decoded)
                    } else {
                        errorMessage = "No QR code detected in the captured image. Please ensure the QR is well-lit and centered."
                    }
                } else {
                    errorMessage = "Failed to load captured image."
                }
            } catch (e: Exception) {
                errorMessage = "Error decoding image: ${e.message}"
            } finally {
                isDecoding = false
            }
        }
    }

    // Gallery photo picker for QR
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isDecoding = true
            errorMessage = null
            try {
                val bitmap = ImageUtils.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val decoded = QrCodeGenerator.decodeQrFromBitmap(bitmap)
                    if (decoded != null) {
                        verificationResult = ReportSecurityManager.verifyRawJson(decoded)
                    } else {
                        errorMessage = "No readable QR code found in selected image."
                    }
                } else {
                    errorMessage = "Could not decode image from gallery."
                }
            } catch (e: Exception) {
                errorMessage = "Error reading image: ${e.message}"
            } finally {
                isDecoding = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DrishtiRoseSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = DrishtiBurgundy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Verify Screening QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoInk
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = NeoInkFaint)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = NeoBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Action Buttons to scan or pick QR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeoPrimaryButton(
                            text = "Capture QR",
                            icon = Icons.Default.PhotoCamera,
                            onClick = {
                                try {
                                    val photoFile = File.createTempFile("qr_scan_", ".jpg", context.cacheDir)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    errorMessage = "Camera error: ${e.message}"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_qr_camera_button")
                        )

                        NeoSecondaryButton(
                            text = "Pick Image",
                            icon = Icons.Default.Image,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_qr_gallery_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { showManualInput = !showManualInput },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (showManualInput) "Hide Manual Input" else "Enter QR Payload Manually",
                            fontSize = 11.5.sp,
                            color = DrishtiBurgundy
                        )
                    }

                    if (showManualInput) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = manualPayloadInput,
                            onValueChange = { manualPayloadInput = it },
                            label = { Text("Paste JSON Payload", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NeoSurfaceSunk,
                                unfocusedContainerColor = NeoSurfaceSunk,
                                focusedBorderColor = DrishtiBurgundy,
                                unfocusedBorderColor = NeoBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoSecondaryButton(
                            text = "Verify Pasted Payload",
                            onClick = {
                                if (manualPayloadInput.isNotBlank()) {
                                    verificationResult = ReportSecurityManager.verifyRawJson(manualPayloadInput.trim())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isDecoding) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DrishtiBurgundy)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Decoding & verifying cryptographic payload...", fontSize = 12.sp, color = NeoInk)
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Grade3Bg,
                            border = BorderStroke(1.dp, Grade3Color.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Grade3Color)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage!!, fontSize = 11.5.sp, color = NeoInk)
                            }
                        }
                    }

                    // Verification Output Card
                    verificationResult?.let { result ->
                        Spacer(modifier = Modifier.height(14.dp))
                        VerificationResultCard(result)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    NeoSecondaryButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationResultCard(result: VerificationReport) {
    val statusColor = when (result.status) {
        VerificationStatus.VALID -> Grade0Color
        VerificationStatus.MODIFIED -> Grade3Color
        VerificationStatus.UNSUPPORTED_VERSION -> Grade2Color
        VerificationStatus.MALFORMED -> Grade3Color
    }

    val statusBg = when (result.status) {
        VerificationStatus.VALID -> Grade0Bg
        VerificationStatus.MODIFIED -> Grade3Bg
        VerificationStatus.UNSUPPORTED_VERSION -> Grade2Bg
        VerificationStatus.MALFORMED -> Grade3Bg
    }

    val statusTitle = when (result.status) {
        VerificationStatus.VALID -> "VALID / INTACT"
        VerificationStatus.MODIFIED -> "INVALID / MODIFIED"
        VerificationStatus.UNSUPPORTED_VERSION -> "UNKNOWN / UNSUPPORTED VERSION"
        VerificationStatus.MALFORMED -> "INVALID / MALFORMED"
    }

    val statusIcon = when (result.status) {
        VerificationStatus.VALID -> Icons.Default.CheckCircle
        VerificationStatus.MODIFIED -> Icons.Default.Dangerous
        VerificationStatus.UNSUPPORTED_VERSION -> Icons.Default.Warning
        VerificationStatus.MALFORMED -> Icons.Default.Error
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = statusBg,
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Status Banner
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = statusTitle,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = statusColor
                    )
                    Text(
                        text = result.message,
                        fontSize = 11.5.sp,
                        color = NeoInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinician Confirmation Status Note
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (result.isClinicianConfirmed) Grade0Bg else Grade1Bg,
                border = BorderStroke(1.dp, if (result.isClinicianConfirmed) Grade0Color.copy(alpha = 0.4f) else Grade1Color.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.isClinicianConfirmed) Icons.Default.Verified else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (result.isClinicianConfirmed) Grade0Color else Grade1Color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (result.isClinicianConfirmed) {
                            "Clinician Confirmed: Signed by ${result.payload?.clinicianName}"
                        } else {
                            "AI Screening Finding Only — Clinician Review Pending (Not medically verified)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (result.isClinicianConfirmed) Grade0Color else Grade1Color
                    )
                }
            }

            // Decoded Payload Metrics
            result.payload?.let { payload ->
                Spacer(modifier = Modifier.height(10.dp))
                val grade = DiabeticRetinopathyGrade.fromGrade(payload.grade)
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val dateStr = dateFormat.format(Date(payload.timestamp))

                Text("Decoded Screening Record:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeoInk)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Record ID: #${payload.reportId}", fontSize = 11.sp, color = NeoInkSoft)
                Text("• Date / Time: $dateStr", fontSize = 11.sp, color = NeoInkSoft)
                Text("• Patient ID: ${payload.patientId} • Eye: ${payload.eye}", fontSize = 11.sp, color = NeoInkSoft)
                Text("• AI DR Grade: ${grade.title} (Grade ${payload.grade})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = grade.severityColor)
                Text("• Model Confidence: ${(payload.confidence * 100).toInt()}%", fontSize = 11.sp, color = NeoInkSoft)
                Text("• Quality: ${payload.quality}", fontSize = 11.sp, color = NeoInkSoft)
                Text("• Review: ${payload.reviewStatus}", fontSize = 11.sp, color = NeoInkSoft)

                if (payload.probabilities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("5-Class Probability Breakdown:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = NeoInk)
                    payload.probabilities.forEachIndexed { idx, p ->
                        Text("  - G$idx (${DiabeticRetinopathyGrade.fromGrade(idx).shortName}): ${(p * 100).toInt()}%", fontSize = 10.sp, color = NeoInkSoft)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "*AI-assisted screening support tool. Operates 100% offline.*",
                fontSize = 10.sp,
                color = NeoInkFaint
            )
        }
    }
}
