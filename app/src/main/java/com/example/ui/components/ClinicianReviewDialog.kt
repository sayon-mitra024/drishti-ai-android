package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.theme.*

@Composable
fun ClinicianReviewDialog(
    initialGrade: DiabeticRetinopathyGrade,
    onDismiss: () -> Unit,
    onSubmitReview: (name: String, license: String, agreed: Boolean, overrideGrade: DiabeticRetinopathyGrade?, notes: String) -> Unit
) {
    var clinicianName by remember { mutableStateOf("Dr. S. Mitra, MD (Ophthal)") }
    var registrationNo by remember { mutableStateOf("REG-OPHTH-2026-894") }
    var agreedWithAi by remember { mutableStateOf(true) }
    var selectedOverrideGrade by remember { mutableStateOf(initialGrade) }
    var clinicalNotes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp),
            shapeRadius = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DrishtiRoseSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = "Clinician Review",
                            tint = DrishtiBurgundy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clinician Review & Sign-Off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoInk
                        )
                        Text(
                            text = "Professional clinical verification of AI screening",
                            fontSize = 11.5.sp,
                            color = NeoInkSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = NeoBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Inputs
                OutlinedTextField(
                    value = clinicianName,
                    onValueChange = { clinicianName = it },
                    label = { Text("Reviewing Clinician Name", fontSize = 12.sp) },
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
                        .testTag("clinician_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = registrationNo,
                    onValueChange = { registrationNo = it },
                    label = { Text("Medical Registration / License #", fontSize = 12.sp) },
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
                        .testTag("clinician_reg_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Clinical Assessment Decision",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = NeoInk
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    onClick = { agreedWithAi = true },
                    shape = RoundedCornerShape(10.dp),
                    color = if (agreedWithAi) DrishtiRoseSubtle else NeoSurfaceSunk,
                    border = BorderStroke(1.dp, if (agreedWithAi) DrishtiRoseBorder else NeoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = agreedWithAi,
                            onClick = { agreedWithAi = true },
                            colors = RadioButtonDefaults.colors(selectedColor = DrishtiBurgundy),
                            modifier = Modifier.testTag("agree_with_ai_radio")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Confirm AI Finding: ${initialGrade.shortName}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeoInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = { agreedWithAi = false },
                    shape = RoundedCornerShape(10.dp),
                    color = if (!agreedWithAi) DrishtiRoseSubtle else NeoSurfaceSunk,
                    border = BorderStroke(1.dp, if (!agreedWithAi) DrishtiRoseBorder else NeoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !agreedWithAi,
                            onClick = { agreedWithAi = false },
                            colors = RadioButtonDefaults.colors(selectedColor = DrishtiBurgundy),
                            modifier = Modifier.testTag("override_ai_radio")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Modify / Override with Clinician Grade",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeoInk
                        )
                    }
                }

                if (!agreedWithAi) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select Clinical Diagnosis Grade:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoInkSoft
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DiabeticRetinopathyGrade.entries.forEach { grade ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedOverrideGrade == grade,
                                onClick = { selectedOverrideGrade = grade },
                                colors = RadioButtonDefaults.colors(selectedColor = DrishtiBurgundy)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = grade.shortName,
                                fontSize = 12.sp,
                                color = grade.severityColor,
                                fontWeight = if (selectedOverrideGrade == grade) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clinicalNotes,
                    onValueChange = { clinicalNotes = it },
                    label = { Text("Clinical Notes & Recommendation", fontSize = 12.sp) },
                    placeholder = { Text("E.g., Microaneurysms noted in superior arcade. Follow-up dilated exam recommended.", fontSize = 11.sp) },
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeoSurfaceSunk,
                        unfocusedContainerColor = NeoSurfaceSunk,
                        focusedBorderColor = DrishtiBurgundy,
                        unfocusedBorderColor = NeoBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinician_notes_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    NeoSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    NeoPrimaryButton(
                        text = "Sign & Save",
                        icon = Icons.Default.Check,
                        onClick = {
                            onSubmitReview(
                                clinicianName,
                                registrationNo,
                                agreedWithAi,
                                if (agreedWithAi) null else selectedOverrideGrade,
                                clinicalNotes.ifBlank { "Screening reviewed and approved by $clinicianName" }
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_review_button")
                    )
                }
            }
        }
    }
}
