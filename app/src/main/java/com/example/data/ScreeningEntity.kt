package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenings")
data class ScreeningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: String,
    val patientName: String,
    val patientAge: Int,
    val eyeSide: String, // "OD" or "OS"
    val timestamp: Long = System.currentTimeMillis(),

    // Image file path cached locally
    val imagePath: String,
    val gradCamPath: String? = null,

    // Quality check
    val qualityStatus: String, // PASS, WARNING, FAIL
    val focusScore: Float,
    val illuminationScore: Float,

    // AI Prediction
    val predictedGrade: Int, // 0 to 4
    val gradeTitle: String,
    val confidence: Float,
    val isReferable: Boolean,
    val probabilitiesJson: String, // e.g. "0.02,0.85,0.10,0.02,0.01"

    // Clinician review
    val reviewStatus: String = "PENDING", // PENDING, CONFIRMED, MODIFIED, REJECTED
    val clinicianName: String? = null,
    val clinicianNotes: String? = null,
    val clinicianAssignedGrade: Int? = null,
    val reviewTimestamp: Long? = null
)
