package com.example.model

enum class QualityStatus {
    PASS,
    WARNING,
    FAIL
}

data class QualityMetric(
    val name: String,
    val score: Float,
    val targetRange: String,
    val status: QualityStatus,
    val feedback: String
)

data class QualityCheckResult(
    val overallStatus: QualityStatus,
    val resolutionMetric: QualityMetric,
    val illuminationMetric: QualityMetric,
    val focusMetric: QualityMetric,
    val suitabilityMetric: QualityMetric,
    val guidanceMessages: List<String>,
    val imageWidth: Int,
    val imageHeight: Int
) {
    val isAcceptableForAnalysis: Boolean
        get() = overallStatus != QualityStatus.FAIL
}
