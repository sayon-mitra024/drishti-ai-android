package com.example.model

import android.graphics.Bitmap

data class LesionCue(
    val title: String,
    val type: String, // "Microaneurysm", "Hemorrhage", "Hard Exudate", "Cotton Wool Spot", "Neovascularization"
    val region: String, // "Macular", "Superior-Temporal", "Inferior-Nasal", etc.
    val severityLevel: String,
    val xPercent: Float, // Relative X coordinate (0.0 to 1.0) for annotation marker
    val yPercent: Float  // Relative Y coordinate (0.0 to 1.0)
)

data class AnalysisResult(
    val predictedGrade: DiabeticRetinopathyGrade,
    val confidence: Float, // e.g. 0.94f (94%)
    val probabilityDistribution: Map<DiabeticRetinopathyGrade, Float>,
    val lesionCues: List<LesionCue>,
    val gradCamBitmap: Bitmap?,
    val executionTimeMs: Long,
    val inferenceEngineName: String = "Drishti-EfficientNet On-Device Engine v2.4"
)

enum class ReviewStatus {
    PENDING,
    CONFIRMED,
    MODIFIED,
    REJECTED
}

data class ClinicianReview(
    val status: ReviewStatus = ReviewStatus.PENDING,
    val clinicianName: String = "",
    val clinicianId: String = "",
    val agreedWithAi: Boolean = true,
    val assignedGrade: DiabeticRetinopathyGrade? = null,
    val clinicalNotes: String = "",
    val reviewTimestamp: Long = 0L
)

enum class EyeSide {
    OD, // Right Eye (Oculus Dexter)
    OS  // Left Eye (Oculus Sinister)
}

data class PatientInfo(
    val patientId: String = "P-${(1000..9999).random()}",
    val fullName: String = "Anonymous Patient",
    val age: Int = 54,
    val gender: String = "Unspecified",
    val eyeSide: EyeSide = EyeSide.OD,
    val diabetesDurationYears: Int = 8
)
