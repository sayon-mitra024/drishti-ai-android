package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.engine.ImageUtils
import com.example.model.EyeSide
import com.example.model.PatientInfo
import com.example.ui.theme.DrishtiPrimary
import java.io.File

@Composable
fun CaptureScreen(
    patientInfo: PatientInfo,
    onPatientInfoChange: (fullName: String?, age: Int?, eyeSide: EyeSide?, diabetesYears: Int?) -> Unit,
    onImageSelected: (Bitmap, Uri?) -> Unit,
    onViewHistory: () -> Unit,
    onViewGuidelines: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Patient Info editing local state
    var isEditingPatient by remember { mutableStateOf(false) }
    var patientNameInput by remember { mutableStateOf(patientInfo.fullName) }
    var patientAgeInput by remember { mutableStateOf(patientInfo.age.toString()) }
    var diabetesYearsInput by remember { mutableStateOf(patientInfo.diabetesDurationYears.toString()) }

    // Model documentation collapsible card
    var isModelDocExpanded by remember { mutableStateOf(false) }

    // Image Preview & Confirmation Modal State
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showPreviewModal by remember { mutableStateOf(false) }

    // Camera Uri state for full-res capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Real Camera Capture Launcher (writes full resolution to tempCameraUri)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val fullBitmap = ImageUtils.loadBitmapFromUri(context, tempCameraUri!!, 1536)
            if (fullBitmap != null) {
                pendingBitmap = fullBitmap
                pendingUri = tempCameraUri
                showPreviewModal = true
            }
        }
    }

    // Permission Launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            showPermissionRationale = true
        }
    }

    // Gallery Picker Launcher (Android Photo Picker zero-permission)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = ImageUtils.loadBitmapFromUri(context, uri, 1536)
            if (bitmap != null) {
                pendingBitmap = bitmap
                pendingUri = uri
                showPreviewModal = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCF9F8)) // Official drishti.sayonedu.in white bg
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Clinical Brand Card (Faithful translation of drishti.sayonedu.in)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E2E1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Official Drishti Logo banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.drishti_logo),
                        contentDescription = "Drishti AI Logo",
                        modifier = Modifier.height(34.dp),
                        contentScale = ContentScale.Fit
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFDF2F3),
                        border = BorderStroke(1.dp, DrishtiPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "RESEARCH PROTOTYPE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DrishtiPrimary,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "See what the eye reveals.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1C1B1B),
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Intelligent retinal screening, designed for earlier clinical attention. Upload or capture a fundus image to run the AI-assisted screening prototype: 5-class diabetic retinopathy scoring, confidence, a Grad-CAM explainability overlay, and referral guidance for clinician review.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF574142),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5-Stage Pipeline Pills
                Text(
                    text = "SCREENING PIPELINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B7173),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PipelineStepPill("01", "Capture", modifier = Modifier.weight(1f))
                    PipelineStepPill("02", "Quality", modifier = Modifier.weight(1f))
                    PipelineStepPill("03", "AI Model", modifier = Modifier.weight(1f))
                    PipelineStepPill("04", "Results", modifier = Modifier.weight(1f))
                    PipelineStepPill("05", "Review", modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patient & Examination Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E2E1))
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDF2F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DrishtiPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Patient & Exam Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                    }

                    TextButton(
                        onClick = { isEditingPatient = !isEditingPatient },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditingPatient) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = null,
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditingPatient) "Done" else "Edit Details",
                            fontSize = 12.sp,
                            color = DrishtiPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isEditingPatient) {
                    OutlinedTextField(
                        value = patientNameInput,
                        onValueChange = {
                            patientNameInput = it
                            onPatientInfoChange(it, null, null, null)
                        },
                        label = { Text("Patient Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DrishtiPrimary,
                            unfocusedBorderColor = Color(0xFFE5E2E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("patient_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = patientAgeInput,
                            onValueChange = {
                                patientAgeInput = it
                                it.toIntOrNull()?.let { age -> onPatientInfoChange(null, age, null, null) }
                            },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DrishtiPrimary,
                                unfocusedBorderColor = Color(0xFFE5E2E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("patient_age_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = diabetesYearsInput,
                            onValueChange = {
                                diabetesYearsInput = it
                                it.toIntOrNull()?.let { yrs -> onPatientInfoChange(null, null, null, yrs) }
                            },
                            label = { Text("DM Years") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DrishtiPrimary,
                                unfocusedBorderColor = Color(0xFFE5E2E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("patient_dm_years_input")
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = patientInfo.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1C1B1B)
                            )
                            Text(
                                text = "MRN: ${patientInfo.patientId} • Age: ${patientInfo.age} yrs",
                                fontSize = 12.sp,
                                color = Color(0xFF574142)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFDF2F3),
                            border = BorderStroke(1.dp, Color(0xFFDEBFBF))
                        ) {
                            Text(
                                text = "DM: ${patientInfo.diabetesDurationYears} yrs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = DrishtiPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Examination Eye Selector (OD / OS)
                Text(
                    text = "EXAMINATION EYE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B7173),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    val isOdSelected = patientInfo.eyeSide == EyeSide.OD
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOdSelected) DrishtiPrimary else Color(0xFFF6F3F2),
                        border = BorderStroke(
                            1.dp,
                            if (isOdSelected) DrishtiPrimary else Color(0xFFE5E2E1)
                        ),
                        contentColor = if (isOdSelected) Color.White else Color(0xFF1C1B1B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPatientInfoChange(null, null, EyeSide.OD, null) }
                            .testTag("eye_od_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isOdSelected) Color.White else Color(0xFF574142)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OD (Right Eye)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isOdSelected) Color.White else Color(0xFF1C1B1B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val isOsSelected = patientInfo.eyeSide == EyeSide.OS
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOsSelected) DrishtiPrimary else Color(0xFFF6F3F2),
                        border = BorderStroke(
                            1.dp,
                            if (isOsSelected) DrishtiPrimary else Color(0xFFE5E2E1)
                        ),
                        contentColor = if (isOsSelected) Color.White else Color(0xFF1C1B1B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPatientInfoChange(null, null, EyeSide.OS, null) }
                            .testTag("eye_os_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isOsSelected) Color.White else Color(0xFF574142)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OS (Left Eye)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isOsSelected) Color.White else Color(0xFF1C1B1B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Image Input Options: Primary = Take Photo, Secondary = Upload Gallery
        Text(
            text = "Acquire Retinal Fundus Image",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1B)
        )
        Text(
            text = "Take Photo is primary action • High-resolution camera with framing guidance",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF574142)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // PRIMARY ACTION: Take Photo (Solid Crimson + Glow)
            Card(
                modifier = Modifier
                    .weight(1.15f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        val cameraGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (cameraGranted) {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    .testTag("take_photo_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DrishtiPrimary
                ),
                border = BorderStroke(1.5.dp, Color(0xFF8B1E2D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B1E2D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Photo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Primary Camera",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFDADA)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // SECONDARY ACTION: Upload Gallery (Crisp White Card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                    .testTag("upload_gallery_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFE5E2E1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF6F3F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Upload Gallery",
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Upload Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
                    Text(
                        text = "Photo Picker",
                        fontSize = 11.sp,
                        color = Color(0xFF574142)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clinical Demo Cases (For instant testing in emulator)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E2E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clinical Demo Fundus Library",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFDF2F3),
                        border = BorderStroke(1.dp, Color(0xFFDEBFBF))
                    ) {
                        Text(
                            text = "EMULATOR READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = DrishtiPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap to load authentic clinical fundus images to inspect quality checks, 5-class predictions & Grad-CAM:",
                    fontSize = 12.sp,
                    color = Color(0xFF574142),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoCaseButton(
                        label = "Normal (G0)",
                        color = Color(0xFF1B7F52),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fundus_sample)
                            if (bitmap != null) {
                                pendingBitmap = bitmap
                                pendingUri = null
                                showPreviewModal = true
                            }
                        }
                    )
                    DemoCaseButton(
                        label = "Moderate (G2)",
                        color = Color(0xFFA36B00),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fundus_sample)
                            if (bitmap != null) {
                                pendingBitmap = bitmap
                                pendingUri = null
                                showPreviewModal = true
                            }
                        }
                    )
                    DemoCaseButton(
                        label = "PDR (G4)",
                        color = Color(0xFF6B0119),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fundus_sample)
                            if (bitmap != null) {
                                pendingBitmap = bitmap
                                pendingUri = null
                                showPreviewModal = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deployed Model Documentation & Validation Metrics (Directly from drishti.sayonedu.in)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E2E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isModelDocExpanded = !isModelDocExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Model Documentation & Validation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Text(
                            text = "EfficientNet-B0 • Grad-CAM • 224×224 Input",
                            fontSize = 11.sp,
                            color = Color(0xFF574142)
                        )
                    }
                    Icon(
                        imageVector = if (isModelDocExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Documentation",
                        tint = DrishtiPrimary
                    )
                }

                AnimatedVisibility(visible = isModelDocExpanded) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Divider(color = Color(0xFFE5E2E1))
                        Spacer(modifier = Modifier.height(10.dp))

                        ModelDocRow("Architecture", "EfficientNet-B0")
                        ModelDocRow("Task", "Diabetic Retinopathy Classification, 5-Class")
                        ModelDocRow("Classes", "No DR · Mild · Moderate · Severe · Proliferative DR")
                        ModelDocRow("Input size", "224 × 224 Standardized Tensor")
                        ModelDocRow("Explainability", "Grad-CAM (Computed on-device)")
                        ModelDocRow("Serving", "Autonomous Mobile Inference (Offline-capable)")

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "KNOWN LIMITATIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B7173),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• AI-assisted screening support tool, not a diagnostic medical device.\n• Final clinical evaluation requires review by an ophthalmologist.\n• Low-quality, obscured, or out-of-focus captures require re-examination.",
                            fontSize = 11.sp,
                            color = Color(0xFF574142),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Buttons (History & Guidelines)
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onViewHistory,
                border = BorderStroke(1.dp, Color(0xFFDEBFBF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1C1B1B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("view_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = DrishtiPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Screening Records", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedButton(
                onClick = onViewGuidelines,
                border = BorderStroke(1.dp, Color(0xFFDEBFBF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1C1B1B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("view_guidelines_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = DrishtiPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ICDR Guidelines", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Clinical Footer Notice & Sayon Mitra credits
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "© 2026 Drishti AI • AI-assisted retinal screening system",
                fontSize = 11.sp,
                color = Color(0xFF8B7173),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Built by Sayon Mitra",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF574142),
                textAlign = TextAlign.Center
            )
        }
    }

    // =========================================================================
    // IMAGE PREVIEW & RETAKE / CONFIRM MODAL (Step 6 in workflow)
    // =========================================================================
    if (showPreviewModal && pendingBitmap != null) {
        Dialog(
            onDismissRequest = { showPreviewModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFDEBFBF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Retinal Image Preview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1B)
                            )
                            Text(
                                text = "Verify optical focus & coverage before quality checks",
                                fontSize = 12.sp,
                                color = Color(0xFF574142)
                            )
                        }
                        IconButton(onClick = { showPreviewModal = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Preview",
                                tint = Color(0xFF574142)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Retinal Framing Viewport with Optical Reticle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F0A0C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = pendingBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Retinal Fundus",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Circular Optical Reticle with Crosshairs
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val reticleRadius = minOf(size.width, size.height) * 0.42f
                            val tick = 14.dp.toPx()

                            // Outer Reticle Circle
                            drawCircle(
                                color = Color(0x99FF8A95),
                                radius = reticleRadius,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                            )
                            // Inner Target Circle
                            drawCircle(
                                color = Color(0x66FFDADA),
                                radius = reticleRadius * 0.35f,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )

                            // Crosshair tick marks
                            drawLine(
                                color = Color(0xCCFF8A95),
                                start = Offset(center.x, center.y - reticleRadius - tick),
                                end = Offset(center.x, center.y - reticleRadius + 4.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xCCFF8A95),
                                start = Offset(center.x, center.y + reticleRadius - 4.dp.toPx()),
                                end = Offset(center.x, center.y + reticleRadius + tick),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xCCFF8A95),
                                start = Offset(center.x - reticleRadius - tick, center.y),
                                end = Offset(center.x - reticleRadius + 4.dp.toPx(), center.y),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xCCFF8A95),
                                start = Offset(center.x + reticleRadius - 4.dp.toPx(), center.y),
                                end = Offset(center.x + reticleRadius + tick, center.y),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Image Metadata Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF6F3F2),
                            border = BorderStroke(1.dp, Color(0xFFE5E2E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RESOLUTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B7173))
                                Text("${pendingBitmap!!.width} × ${pendingBitmap!!.height}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1B))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFDF2F3),
                            border = BorderStroke(1.dp, Color(0xFFDEBFBF)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EXAMINATION EYE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B7173))
                                Text(patientInfo.eyeSide.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DrishtiPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF6F3F2),
                            border = BorderStroke(1.dp, Color(0xFFE5E2E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COLOR PROFILE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B7173))
                                Text("sRGB 24-bit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions: Retake vs. Confirm & Run Quality Check
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                showPreviewModal = false
                                pendingBitmap = null
                                pendingUri = null
                                // Re-trigger camera
                                val uri = createTempImageUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            border = BorderStroke(1.dp, Color(0xFFDEBFBF)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1C1B1B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("retake_photo_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = DrishtiPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake Photo", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                val b = pendingBitmap!!
                                val u = pendingUri
                                showPreviewModal = false
                                pendingBitmap = null
                                pendingUri = null
                                onImageSelected(b, u)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DrishtiPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("confirm_photo_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Assess", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Permission Alert Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text(
                    "Camera Access Required",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1B)
                )
            },
            text = {
                Text(
                    "Drishti AI requires camera access to photograph the retinal fundus through the ophthalmoscope lens adapter. You can also select existing fundus images from your gallery.",
                    color = Color(0xFF574142),
                    lineHeight = 18.sp
                )
            },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DrishtiPrimary)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Use Gallery", color = DrishtiPrimary)
                }
            }
        )
    }
}

@Composable
private fun PipelineStepPill(
    number: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF6F3F2),
        border = BorderStroke(1.dp, Color(0xFFE5E2E1)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = DrishtiPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1B)
            )
        }
    }
}

@Composable
private fun DemoCaseButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ModelDocRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF8B7173),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1B1B),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("drishti_retinal_", ".jpg", context.cacheDir).apply {
        createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
