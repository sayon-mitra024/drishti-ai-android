package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScreeningEntity
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.theme.DrishtiPrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    screenings: List<ScreeningEntity>,
    onBack: () -> Unit,
    onDeleteScreening: (Long) -> Unit,
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
            .background(Color(0xFF090607))
            .padding(16.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to screening"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Screening Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${screenings.size} local screenings persisted in Room DB",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by patient name or ID...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilterGrade == null,
                    onClick = { selectedFilterGrade = null },
                    label = { Text("All (${screenings.size})") }
                )
            }
            items(DiabeticRetinopathyGrade.entries) { grade ->
                val count = screenings.count { it.predictedGrade == grade.grade }
                FilterChip(
                    selected = selectedFilterGrade == grade.grade,
                    onClick = {
                        selectedFilterGrade = if (selectedFilterGrade == grade.grade) null else grade.grade
                    },
                    label = { Text("G${grade.grade} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Screenings List
        if (filteredScreenings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching patient screenings found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScreenings, key = { it.id }) { item ->
                    ScreeningItemCard(
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
        ScreeningDetailDialog(
            screening = screening,
            onDismiss = { selectedScreeningForDetail = null }
        )
    }
}

@Composable
private fun ScreeningItemCard(
    screening: ScreeningEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val grade = DiabeticRetinopathyGrade.fromGrade(screening.predictedGrade)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val dateStr = remember(screening.timestamp) { dateFormat.format(Date(screening.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, grade.severityColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val bitmap = remember(screening.imagePath) {
                try {
                    val file = File(screening.imagePath)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                } catch (e: Exception) {
                    null
                }
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
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
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = DrishtiPrimary
                    )
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
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = grade.severityColor.copy(alpha = 0.18f),
                        contentColor = grade.severityColor
                    ) {
                        Text(
                            text = "G${grade.grade}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${screening.gradeTitle} • ${(screening.confidence * 100).toInt()}% conf",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = grade.severityColor
                )

                Text(
                    text = "Eye: ${screening.eyeSide} • $dateStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete record",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreeningDetailDialog(
    screening: ScreeningEntity,
    onDismiss: () -> Unit
) {
    val grade = DiabeticRetinopathyGrade.fromGrade(screening.predictedGrade)
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy 'at' hh:mm a", Locale.getDefault()) }
    val dateStr = remember(screening.timestamp) { dateFormat.format(Date(screening.timestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(screening.patientName, fontWeight = FontWeight.Bold)
                Text(
                    "ID: ${screening.patientId} • Eye: ${screening.eyeSide}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = grade.severityColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, grade.severityColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = grade.title,
                            fontWeight = FontWeight.Bold,
                            color = grade.severityColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Confidence: ${(screening.confidence * 100).toInt()}% • Referable: ${if (screening.isReferable) "YES" else "NO"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Date of Exam: $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Quality Status: ${screening.qualityStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Clinician Review: ${screening.reviewStatus} (${screening.clinicianName ?: "Pending"})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (!screening.clinicianNotes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Clinician Notes: \"${screening.clinicianNotes}\"",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
