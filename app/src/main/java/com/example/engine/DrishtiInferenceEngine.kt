package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import com.example.model.AnalysisResult
import com.example.model.DiabeticRetinopathyGrade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * Executes the offline AI analysis workflow on retinal fundus images using
 * the local ONNX Runtime classifier (drishti_model.onnx).
 */
class DrishtiInferenceEngine(private val context: Context) : Closeable {

    private val classifier = DrishtiClassifier(context)

    /**
     * Executes the on-device AI analysis pipeline.
     * Uses real ONNX Runtime inference exclusively.
     */
    suspend fun analyze(
        bitmap: Bitmap,
        onProgressUpdate: (step: String, progress: Float) -> Unit = { _, _ -> }
    ): AnalysisResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        // Stage 1: Load ONNX Runtime model session
        onProgressUpdate("Initializing ONNX Runtime offline session...", 0.25f)
        val initResult = classifier.initialize()
        if (initResult.isFailure) {
            throw initResult.exceptionOrNull()
                ?: IllegalStateException("Failed to initialize ONNX Runtime engine.")
        }

        // Stage 2 & 3: Bilinear resize, ImageNet normalization, Float32 CHW tensor & inference
        onProgressUpdate("Executing EfficientNet-B0 inference on drishti_model.onnx...", 0.70f)
        val predictionResult = classifier.classify(bitmap)
        val prediction = predictionResult.getOrThrow()

        // Stage 4: Probability calibration and CAM feature map alignment
        onProgressUpdate("Computing Class Activation Map (CAM) from model features...", 0.90f)
        onProgressUpdate("Screening analysis complete.", 1.0f)
        val totalTime = System.currentTimeMillis() - startTime

        val detectedGrade = DiabeticRetinopathyGrade.fromGrade(prediction.classIndex)
        val probabilities = prediction.probabilities.mapKeys { (classIdx, _) ->
            DiabeticRetinopathyGrade.fromGrade(classIdx)
        }

        AnalysisResult(
            predictedGrade = detectedGrade,
            confidence = prediction.confidence,
            probabilityDistribution = probabilities,
            lesionCues = emptyList(), // No synthetic or fabricated cues
            gradCamBitmap = prediction.camBitmap, // Real CAM from cam_features & classifier weights
            executionTimeMs = totalTime,
            inferenceEngineName = "Drishti ONNX Runtime (EfficientNet-B0)"
        )
    }

    override fun close() {
        classifier.close()
    }
}
