package com.example.report

import org.json.JSONArray
import org.json.JSONObject

/**
 * Compact, structured offline payload for QR code representation.
 * Excludes large binary assets (retinal image) and private clinician notes,
 * retaining critical screening metrics and cryptographic integrity signature.
 */
data class QrScreeningPayload(
    val version: Int = 1,
    val reportId: Long,
    val timestamp: Long,
    val patientId: String,
    val eye: String,
    val grade: Int,
    val confidence: Float,
    val probabilities: List<Float>,
    val quality: String,
    val reviewStatus: String,
    val clinicianName: String? = null,
    val signature: String = ""
) {
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("v", version)
        json.put("id", reportId)
        json.put("ts", timestamp)
        json.put("pid", patientId)
        json.put("eye", eye)
        json.put("g", grade)
        json.put("c", Math.round(confidence * 1000.0) / 1000.0)
        
        val pArray = JSONArray()
        probabilities.forEach { p ->
            pArray.put(Math.round(p * 1000.0) / 1000.0)
        }
        json.put("p", pArray)
        json.put("q", quality)
        json.put("rev", reviewStatus)
        if (!clinicianName.isNullOrBlank()) {
            json.put("cn", clinicianName)
        }
        json.put("sig", signature)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): QrScreeningPayload? {
            return try {
                val json = JSONObject(jsonStr)
                val v = json.optInt("v", -1)
                if (v <= 0) return null

                val id = json.getLong("id")
                val ts = json.getLong("ts")
                val pid = json.getString("pid")
                val eye = json.getString("eye")
                val g = json.getInt("g")
                val c = json.getDouble("c").toFloat()
                
                val pArray = json.optJSONArray("p")
                val probs = mutableListOf<Float>()
                if (pArray != null) {
                    for (i in 0 until pArray.length()) {
                        probs.add(pArray.getDouble(i).toFloat())
                    }
                }

                val q = json.optString("q", "PASS")
                val rev = json.optString("rev", "PENDING")
                val cn = if (json.has("cn")) json.getString("cn") else null
                val sig = json.optString("sig", "")

                QrScreeningPayload(
                    version = v,
                    reportId = id,
                    timestamp = ts,
                    patientId = pid,
                    eye = eye,
                    grade = g,
                    confidence = c,
                    probabilities = probs,
                    quality = q,
                    reviewStatus = rev,
                    clinicianName = cn,
                    signature = sig
                )
            } catch (_: Exception) {
                null
            }
        }

        fun fromReport(report: ScreeningReport): QrScreeningPayload {
            val probs = (0..4).map { report.probabilities[it] ?: 0f }
            return QrScreeningPayload(
                version = 1,
                reportId = report.screeningDbId,
                timestamp = report.timestamp,
                patientId = report.patientId,
                eye = report.eyeSide,
                grade = report.predictedGrade,
                confidence = report.confidence,
                probabilities = probs,
                quality = report.qualityStatus,
                reviewStatus = report.reviewStatus,
                clinicianName = report.clinicianName
            )
        }
    }
}
