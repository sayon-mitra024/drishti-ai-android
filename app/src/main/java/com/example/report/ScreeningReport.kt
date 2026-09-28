package com.example.report

import com.example.data.ScreeningEntity
import com.example.model.DiabeticRetinopathyGrade
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Deterministic report model representing the screening examination.
 * Encapsulates persisted Room data with clinical prototype disclaimer.
 */
data class ScreeningReport(
    val reportId: String,
    val screeningDbId: Long,
    val timestamp: Long,
    val formattedDateTime: String,
    val patientId: String,
    val patientName: String,
    val patientAge: Int,
    val eyeSide: String,
    val predictedGrade: Int,
    val gradeTitle: String,
    val confidence: Float,
    val isReferable: Boolean,
    val probabilities: Map<Int, Float>,
    val qualityStatus: String,
    val focusScore: Float,
    val illuminationScore: Float,
    val reviewStatus: String,
    val clinicianName: String?,
    val clinicianNotes: String?,
    val clinicianAssignedGrade: Int?,
    val reviewTimestamp: Long?,
    val modelName: String = "EfficientNet-B0 (drishti_model.onnx)",
    val disclaimer: String = "AI-assisted retinal screening result. Research prototype, not a medical diagnosis. Requires review and verification by an eye care specialist."
) {
    fun toFormattedReportText(): String {
        val grade = DiabeticRetinopathyGrade.fromGrade(predictedGrade)
        val probText = probabilities.toSortedMap().entries.joinToString("\n") { (g, p) ->
            val gTitle = DiabeticRetinopathyGrade.fromGrade(g).shortName
            "  - Grade $g ($gTitle): ${(p * 100).toInt()}%"
        }
        val clinicianInfo = if (reviewStatus != "PENDING" && !clinicianName.isNullOrBlank()) {
            "Clinician Review: $reviewStatus\nReviewed By: $clinicianName\nNotes: ${clinicianNotes ?: "None"}"
        } else {
            "Clinician Review: PENDING (Awaiting ophthalmic specialist sign-off)"
        }

        return """
        ====================================================
        DRISHTI AI RETINAL SCREENING REPORT
        ====================================================
        Report ID: $reportId
        Date / Time: $formattedDateTime
        Model: $modelName
        
        [PATIENT INFORMATION]
        Patient ID: $patientId
        Patient Name: $patientName
        Age: $patientAge
        Examined Eye: $eyeSide (${if (eyeSide == "OD") "Right Eye" else "Left Eye"})
        
        [IMAGE QUALITY ASSESSMENT]
        Status: $qualityStatus
        Focus Score: ${focusScore.toInt()} / 100
        Illumination Score: ${illuminationScore.toInt()} / 100
        
        [AI-ASSISTED SCREENING FINDING]
        Estimated DR Severity: ${grade.title} (Grade $predictedGrade)
        Model Confidence: ${(confidence * 100).toInt()}%
        Referable Diabetic Retinopathy: ${if (isReferable) "YES (Urgent Action)" else "NO (Routine Follow-up)"}
        
        [PROBABILITY DISTRIBUTION]
$probText
        
        [CLINICAL ACTION PLAN]
        ${grade.referralRecommendation}
        
        [CLINICIAN OVERSIGHT]
        $clinicianInfo
        
        ----------------------------------------------------
        LEGAL & CLINICAL DISCLAIMER:
        $disclaimer
        ====================================================
        """.trimIndent()
    }

    companion object {
        fun fromEntity(entity: ScreeningEntity): ScreeningReport {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date(entity.timestamp))
            val reportId = "DRISHTI-RPT-${entity.id.toString().padStart(5, '0')}"

            val probMap = mutableMapOf<Int, Float>()
            try {
                if (entity.probabilitiesJson.startsWith("{")) {
                    val json = JSONObject(entity.probabilitiesJson)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val g = k.toIntOrNull()
                        if (g != null) {
                            probMap[g] = json.getDouble(k).toFloat()
                        }
                    }
                } else {
                    entity.probabilitiesJson.split(",").forEach { pair ->
                        val parts = pair.split(":")
                        if (parts.size == 2) {
                            val g = parts[0].trim().toIntOrNull()
                            val p = parts[1].trim().toFloatOrNull()
                            if (g != null && p != null) {
                                probMap[g] = p
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            if (probMap.isEmpty()) {
                probMap[entity.predictedGrade] = entity.confidence
                for (g in 0..4) {
                    if (!probMap.containsKey(g)) probMap[g] = 0.0f
                }
            }

            return ScreeningReport(
                reportId = reportId,
                screeningDbId = entity.id,
                timestamp = entity.timestamp,
                formattedDateTime = dateStr,
                patientId = entity.patientId,
                patientName = entity.patientName,
                patientAge = entity.patientAge,
                eyeSide = entity.eyeSide,
                predictedGrade = entity.predictedGrade,
                gradeTitle = entity.gradeTitle,
                confidence = entity.confidence,
                isReferable = entity.isReferable,
                probabilities = probMap,
                qualityStatus = entity.qualityStatus,
                focusScore = entity.focusScore,
                illuminationScore = entity.illuminationScore,
                reviewStatus = entity.reviewStatus,
                clinicianName = entity.clinicianName,
                clinicianNotes = entity.clinicianNotes,
                clinicianAssignedGrade = entity.clinicianAssignedGrade,
                reviewTimestamp = entity.reviewTimestamp
            )
        }
    }
}
