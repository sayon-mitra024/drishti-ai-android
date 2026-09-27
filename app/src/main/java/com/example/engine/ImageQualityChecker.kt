package com.example.engine

import android.graphics.Bitmap
import com.example.model.QualityCheckResult

object ImageQualityChecker {
    private val service = ImageAnalysisService()

    fun evaluate(bitmap: Bitmap): QualityCheckResult {
        return service.analyzeImageQuality(bitmap)
    }

    fun canProceedToInference(result: QualityCheckResult?): Boolean {
        return service.canProceedToInference(result)
    }
}
