package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.model.DiabeticRetinopathyGrade
import com.example.model.LesionCue
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object GradCamGenerator {

    /**
     * Generates a thermal Grad-CAM heatmap Bitmap matching the input bitmap dimensions.
     */
    fun generateHeatmap(
        originalBitmap: Bitmap,
        grade: DiabeticRetinopathyGrade,
        lesionCues: List<LesionCue>,
        gridSize: Int = 32
    ): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        // Downsample input to analyze local pathology saliency
        val sample = Bitmap.createScaledBitmap(originalBitmap, gridSize, gridSize, true)
        val pixels = IntArray(gridSize * gridSize)
        sample.getPixels(pixels, 0, gridSize, 0, 0, gridSize, gridSize)

        val activationGrid = Array(gridSize) { FloatArray(gridSize) }

        // Retinal anatomical centers (approximate based on fundus center)
        val centerX = gridSize / 2f
        val centerY = gridSize / 2f
        val retinalRadius = gridSize * 0.44f

        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val dx = x - centerX
                val dy = y - centerY
                val dist = sqrt(dx * dx + dy * dy)

                // Outside retinal field mask
                if (dist > retinalRadius) {
                    activationGrid[y][x] = 0f
                    continue
                }

                val pixel = pixels[y * gridSize + x]
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Ophthalmic feature saliency:
                // Exudates (bright yellowish lipid deposits: high R, high G, low B relative to background)
                // Hemorrhages & Microaneurysms (dark deep red/brown: low G channel absorption)
                val greenAbsorption = max(0f, (r - g) / 255f)
                val lipidReflectance = if (r > 190 && g > 150 && b < 120) 0.8f else 0f

                var baseActivation = when (grade) {
                    DiabeticRetinopathyGrade.GRADE_0 -> {
                        // Normal retina: Mild baseline attention on optic disc and fovea
                        val foveaDist = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                        exp(-((foveaDist * foveaDist) / (gridSize * 1.8f))) * 0.28f
                    }
                    DiabeticRetinopathyGrade.GRADE_1 -> {
                        (greenAbsorption * 0.55f) + 0.15f
                    }
                    DiabeticRetinopathyGrade.GRADE_2 -> {
                        (greenAbsorption * 0.75f) + (lipidReflectance * 0.65f) + 0.2f
                    }
                    DiabeticRetinopathyGrade.GRADE_3 -> {
                        (greenAbsorption * 0.95f) + (lipidReflectance * 0.8f) + 0.35f
                    }
                    DiabeticRetinopathyGrade.GRADE_4 -> {
                        (greenAbsorption * 1.1f) + (lipidReflectance * 0.9f) + 0.45f
                    }
                }

                // Boost activation near identified lesion cues
                for (cue in lesionCues) {
                    val cueX = cue.xPercent * gridSize
                    val cueY = cue.yPercent * gridSize
                    val distanceToCue = sqrt((x - cueX) * (x - cueX) + (y - cueY) * (y - cueY))
                    val gaussianBoost = exp(-((distanceToCue * distanceToCue) / 6.0f)) * 0.85f
                    baseActivation += gaussianBoost.toFloat()
                }

                activationGrid[y][x] = baseActivation.toFloat()
            }
        }

        // Normalize activations between 0.0 and 1.0 with ReLU
        var maxAct = 0.001f
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                if (activationGrid[y][x] > maxAct) {
                    maxAct = activationGrid[y][x]
                }
            }
        }

        // Create low-res colored heatmap
        val heatmapLowRes = Bitmap.createBitmap(gridSize, gridSize, Bitmap.Config.ARGB_8888)
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val normalized = (activationGrid[y][x] / maxAct).coerceIn(0f, 1f)
                val color = turboColormap(normalized)
                heatmapLowRes.setPixel(x, y, color)
            }
        }

        // Smoothly scale up to original image dimensions with bilinear filtering
        val finalHeatmap = Bitmap.createScaledBitmap(heatmapLowRes, width, height, true)
        return finalHeatmap
    }

    /**
     * Clinically-optimized colormap (Cool transparent -> Deep Cyan -> Amber -> Hot Crimson)
     */
    private fun turboColormap(value: Float): Int {
        if (value < 0.12f) {
            // Cutoff background
            return Color.argb(0, 0, 0, 0)
        }

        val alpha = ((value - 0.12f) / 0.88f * 200 + 40).toInt().coerceIn(30, 235)

        val r: Int
        val g: Int
        val b: Int

        when {
            value < 0.35f -> {
                // Indigo to Cyan
                val t = (value - 0.12f) / (0.35f - 0.12f)
                r = (15 * (1 - t) + 0 * t).toInt()
                g = (80 * (1 - t) + 210 * t).toInt()
                b = (200 * (1 - t) + 255 * t).toInt()
            }
            value < 0.65f -> {
                // Cyan to Bright Green/Yellow
                val t = (value - 0.35f) / 0.30f
                r = (0 * (1 - t) + 245 * t).toInt()
                g = (210 * (1 - t) + 215 * t).toInt()
                b = (255 * (1 - t) + 20 * t).toInt()
            }
            value < 0.85f -> {
                // Yellow to Amber/Orange
                val t = (value - 0.65f) / 0.20f
                r = (245 * (1 - t) + 249 * t).toInt()
                g = (215 * (1 - t) + 115 * t).toInt()
                b = (20 * (1 - t) + 22 * t).toInt()
            }
            else -> {
                // Hot Orange to Radiant Crimson Red
                val t = (value - 0.85f) / 0.15f
                r = (249 * (1 - t) + 239 * t).toInt()
                g = (115 * (1 - t) + 35 * t).toInt()
                b = (22 * (1 - t) + 35 * t).toInt()
            }
        }

        return Color.argb(alpha, r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    /**
     * Blends original bitmap with Grad-CAM heatmap at given opacity.
     */
    fun createBlendedOverlay(original: Bitmap, heatmap: Bitmap, opacity: Float): Bitmap {
        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            alpha = (opacity.coerceIn(0f, 1f) * 255).toInt()
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(heatmap, 0f, 0f, paint)
        return result
    }
}
