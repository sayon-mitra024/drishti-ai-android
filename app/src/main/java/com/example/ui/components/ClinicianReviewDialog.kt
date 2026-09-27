package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.theme.DrishtiPrimary

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DrishtiPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = "Clinician Review",
                            tint = DrishtiPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clinician Review & Sign-Off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Formal clinical verification of AI screening",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clinician Identity Inputs
                OutlinedTextField(
                    value = clinicianName,
                    onValueChange = { clinicianName = it },
                    label = { Text("Reviewing Clinician Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinician_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = registrationNo,
                    onValueChange = { registrationNo = it },
                    label = { Text("Medical Council Registration / License #") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinician_reg_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Clinical Assessment Decision",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Agreement Radio selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = agreedWithAi,
                        onClick = { agreedWithAi = true },
                        modifier = Modifier.testTag("agree_with_ai_radio")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Agree with AI: ${initialGrade.shortName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !agreedWithAi,
                        onClick = { agreedWithAi = false },
                        modifier = Modifier.testTag("override_ai_radio")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Override with clinician-graded diagnosis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // If overriding, show grade selector
                if (!agreedWithAi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select Overridden ICDR Grade:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    DiabeticRetinopathyGrade.entries.forEach { grade ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedOverrideGrade == grade,
                                onClick = { selectedOverrideGrade = grade }
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

                // Notes input
                OutlinedTextField(
                    value = clinicalNotes,
                    onValueChange = { clinicalNotes = it },
                    label = { Text("Clinical Notes & Referral Advice") },
                    placeholder = { Text("E.g. Verified blot hemorrhages. Schedule dilated fundus examination and OCT.") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinician_notes_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmitReview(
                                clinicianName,
                                registrationNo,
                                agreedWithAi,
                                if (agreedWithAi) null else selectedOverrideGrade,
                                clinicalNotes.ifBlank { "Screening reviewed and approved by $clinicianName" }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DrishtiPrimary),
                        modifier = Modifier.testTag("submit_review_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign & Record")
                    }
                }
            }
        }
    }
}
