package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ScreeningEntity
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoPrimaryButton
import com.example.ui.components.NeoSecondaryButton
import com.example.ui.components.NeoSunkWell
import com.example.ui.components.SeverityPill
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    screenings: List<ScreeningEntity>,
    onBack: () -> Unit,
    onDeleteScreening: (Long) -> Unit,
    onOpenScreening: (ScreeningEntity) -> Unit = {},
    onScanQr: () -> Unit = {},
    onViewReport: (ScreeningEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterGrade by remember { mutableStateOf<Int?>(null) }
    var selectedScreeningForDetail by remember { mutableStateOf<ScreeningEntity?>(null) }

    val filteredScreenings = remember(screenings, searchQuery, selectedFilterGrade) {
        screenings.filter { s ->
            val matchesQuery = s.patientName.contains(searchQuery, ignoreCase = true) ||
                    s.patientId.contains(searchQuery, ignoreCase = true)
            val matchesGrade = selectedFilterGrade == null || s.predictedGrade == selectedFilterGrade
            matchesQuery && matchesGrade
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .padding(horizontal = Space16, vertical = Space16)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeoInk
                    )
                }
                Spacer(modifier = Modifier.width(Space4))
                Column {
                    Text(
                        text = "Screening History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeoInk
                    )
                    Text(
                        text = "${screenings.size} records in local Room DB",
                        fontSize = 12.sp,
                        color = NeoInkSoft
                    )
                }
            }

            // QR Verify Scanner trigger
            IconButton(
                onClick = onScanQr,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DrishtiRoseSubtle)
                    .testTag("history_scan_qr_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Verify QR Code",
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Space16))

        // Search Input well
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by patient name or ID...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeoInkFaint, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = NeoInkFaint, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NeoSurfaceSunk,
                unfocusedContainerColor = NeoSurfaceSunk,
                focusedBorderColor = DrishtiBurgundy,
                unfocusedBorderColor = NeoBorder
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(Space8))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space8)
        ) {
            item {
                HistoryFilterPill(
                    label = "All (${screenings.size})",
                    isSelected = selectedFilterGrade == null,
                    onClick = { selectedFilterGrade = null }
                )
            }

            DiabeticRetinopathyGrade.entries.forEach { grade ->
                val count = screenings.count { it.predictedGrade == grade.grade }
                item {
                    HistoryFilterPill(
                        label = "G${grade.grade} ($count)",
                        isSelected = selectedFilterGrade == grade.grade,
                        onClick = { selectedFilterGrade = grade.grade }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space12))

        // Records List
        if (filteredScreenings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NeoSurfaceSunk),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = NeoInkFaint, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(Space8))
                    Text(
                        text = if (searchQuery.isEmpty() && selectedFilterGrade == null) "No screenings saved yet" else "No matching records found",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoInkSoft
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space8)
            ) {
                items(filteredScreenings, key = { it.id }) { item ->
                    HistoryCardItem(
                        screening = item,
                        onClick = { selectedScreeningForDetail = item },
                        onDelete = { onDeleteScreening(item.id) }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedScreeningForDetail?.let { screening ->
        HistoryDetailDialog(
            screening = screening,
            onDismiss = { selectedScreeningForDetail = null },
            onOpenScreening = {
                selectedScreeningForDetail = null
                onOpenScreening(it)
            },
            onViewReport = {
                selectedScreeningForDetail = null
                onViewReport(it)
            }
        )
    }
}

@Composable
private fun HistoryFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) DrishtiBurgundy else NeoSurfaceSunk,
        border = BorderStroke(1.dp, if (isSelected) DrishtiBurgundy else NeoBorder)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else NeoInk,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HistoryCardItem(
    screening: ScreeningEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val grade = remember(screening.predictedGrade) { DiabeticRetinopathyGrade.fromGrade(screening.predictedGrade) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = remember(screening.timestamp) { dateFormat.format(Date(screening.timestamp)) }

    val gradeColor = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
    }

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val bitmap = remember(screening.imagePath) {
                try {
                    val file = File(screening.imagePath)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                } catch (_: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = DrishtiBurgundy, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = screening.patientName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = NeoInk
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gradeColor
                    ) {
                        Text(
                            text = "G${grade.grade}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${screening.gradeTitle} • ${(screening.confidence * 100).toInt()}% conf",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gradeColor
                )

                Text(
                    text = "ID: ${screening.patientId} • Eye: ${screening.eyeSide} • $dateStr",
                    fontSize = 10.5.sp,
                    color = NeoInkFaint
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = NeoInkFaint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryDetailDialog(
    screening: ScreeningEntity,
    onDismiss: () -> Unit,
    onOpenScreening: (ScreeningEntity) -> Unit,
    onViewReport: (ScreeningEntity) -> Unit
) {
    val context = LocalContext.current
    val grade = remember(screening.predictedGrade) { DiabeticRetinopathyGrade.fromGrade(screening.predictedGrade) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = remember(screening.timestamp) { dateFormat.format(Date(screening.timestamp)) }

    val gradeColor = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
    }

    val savedBitmap = remember(screening.imagePath) {
        try {
            val file = File(screening.imagePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (_: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shapeRadius = 20.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(screening.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NeoInk)
                    Text("ID: ${screening.patientId} • Eye: ${screening.eyeSide}", fontSize = 11.5.sp, color = NeoInkSoft)
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = gradeColor
                ) {
                    Text(
                        text = "Grade ${grade.grade}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (savedBitmap != null) {
                NeoSunkWell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shapeRadius = 12.dp,
                    backgroundColor = Color.Black
                ) {
                    Image(
                        bitmap = savedBitmap.asImageBitmap(),
                        contentDescription = "Fundus photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = gradeColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = grade.title,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "Confidence: ${(screening.confidence * 100).toInt()}% • Referable: ${if (screening.isReferable) "YES" else "NO"}",
                        fontSize = 11.5.sp,
                        color = NeoInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Date / Time: $dateStr", fontSize = 11.sp, color = NeoInkSoft)
            Text("Quality: ${screening.qualityStatus}", fontSize = 11.sp, color = NeoInkSoft)
            Text("Clinician Review: ${screening.reviewStatus} (${screening.clinicianName ?: "Pending"})", fontSize = 11.sp, color = NeoInkSoft)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeoSecondaryButton(
                    text = "Report & QR",
                    icon = Icons.Default.QrCode,
                    onClick = { onViewReport(screening) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_item_view_report_button")
                )

                NeoPrimaryButton(
                    text = "Open Results",
                    onClick = { onOpenScreening(screening) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
                                DRISHTI AI RETINAL SCREENING REPORT
                                Record ID: ${screening.id}
                                Patient: ${screening.patientName} (ID: ${screening.patientId})
                                Eye: ${screening.eyeSide} • Date: $dateStr
                                Predicted DR: ${grade.title} (Grade ${grade.grade})
                                Confidence: ${(screening.confidence * 100).toInt()}%
                                Referable Action: ${if (screening.isReferable) "YES (Action Required)" else "NO (Routine Follow-up)"}
                                Clinician: ${screening.reviewStatus} (${screening.clinicianName ?: "Unsigned"})
                                """.trimIndent()
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Stored Report"))
                    }
                ) {
                    Text("Share Plaintext", fontSize = 11.5.sp, color = DrishtiBurgundy)
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", fontSize = 11.5.sp, color = NeoInkSoft)
                }
            }
        }
    }
}
