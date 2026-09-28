package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Mathematical Class Activation Map (CAM) generator for EfficientNet-B0.
 *
 * For a Global Average Pooled convolutional architecture:
 * CAM_c(x, y) = ReLU( \sum_{k=0}^{1279} w_{c, k} \cdot F_{k}(x, y) )
 *
 * Where:
 * - c is the predicted class index (0..4)
 * - w_{c, k} is the classification layer weight connecting feature channel k to class c
 * - F_{k}(x, y) is the spatial activation at position (x, y) in the 7×7 final convolutional feature map (features.8)
 */
object CamGenerator {

    const val FEATURE_CHANNELS = 1280
    const val FEATURE_HEIGHT = 7
    const val FEATURE_WIDTH = 7
    const val SPATIAL_SIZE = FEATURE_HEIGHT * FEATURE_WIDTH // 49
    const val NUM_CLASSES = 5

    /**
     * Computes the Class Activation Map for the given predicted class using the actual
     * model weights and feature activations.
     *
     * @param features FloatArray of size 1280 * 7 * 7 in CHW layout
     * @param classifierWeights FloatArray of size 5 * 1280 (row-major: classIndex * 1280 + channel)
     * @param classIndex The target class index (0..4)
     * @param targetWidth Output image width
     * @param targetHeight Output image height
     * @return Transparent ARGB Bitmap overlay aligned with the original retinal image, or null if invalid inputs
     */
    fun computeCamBitmap(
        features: FloatArray,
        classifierWeights: FloatArray,
        classIndex: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        if (classIndex !in 0 until NUM_CLASSES) return null
        val expectedFeaturesSize = FEATURE_CHANNELS * SPATIAL_SIZE
        if (features.size < expectedFeaturesSize) return null
        val expectedWeightsSize = NUM_CLASSES * FEATURE_CHANNELS
        if (classifierWeights.size < expectedWeightsSize) return null
        if (targetWidth <= 0 || targetHeight <= 0) return null

        val weightOffset = classIndex * FEATURE_CHANNELS

        // 1. Calculate raw CAM(y, x) for the 7x7 grid
        val rawCam = FloatArray(SPATIAL_SIZE)
        var maxCam = 0.0f

        for (y in 0 until FEATURE_HEIGHT) {
            val yOffset = y * FEATURE_WIDTH
            for (x in 0 until FEATURE_WIDTH) {
                val spatialIdx = yOffset + x
                var sum = 0.0f
                for (k in 0 until FEATURE_CHANNELS) {
                    val w = classifierWeights[weightOffset + k]
                    val feat = features[k * SPATIAL_SIZE + spatialIdx]
                    sum += w * feat
                }
                // ReLU: retain only features that positively contribute to the target class
                val positiveContribution = max(0.0f, sum)
                rawCam[spatialIdx] = positiveContribution
                if (positiveContribution > maxCam) {
                    maxCam = positiveContribution
                }
            }
        }

        // 2. Normalize CAM to [0.0, 1.0]
        val normalizedCam = FloatArray(SPATIAL_SIZE)
        if (maxCam > 0.0f) {
            for (i in 0 until SPATIAL_SIZE) {
                normalizedCam[i] = rawCam[i] / maxCam
            }
        }

        // 3. Render 7x7 thermal color palette
        val colors7x7 = IntArray(SPATIAL_SIZE)
        for (i in 0 until SPATIAL_SIZE) {
            val value = normalizedCam[i]
            colors7x7[i] = mapValueToThermalColor(value)
        }

        val bitmap7x7 = Bitmap.createBitmap(colors7x7, FEATURE_WIDTH, FEATURE_HEIGHT, Bitmap.Config.ARGB_8888)

        // 4. Bilinear scaling to target image dimensions for smooth spatial attention
        val scaledCam = Bitmap.createScaledBitmap(bitmap7x7, targetWidth, targetHeight, true)
        if (scaledCam != bitmap7x7) {
            bitmap7x7.recycle()
        }

        return scaledCam
    }

    /**
     * Maps normalized saliency [0.0, 1.0] to a thermal colormap with alpha transparency:
     * - 0.00..0.15: Fully transparent (background / low attention)
     * - 0.15..0.40: Blue to Cyan
     * - 0.40..0.65: Cyan to Green/Yellow
     * - 0.65..0.85: Yellow to Orange
     * - 0.85..1.00: Orange to Vivid Red (focal lesion/pathology focus)
     */
    fun mapValueToThermalColor(value: Float): Int {
        if (value < 0.12f) {
            return Color.TRANSPARENT
        }

        // Normalize value in the active [0.12, 1.0] range
        val t = ((value - 0.12f) / 0.88f).coerceIn(0.0f, 1.0f)

        // Alpha ramps from 70 (subtle) to 215 (vivid overlay)
        val alpha = (70 + (t * 145f)).toInt().coerceIn(0, 255)

        val r: Int
        val g: Int
        val b: Int

        when {
            t < 0.25f -> {
                // Blue to Cyan: (0, 30, 180) -> (0, 200, 220)
                val f = t / 0.25f
                r = 0
                g = (30 + f * 170).toInt()
                b = (180 + f * 40).toInt()
            }
            t < 0.50f -> {
                // Cyan to Green: (0, 200, 220) -> (16, 185, 129)
                val f = (t - 0.25f) / 0.25f
                r = (f * 16).toInt()
                g = (200 - f * 15).toInt()
                b = (220 - f * 91).toInt()
            }
            t < 0.75f -> {
                // Green to Yellow/Amber: (16, 185, 129) -> (245, 158, 11)
                val f = (t - 0.50f) / 0.25f
                r = (16 + f * 229).toInt()
                g = (185 - f * 27).toInt()
                b = (129 - f * 118).toInt()
            }
            else -> {
                // Amber to Vivid Red: (245, 158, 11) -> (239, 68, 68)
                val f = (t - 0.75f) / 0.25f
                r = (245 - f * 6).toInt()
                g = (158 - f * 90).toInt()
                b = (11 + f * 57).toInt()
            }
        }

        return Color.argb(alpha, r, g, b)
    }
}
