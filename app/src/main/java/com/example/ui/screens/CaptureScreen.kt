package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.engine.ImageUtils
import com.example.model.DiabeticRetinopathyGrade
import com.example.model.EyeSide
import com.example.model.PatientInfo
import com.example.ui.components.*
import com.example.ui.theme.*
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

    // Patient Info form expansion/inputs
    var isEditingPatient by remember { mutableStateOf(false) }
    var patientNameInput by remember { mutableStateOf(patientInfo.fullName) }
    var patientAgeInput by remember { mutableStateOf(patientInfo.age.toString()) }
    var diabetesYearsInput by remember { mutableStateOf(patientInfo.diabetesDurationYears.toString()) }

    // Selected image preview modal before proceeding
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showPreviewModal by remember { mutableStateOf(false) }

    // Sample fundus modal state
    var showSampleModal by remember { mutableStateOf(false) }

    // Full-resolution camera launcher
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
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

    // Camera permission request
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

    // Photo picker launcher (zero-permission)
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
            .background(NeoBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space16, vertical = Space16)
    ) {
        // Clinical Introduction Card (Refined Neo-Skeuomorphic)
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Retinal Screening",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeoInk
                    )
                    Spacer(modifier = Modifier.height(Space4))
                    Text(
                        text = "Capture fundus photo for on-device 5-class DR assessment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoInkSoft,
                        lineHeight = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = DrishtiRoseSubtle,
                    border = BorderStroke(1.dp, DrishtiRoseBorder)
                ) {
                    Text(
                        text = "STEP 1 OF 3",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DrishtiBurgundy,
                        modifier = Modifier.padding(horizontal = Space8, vertical = Space4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space16))

            // Step Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space8)
            ) {
                StepIndicatorItem("1. Capture", isActive = true, modifier = Modifier.weight(1f))
                StepIndicatorItem("2. Quality", isActive = false, modifier = Modifier.weight(1f))
                StepIndicatorItem("3. Analysis", isActive = false, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Patient & Examination Card (Compact Neo-Skeuomorphic)
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DrishtiRoseSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DrishtiBurgundy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = patientInfo.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeoInk
                        )
                        Text(
                            text = "ID: ${patientInfo.patientId} • Age: ${patientInfo.age} yrs • DM: ${patientInfo.diabetesDurationYears} yrs",
                            fontSize = 11.sp,
                            color = NeoInkSoft
                        )
                    }
                }

                TextButton(
                    onClick = { isEditingPatient = !isEditingPatient },
                    modifier = Modifier.testTag("toggle_patient_edit_button")
                ) {
                    Text(
                        text = if (isEditingPatient) "Done" else "Edit",
                        color = DrishtiBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (isEditingPatient) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = NeoBorder)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = patientNameInput,
                    onValueChange = {
                        patientNameInput = it
                        onPatientInfoChange(it, null, null, null)
                    },
                    label = { Text("Full Patient Name", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeoSurfaceSunk,
                        unfocusedContainerColor = NeoSurfaceSunk,
                        focusedBorderColor = DrishtiBurgundy,
                        unfocusedBorderColor = NeoBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = patientAgeInput,
                        onValueChange = {
                            patientAgeInput = it
                            it.toIntOrNull()?.let { age -> onPatientInfoChange(null, age, null, null) }
                        },
                        label = { Text("Age", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NeoSurfaceSunk,
                            unfocusedContainerColor = NeoSurfaceSunk,
                            focusedBorderColor = DrishtiBurgundy,
                            unfocusedBorderColor = NeoBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = diabetesYearsInput,
                        onValueChange = {
                            diabetesYearsInput = it
                            it.toIntOrNull()?.let { dm -> onPatientInfoChange(null, null, null, dm) }
                        },
                        label = { Text("DM Years", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NeoSurfaceSunk,
                            unfocusedContainerColor = NeoSurfaceSunk,
                            focusedBorderColor = DrishtiBurgundy,
                            unfocusedBorderColor = NeoBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Eye Selection (OD vs OS) Segment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Examined Eye:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeoInkSoft
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EyeSelectionChip(
                        label = "OD (Right Eye)",
                        isSelected = patientInfo.eyeSide == EyeSide.OD,
                        onClick = { onPatientInfoChange(null, null, EyeSide.OD, null) }
                    )

                    EyeSelectionChip(
                        label = "OS (Left Eye)",
                        isSelected = patientInfo.eyeSide == EyeSide.OS,
                        onClick = { onPatientInfoChange(null, null, EyeSide.OS, null) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Visual Centerpiece: Retinal Capture Area
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Space16)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sunk Image Frame Well
                NeoSunkWell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shapeRadius = 16.dp,
                    backgroundColor = Color(0xFF1B0B0E),
                    borderColor = NeoBorderStrong
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Retinal alignment target ring
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(DrishtiBurgundy.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Space16))

                        Text(
                            text = "Align retina within optical frame",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Macula & optic disc centered • 45° field of view",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space16))

                // Primary Capture Action Button
                NeoPrimaryButton(
                    text = "Capture Retinal Photo",
                    icon = Icons.Default.PhotoCamera,
                    onClick = {
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_camera_button")
                )

                Spacer(modifier = Modifier.height(Space8))

                // Secondary Gallery Action
                NeoSecondaryButton(
                    text = "Choose from Gallery",
                    icon = Icons.Default.Image,
                    onClick = {
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_gallery_button")
                )

                Spacer(modifier = Modifier.height(Space8))

                // Clinical sample fundus test loader
                TextButton(
                    onClick = { showSampleModal = true },
                    modifier = Modifier.testTag("open_samples_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DrishtiBurgundy,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(Space8))
                    Text(
                        text = "Load Clinical Reference Fundus Image",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DrishtiBurgundy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Clinical Regulatory Disclaimer Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NeoSurfaceSunk,
            border = BorderStroke(1.dp, NeoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(Space16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeoInkFaint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Space8))
                Text(
                    text = "Drishti AI is an on-device clinical decision support prototype. Not for primary unassisted diagnosis.",
                    fontSize = 11.sp,
                    color = NeoInkSoft,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(Space24))
    }

    // Image Preview & Confirmation Dialog
    if (showPreviewModal && pendingBitmap != null) {
        Dialog(onDismissRequest = { showPreviewModal = false }) {
            NeoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shapeRadius = 20.dp
            ) {
                Text(
                    text = "Confirm Selected Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verify that the retinal fundus image is sharp, centered, and free of lens reflections.",
                    fontSize = 12.sp,
                    color = NeoInkSoft
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = pendingBitmap!!.asImageBitmap(),
                        contentDescription = "Selected retina",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoSecondaryButton(
                        text = "Retake",
                        onClick = { showPreviewModal = false },
                        modifier = Modifier.weight(1f)
                    )

                    NeoPrimaryButton(
                        text = "Validate Image",
                        onClick = {
                            showPreviewModal = false
                            onImageSelected(pendingBitmap!!, pendingUri)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("confirm_preview_button")
                    )
                }
            }
        }
    }

    // Reference Sample Fundus Dialog
    if (showSampleModal) {
        Dialog(onDismissRequest = { showSampleModal = false }) {
            NeoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shapeRadius = 20.dp
            ) {
                Text(
                    text = "Clinical Sample Fundus Images",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a validated reference retinal photo to test on-device inference.",
                    fontSize = 11.5.sp,
                    color = NeoInkSoft
                )

                Spacer(modifier = Modifier.height(14.dp))

                SampleFundusItem("Standard Reference Fundus (Grade 2 NPDR)", R.drawable.fundus_sample) {
                    val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.fundus_sample)
                    if (bmp != null) {
                        showSampleModal = false
                        onImageSelected(bmp, null)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = { showSampleModal = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = NeoInkSoft)
                }
            }
        }
    }
}

@Composable
private fun SampleFundusItem(title: String, resId: Int, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = NeoSurfaceSunk,
        border = BorderStroke(1.dp, NeoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = DrishtiBurgundy, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = NeoInk)
        }
    }
}

@Composable
private fun StepIndicatorItem(label: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) DrishtiRoseSubtle else NeoSurfaceSunk,
        border = BorderStroke(1.dp, if (isActive) DrishtiRoseBorder else NeoBorder)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) DrishtiBurgundy else NeoInkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}

@Composable
private fun EyeSelectionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) DrishtiBurgundy else NeoSurfaceSunk,
        border = BorderStroke(1.dp, if (isSelected) DrishtiBurgundy else NeoBorder),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
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

private fun createTempImageUri(context: Context): Uri {
    val photoFile = File.createTempFile("drishti_retina_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}
