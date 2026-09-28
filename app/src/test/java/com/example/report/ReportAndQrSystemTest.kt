package com.example.report

import android.graphics.Bitmap
import com.example.data.ScreeningEntity
import com.example.engine.CamGenerator
import com.example.model.DiabeticRetinopathyGrade
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportAndQrSystemTest {

    private fun createSamplePayload(
        id: Long = 101L,
        grade: Int = 2,
        conf: Float = 0.892f,
        reviewStatus: String = "PENDING",
        clinicianName: String? = null,
        version: Int = 1
    ): QrScreeningPayload {
        return QrScreeningPayload(
            version = version,
            reportId = id,
            timestamp = 1774880000000L,
            patientId = "PAT-2026-0042",
            eye = "OD",
            grade = grade,
            confidence = conf,
            probabilities = listOf(0.012f, 0.045f, 0.892f, 0.031f, 0.020f),
            quality = "PASS",
            reviewStatus = reviewStatus,
            clinicianName = clinicianName
        )
    }

    @Test
    fun `test QR payload serialization and deserialization`() {
        val payload = createSamplePayload()
        val jsonString = payload.toJsonString()

        assertNotNull(jsonString)
        assertTrue(jsonString.contains("\"id\":101"))
        assertTrue(jsonString.contains("\"pid\":\"PAT-2026-0042\""))
        assertTrue(jsonString.contains("\"g\":2"))

        val parsed = QrScreeningPayload.fromJson(jsonString)
        assertNotNull(parsed)
        assertEquals(payload.version, parsed?.version)
        assertEquals(payload.reportId, parsed?.reportId)
        assertEquals(payload.timestamp, parsed?.timestamp)
        assertEquals(payload.patientId, parsed?.patientId)
        assertEquals(payload.eye, parsed?.eye)
        assertEquals(payload.grade, parsed?.grade)
        assertEquals(payload.confidence, parsed!!.confidence, 0.005f)
        assertEquals(payload.quality, parsed.quality)
        assertEquals(payload.reviewStatus, parsed.reviewStatus)
        assertEquals(payload.probabilities.size, parsed.probabilities.size)
    }

    @Test
    fun `test valid payload verification`() {
        val unsigned = createSamplePayload(reviewStatus = "PENDING")
        val signed = ReportSecurityManager.signPayload(unsigned)

        assertFalse(signed.signature.isBlank())

        val result = ReportSecurityManager.verifyPayload(signed)
        assertEquals(VerificationStatus.VALID, result.status)
        assertFalse(result.isClinicianConfirmed)
        assertEquals(signed.reportId, result.payload?.reportId)

        // Clinician confirmed case
        val signedConfirmed = ReportSecurityManager.signPayload(
            unsigned.copy(reviewStatus = "CONFIRMED", clinicianName = "Dr. Sarah Chen, MD")
        )
        val confirmedResult = ReportSecurityManager.verifyPayload(signedConfirmed)
        assertEquals(VerificationStatus.VALID, confirmedResult.status)
        assertTrue(confirmedResult.isClinicianConfirmed)
    }

    @Test
    fun `test modified payload rejection`() {
        val original = createSamplePayload(grade = 0, conf = 0.95f)
        val signed = ReportSecurityManager.signPayload(original)

        // Tampering: changing grade from 0 (No DR) to 4 (Proliferative DR) without re-signing
        val tamperedGrade = signed.copy(grade = 4)
        val resultTamperedGrade = ReportSecurityManager.verifyPayload(tamperedGrade)
        assertEquals(VerificationStatus.MODIFIED, resultTamperedGrade.status)

        // Tampering: modifying confidence score
        val tamperedConf = signed.copy(confidence = 0.50f)
        val resultTamperedConf = ReportSecurityManager.verifyPayload(tamperedConf)
        assertEquals(VerificationStatus.MODIFIED, resultTamperedConf.status)

        // Tampering: modifying patient ID
        val tamperedPatient = signed.copy(patientId = "PAT-HACKER")
        val resultTamperedPatient = ReportSecurityManager.verifyPayload(tamperedPatient)
        assertEquals(VerificationStatus.MODIFIED, resultTamperedPatient.status)
    }

    @Test
    fun `test unsupported version handling`() {
        val futurePayload = createSamplePayload(version = 99)
        val signed = ReportSecurityManager.signPayload(futurePayload)

        val result = ReportSecurityManager.verifyPayload(signed)
        assertEquals(VerificationStatus.UNSUPPORTED_VERSION, result.status)
    }

    @Test
    fun `test missing or invalid fields handling`() {
        val invalidRaw = "{}"
        val result1 = ReportSecurityManager.verifyRawJson(invalidRaw)
        assertEquals(VerificationStatus.MALFORMED, result1.status)

        val randomString = "https://arbitrary-unverified-website.com/data"
        val result2 = ReportSecurityManager.verifyRawJson(randomString)
        assertEquals(VerificationStatus.MALFORMED, result2.status)

        // Invalid grade (outside 0..4)
        val invalidGradePayload = createSamplePayload(grade = 10)
        val signedInvalid = ReportSecurityManager.signPayload(invalidGradePayload)
        val result3 = ReportSecurityManager.verifyPayload(signedInvalid)
        assertEquals(VerificationStatus.MALFORMED, result3.status)
    }

    @Test
    fun `test round-trip encode and decode via ZXing`() {
        val payload = createSamplePayload(id = 555L, grade = 3, conf = 0.88f)
        val signed = ReportSecurityManager.signPayload(payload)
        val json = signed.toJsonString()

        // Generate QR code Bitmap locally
        val qrBitmap = QrCodeGenerator.generateQrBitmap(json, 400)
        assertNotNull(qrBitmap)
        assertEquals(400, qrBitmap?.width)
        assertEquals(400, qrBitmap?.height)

        // Decode QR code from Bitmap locally
        val decodedText = QrCodeGenerator.decodeQrFromBitmap(qrBitmap!!)
        assertNotNull(decodedText)
        assertEquals(json, decodedText)

        // Verify decoded payload
        val report = ReportSecurityManager.verifyRawJson(decodedText!!)
        assertEquals(VerificationStatus.VALID, report.status)
        assertEquals(555L, report.payload?.reportId)
        assertEquals(3, report.payload?.grade)
    }

    @Test
    fun `test existing prediction and CAM pipeline integrity`() {
        // Verify 5 ICDR classes
        assertEquals(5, DiabeticRetinopathyGrade.entries.size)
        assertEquals(DiabeticRetinopathyGrade.GRADE_0, DiabeticRetinopathyGrade.fromGrade(0))
        assertEquals(DiabeticRetinopathyGrade.GRADE_4, DiabeticRetinopathyGrade.fromGrade(4))

        // Verify CAM generator deterministic behavior
        val features = FloatArray(CamGenerator.FEATURE_CHANNELS * CamGenerator.SPATIAL_SIZE) { 0.8f }
        val weights = FloatArray(CamGenerator.NUM_CLASSES * CamGenerator.FEATURE_CHANNELS) { 0.5f }
        val cam = CamGenerator.computeCamBitmap(features, weights, 2, 64, 64)
        assertNotNull(cam)
        assertEquals(64, cam?.width)
        assertEquals(64, cam?.height)
    }

    @Test
    fun `test existing Room history entity to report transformation`() {
        val entity = ScreeningEntity(
            id = 42L,
            patientId = "PAT-9912",
            patientName = "Jane Doe",
            patientAge = 62,
            eyeSide = "OS",
            timestamp = 1774880000000L,
            imagePath = "/data/user/0/com.example/files/retina.jpg",
            gradCamPath = "/data/user/0/com.example/files/cam.png",
            qualityStatus = "PASS",
            focusScore = 95f,
            illuminationScore = 91f,
            predictedGrade = 1,
            gradeTitle = "Mild NPDR",
            confidence = 0.84f,
            isReferable = false,
            probabilitiesJson = "{\"0\":0.10,\"1\":0.84,\"2\":0.04,\"3\":0.01,\"4\":0.01}",
            reviewStatus = "PENDING"
        )

        val report = ScreeningReport.fromEntity(entity)
        assertEquals("DRISHTI-RPT-00042", report.reportId)
        assertEquals("Jane Doe", report.patientName)
        assertEquals("OS", report.eyeSide)
        assertEquals(1, report.predictedGrade)
        assertEquals(5, report.probabilities.size)
        assertEquals(0.84f, report.probabilities[1] ?: 0f, 0.01f)

        val formattedText = report.toFormattedReportText()
        assertTrue(formattedText.contains("DRISHTI AI RETINAL SCREENING REPORT"))
        assertTrue(formattedText.contains("Jane Doe"))
        assertTrue(formattedText.contains("Mild NPDR"))
        assertTrue(formattedText.contains("AI-assisted retinal screening result"))
    }
}
