package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.QualityCheckResult
import com.example.model.QualityMetric
import com.example.model.QualityStatus
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * ImageAnalysisService performs pre-inference validation:
 * 1. Image Sharpness (Laplacian variance calculation)
 * 2. Illumination thresholds (Mean luminance, overexposure, underexposure, contrast)
 * 3. Image Resolution (Minimum and optimal pixel dimensions)
 * 4. Retinal Field Suitability (Chromaticity and coverage ratio)
 *
 * Enforces gating before allowing the flow to proceed to AI inference.
 */
class ImageAnalysisService {

    companion object {
        const val MIN_RESOLUTION_PX = 512
        const val OPTIMAL_RESOLUTION_PX = 1024

        const val MIN_LUMINANCE_THRESHOLD = 35.0
        const val OPTIMAL_MIN_LUMINANCE = 45.0
        const val OPTIMAL_MAX_LUMINANCE = 185.0
        const val MAX_OVEREXPOSURE_PCT = 15.0f
        const val MAX_UNDEREXPOSURE_PCT = 45.0f
        const val MIN_CONTRAST_STD_DEV = 22.0f

        const val MIN_LAPLACIAN_VARIANCE_PASS = 75.0
        const val MIN_LAPLACIAN_VARIANCE_ACCEPTABLE = 35.0
    }

    /**
     * Complete quality evaluation pipeline.
     */
    fun analyzeImageQuality(bitmap: Bitmap): QualityCheckResult {
        val width = bitmap.width
        val height = bitmap.height

        val resolutionMetric = checkResolution(width, height)

        // Downsample to 256x256 for fast, normalized Laplacian and illumination calculation
        val sampleSize = 256
        val sample = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
        val pixels = IntArray(sampleSize * sampleSize)
        sample.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

        val illuminationMetric = checkIllumination(pixels, sampleSize)
        val sharpnessMetric = checkSharpness(pixels, sampleSize)
        val suitabilityMetric = checkRetinalSuitability(pixels, sampleSize)

        // Evaluate overall quality gate
        val failCount = listOf(resolutionMetric, illuminationMetric, sharpnessMetric, suitabilityMetric)
            .count { it.status == QualityStatus.FAIL }
        val warningCount = listOf(resolutionMetric, illuminationMetric, sharpnessMetric, suitabilityMetric)
            .count { it.status == QualityStatus.WARNING }

        val overallStatus = when {
            failCount > 0 -> QualityStatus.FAIL
            warningCount >= 2 -> QualityStatus.WARNING
            else -> QualityStatus.PASS
        }

        val guidance = mutableListOf<String>()
        if (sharpnessMetric.status == QualityStatus.FAIL) {
            guidance.add("Focus Issue: Significant motion blur or optical defocus detected. Adjust the diopter wheel and steady the device.")
        }
        if (illuminationMetric.status == QualityStatus.FAIL) {
            guidance.add("Illumination Issue: Image is under-illuminated or has excessive specular glare. Adjust lighting and avoid corneal reflection.")
        }
        if (resolutionMetric.status == QualityStatus.FAIL) {
            guidance.add("Resolution Substandard: Image is below the required 512×512 threshold. Capture in high-resolution mode.")
        }
        if (suitabilityMetric.status == QualityStatus.FAIL) {
            guidance.add("Field Alignment: Retinal fundus signature not verified. Center the macula and optic disc in the optical guide.")
        }
        if (guidance.isEmpty()) {
            guidance.add("Image satisfies clinical screening standards. Verified for AI inference.")
        }

        return QualityCheckResult(
            overallStatus = overallStatus,
            resolutionMetric = resolutionMetric,
            illuminationMetric = illuminationMetric,
            focusMetric = sharpnessMetric,
            suitabilityMetric = suitabilityMetric,
            guidanceMessages = guidance,
            imageWidth = width,
            imageHeight = height
        )
    }

    /**
     * Validates image pixel dimensions.
     */
    fun checkResolution(width: Int, height: Int): QualityMetric {
        val minDim = min(width, height)
        return when {
            minDim >= OPTIMAL_RESOLUTION_PX -> QualityMetric(
                name = "Image Resolution",
                score = 100f,
                targetRange = "≥ 1024×1024 px",
                status = QualityStatus.PASS,
                feedback = "Optimal diagnostic resolution (${width}×${height} px)"
            )
            minDim >= MIN_RESOLUTION_PX -> QualityMetric(
                name = "Image Resolution",
                score = 75f,
                targetRange = "≥ 1024×1024 px",
                status = QualityStatus.WARNING,
                feedback = "Acceptable resolution (${width}×${height} px); ≥ 1024×1024 recommended for micro-lesions"
            )
            else -> QualityMetric(
                name = "Image Resolution",
                score = 25f,
                targetRange = "≥ 512×512 px",
                status = QualityStatus.FAIL,
                feedback = "Substandard resolution (${width}×${height} px). Cannot resolve microaneurysms."
            )
        }
    }

    /**
     * Evaluates illumination, exposure thresholds, and contrast.
     */
    fun checkIllumination(pixels: IntArray, sampleSize: Int): QualityMetric {
        var sumLuminance = 0.0
        var overexposedPixels = 0
        var underexposedPixels = 0
        var validRetinalPixels = 0

        val luminances = DoubleArray(pixels.size)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Retinal luminance (green-weighted ophthalmic perceptual formula)
            val lum = 0.299 * r + 0.587 * g + 0.114 * b
            luminances[i] = lum

            // Exclude black borders outside the circular aperture
            if (lum > 8) {
                validRetinalPixels++
                sumLuminance += lum
                if (lum > 240) overexposedPixels++
                if (lum < 25) underexposedPixels++
            }
        }

        val totalValid = max(1, validRetinalPixels)
        val meanLuminance = sumLuminance / totalValid
        val overexposePct = (overexposedPixels.toFloat() / totalValid) * 100f
        val underexposePct = (underexposedPixels.toFloat() / totalValid) * 100f

        // Contrast standard deviation
        var sumVariance = 0.0
        for (i in pixels.indices) {
            val lum = luminances[i]
            if (lum > 8) {
                val diff = lum - meanLuminance
                sumVariance += diff * diff
            }
        }
        val contrastStdDev = sqrt(sumVariance / totalValid).toFloat()

        return when {
            overexposePct > MAX_OVEREXPOSURE_PCT -> QualityMetric(
                name = "Illumination & Exposure",
                score = 35f,
                targetRange = "Balanced (Glare < 15%)",
                status = QualityStatus.WARNING,
                feedback = "Severe optical glare or flash overexposure (${String.format("%.1f", overexposePct)}% saturation)"
            )
            meanLuminance < MIN_LUMINANCE_THRESHOLD || underexposePct > MAX_UNDEREXPOSURE_PCT -> QualityMetric(
                name = "Illumination & Exposure",
                score = 25f,
                targetRange = "Mean Lum 45–185",
                status = QualityStatus.FAIL,
                feedback = "Under-illuminated (${String.format("%.1f", meanLuminance)} mean lum). Vascular network obscured."
            )
            meanLuminance in OPTIMAL_MIN_LUMINANCE..OPTIMAL_MAX_LUMINANCE && contrastStdDev >= MIN_CONTRAST_STD_DEV -> QualityMetric(
                name = "Illumination & Exposure",
                score = 95f,
                targetRange = "Mean Lum 45–185",
                status = QualityStatus.PASS,
                feedback = "Optimal exposure (${String.format("%.1f", meanLuminance)}) with high vascular contrast"
            )
            else -> QualityMetric(
                name = "Illumination & Exposure",
                score = 65f,
                targetRange = "Mean Lum 45–185",
                status = QualityStatus.WARNING,
                feedback = "Sub-optimal illumination contrast. Screening reliable but clinician review advised."
            )
        }
    }

    /**
     * Calculates image sharpness using the discrete 2D Laplacian operator on the green channel.
     * High Laplacian variance indicates sharp edges and crisp vascular boundaries.
     */
    fun checkSharpness(pixels: IntArray, sampleSize: Int): QualityMetric {
        var laplacianSum = 0.0
        var laplacianSqSum = 0.0
        var countLaplacian = 0

        for (y in 1 until sampleSize - 1) {
            val rowOffset = y * sampleSize
            for (x in 1 until sampleSize - 1) {
                val centerPixel = pixels[rowOffset + x]
                val centerGreen = Color.green(centerPixel)

                // Skip dark outer margin
                if (centerGreen < 12) continue

                val topGreen = Color.green(pixels[rowOffset - sampleSize + x])
                val bottomGreen = Color.green(pixels[rowOffset + sampleSize + x])
                val leftGreen = Color.green(pixels[rowOffset + x - 1])
                val rightGreen = Color.green(pixels[rowOffset + x + 1])

                // 2D Discrete Laplacian Filter: [0, 1, 0; 1, -4, 1; 0, 1, 0]
                val laplacianVal = (topGreen + bottomGreen + leftGreen + rightGreen - (4 * centerGreen)).toDouble()
                laplacianSum += laplacianVal
                laplacianSqSum += laplacianVal * laplacianVal
                countLaplacian++
            }
        }

        val laplacianVariance = if (countLaplacian > 0) {
            val mean = laplacianSum / countLaplacian
            (laplacianSqSum / countLaplacian) - (mean * mean)
        } else {
            0.0
        }

        return when {
            laplacianVariance >= MIN_LAPLACIAN_VARIANCE_PASS -> QualityMetric(
                name = "Focus & Sharpness",
                score = min(100f, (laplacianVariance.toFloat() / 1.8f) + 40f),
                targetRange = "Laplacian Var ≥ 75",
                status = QualityStatus.PASS,
                feedback = "Sharp focus (Laplacian Var: ${String.format("%.1f", laplacianVariance)}). Microvasculature clearly delineated."
            )
            laplacianVariance >= MIN_LAPLACIAN_VARIANCE_ACCEPTABLE -> QualityMetric(
                name = "Focus & Sharpness",
                score = 65f,
                targetRange = "Laplacian Var ≥ 75",
                status = QualityStatus.WARNING,
                feedback = "Mild defocus (Laplacian Var: ${String.format("%.1f", laplacianVariance)}). Fine microaneurysms may be attenuated."
            )
            else -> QualityMetric(
                name = "Focus & Sharpness",
                score = 20f,
                targetRange = "Laplacian Var ≥ 75",
                status = QualityStatus.FAIL,
                feedback = "Motion blur or severe out-of-focus capture (Laplacian Var: ${String.format("%.1f", laplacianVariance)})."
            )
        }
    }

    /**
     * Checks retinal fundus chromaticity profile (red-dominant vascular tissue) and field coverage.
     */
    fun checkRetinalSuitability(pixels: IntArray, sampleSize: Int): QualityMetric {
        var sumRed = 0.0
        var sumGreen = 0.0
        var sumBlue = 0.0
        var validPixels = 0

        for (pixel in pixels) {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val lum = 0.299 * r + 0.587 * g + 0.114 * b
            if (lum > 8) {
                validPixels++
                sumRed += r
                sumGreen += g
                sumBlue += b
            }
        }

        val total = max(1, validPixels)
        val avgR = sumRed / total
        val avgG = sumGreen / total
        val avgB = sumBlue / total
        val coverageRatio = validPixels.toFloat() / (sampleSize * sampleSize)

        val isWarmRetinalProfile = (avgR > avgG * 1.15) && (avgR > avgB * 1.6)
        val hasAdequateCoverage = coverageRatio in 0.35f..0.98f

        return when {
            isWarmRetinalProfile && hasAdequateCoverage -> QualityMetric(
                name = "Retinal Field Suitability",
                score = 95f,
                targetRange = "Fundus Chromaticity Match",
                status = QualityStatus.PASS,
                feedback = "Valid retinal fundus field verified with proper chromaticity profile"
            )
            isWarmRetinalProfile -> QualityMetric(
                name = "Retinal Field Suitability",
                score = 60f,
                targetRange = "Fundus Chromaticity Match",
                status = QualityStatus.WARNING,
                feedback = "Field boundaries decentered or partial pupil alignment"
            )
            else -> QualityMetric(
                name = "Retinal Field Suitability",
                score = 15f,
                targetRange = "Fundus Chromaticity Match",
                status = QualityStatus.FAIL,
                feedback = "Non-retinal input detected. Image lacks characteristic retinal tissue signatures."
            )
        }
    }

    /**
     * Enforces the gate: Returns true only if quality meets acceptable criteria to proceed to AI inference.
     */
    fun canProceedToInference(result: QualityCheckResult?): Boolean {
        if (result == null) return false
        return result.isAcceptableForAnalysis
    }
}
