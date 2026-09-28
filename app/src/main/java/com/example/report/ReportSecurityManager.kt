package com.example.report

import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

enum class VerificationStatus {
    VALID,                  // Cryptographically valid and matches canonical signature
    MODIFIED,               // Payload altered or signature mismatch
    UNSUPPORTED_VERSION,    // Version unrecognized or newer than client support
    MALFORMED               // Missing required fields or unparseable JSON
}

data class VerificationReport(
    val status: VerificationStatus,
    val payload: QrScreeningPayload?,
    val message: String,
    val isClinicianConfirmed: Boolean
)

/**
 * Manages cryptographic signing and integrity verification for offline screening reports.
 * Uses standard Android platform javax.crypto.Mac (HmacSHA256) without cloud dependencies.
 */
object ReportSecurityManager {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    // Internal offline secret salt for tamper-evidence
    private val INTEGRITY_SECRET = "DRISHTI_OFFLINE_INTEGRITY_SALT_V1".toByteArray(Charsets.UTF_8)
    private const val SUPPORTED_VERSION = 1

    /**
     * Builds canonical string representation of critical screening metrics.
     */
    fun buildCanonicalString(payload: QrScreeningPayload): String {
        val confFormatted = String.format(Locale.US, "%.3f", payload.confidence)
        val probsFormatted = if (payload.probabilities.isNotEmpty()) {
            payload.probabilities.joinToString(",") { String.format(Locale.US, "%.3f", it) }
        } else {
            "0.000"
        }
        val clinician = payload.clinicianName?.trim() ?: ""

        return "v${payload.version}|id:${payload.reportId}|ts:${payload.timestamp}|pid:${payload.patientId.trim()}|eye:${payload.eye.trim()}|g:${payload.grade}|c:$confFormatted|p:$probsFormatted|q:${payload.quality}|rev:${payload.reviewStatus}|cn:$clinician"
    }

    /**
     * Computes HMAC-SHA256 signature for the given payload.
     */
    fun computeSignature(payload: QrScreeningPayload): String {
        return try {
            val canonical = buildCanonicalString(payload)
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val keySpec = SecretKeySpec(INTEGRITY_SECRET, HMAC_ALGORITHM)
            mac.init(keySpec)
            val bytes = mac.doFinal(canonical.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Signs the payload with its cryptographic integrity signature.
     */
    fun signPayload(payload: QrScreeningPayload): QrScreeningPayload {
        val sig = computeSignature(payload)
        return payload.copy(signature = sig)
    }

    /**
     * Verifies the cryptographic integrity of a parsed QR payload.
     */
    fun verifyPayload(payload: QrScreeningPayload): VerificationReport {
        if (payload.version != SUPPORTED_VERSION) {
            return VerificationReport(
                status = VerificationStatus.UNSUPPORTED_VERSION,
                payload = payload,
                message = "Unsupported QR report version (v${payload.version}). Expected v$SUPPORTED_VERSION.",
                isClinicianConfirmed = false
            )
        }

        if (payload.reportId <= 0 || payload.grade !in 0..4 || payload.confidence !in 0f..1.001f) {
            return VerificationReport(
                status = VerificationStatus.MALFORMED,
                payload = payload,
                message = "Invalid or corrupted screening metrics in payload.",
                isClinicianConfirmed = false
            )
        }

        if (payload.signature.isBlank()) {
            return VerificationReport(
                status = VerificationStatus.MODIFIED,
                payload = payload,
                message = "Missing cryptographic signature.",
                isClinicianConfirmed = false
            )
        }

        val expectedSig = computeSignature(payload)
        val isValid = constantTimeEquals(expectedSig, payload.signature)

        return if (isValid) {
            val isConfirmed = (payload.reviewStatus == "CONFIRMED" || payload.reviewStatus == "MODIFIED") &&
                    !payload.clinicianName.isNullOrBlank()

            VerificationReport(
                status = VerificationStatus.VALID,
                payload = payload,
                message = "Screening record intact and verified via offline HMAC-SHA256.",
                isClinicianConfirmed = isConfirmed
            )
        } else {
            VerificationReport(
                status = VerificationStatus.MODIFIED,
                payload = payload,
                message = "Cryptographic signature mismatch. Record content has been modified or corrupted.",
                isClinicianConfirmed = false
            )
        }
    }

    /**
     * Decodes and verifies a raw QR code string.
     */
    fun verifyRawJson(rawJson: String): VerificationReport {
        val payload = QrScreeningPayload.fromJson(rawJson)
            ?: return VerificationReport(
                status = VerificationStatus.MALFORMED,
                payload = null,
                message = "QR code does not contain a valid Drishti screening structure.",
                isClinicianConfirmed = false
            )

        return verifyPayload(payload)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
