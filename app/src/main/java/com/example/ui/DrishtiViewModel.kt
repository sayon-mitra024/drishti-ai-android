package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DrishtiApp
import com.example.data.ScreeningEntity
import com.example.data.ScreeningRepository
import com.example.engine.DrishtiInferenceEngine
import com.example.engine.GradCamGenerator
import com.example.engine.ImageQualityChecker
import com.example.engine.ImageUtils
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ScreenStep {
    CAPTURE,
    QUALITY_CHECK,
    ANALYSIS_RUNNING,
    RESULTS,
    HISTORY,
    GUIDELINES
}

enum class GradCamDisplayMode {
    OVERLAY,
    ORIGINAL,
    HEATMAP_ONLY,
    SPLIT
}

data class UiState(
    val currentStep: ScreenStep = ScreenStep.CAPTURE,
    val patientInfo: PatientInfo = PatientInfo(),
    val capturedBitmap: Bitmap? = null,
    val capturedImageUri: Uri? = null,
    val localImagePath: String? = null,
    val qualityResult: QualityCheckResult? = null,
    val isAnalyzing: Boolean = false,
    val analysisProgressText: String = "",
    val analysisProgressPercent: Float = 0f,
    val analysisResult: AnalysisResult? = null,
    val gradCamOpacity: Float = 0.65f,
    val gradCamDisplayMode: GradCamDisplayMode = GradCamDisplayMode.OVERLAY,
    val showLesionMarkers: Boolean = true,
    val clinicianReview: ClinicianReview = ClinicianReview(),
    val isClinicianDialogOpen: Boolean = false,
    val isRecaptureDialogOpen: Boolean = false,
    val savedScreeningId: Long? = null,
    val selectedHistoryScreening: ScreeningEntity? = null,
    val errorMessage: String? = null
)

class DrishtiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScreeningRepository = (application as DrishtiApp).repository
    private val inferenceEngine = DrishtiInferenceEngine(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allScreenings: StateFlow<List<ScreeningEntity>> = repository.allScreenings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updatePatientInfo(
        fullName: String? = null,
        age: Int? = null,
        eyeSide: EyeSide? = null,
        diabetesYears: Int? = null
    ) {
        val current = _uiState.value.patientInfo
        _uiState.value = _uiState.value.copy(
            patientInfo = current.copy(
                fullName = fullName ?: current.fullName,
                age = age ?: current.age,
                eyeSide = eyeSide ?: current.eyeSide,
                diabetesDurationYears = diabetesYears ?: current.diabetesDurationYears
            )
        )
    }

    fun onImageCaptured(bitmap: Bitmap, uri: Uri? = null) {
        viewModelScope.launch {
            // Save bitmap to internal cache
            val file = ImageUtils.saveBitmapToFile(getApplication(), bitmap, "retina_capture_")
            val quality = ImageQualityChecker.evaluate(bitmap)

            _uiState.value = _uiState.value.copy(
                capturedBitmap = bitmap,
                capturedImageUri = uri,
                localImagePath = file.absolutePath,
                qualityResult = quality,
                currentStep = ScreenStep.QUALITY_CHECK,
                analysisResult = null,
                clinicianReview = ClinicianReview(),
                savedScreeningId = null,
                errorMessage = null
            )
        }
    }

    fun proceedToAnalysis() {
        val bitmap = _uiState.value.capturedBitmap ?: return
        val quality = _uiState.value.qualityResult
        if (!ImageQualityChecker.canProceedToInference(quality)) {
            _uiState.value = _uiState.value.copy(
                isRecaptureDialogOpen = true,
                errorMessage = "Quality validation failed. Focus, illumination, or resolution is below the clinical threshold. Please retake photo."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            currentStep = ScreenStep.ANALYSIS_RUNNING,
            isAnalyzing = true,
            analysisProgressText = "Initializing clinical inference engine...",
            analysisProgressPercent = 0.05f
        )

        viewModelScope.launch {
            try {
                val result = inferenceEngine.analyze(bitmap) { stepText, progress ->
                    _uiState.value = _uiState.value.copy(
                        analysisProgressText = stepText,
                        analysisProgressPercent = progress
                    )
                }

                // Save Grad-CAM heatmap to file
                var gradCamPath: String? = null
                result.gradCamBitmap?.let { heatmap ->
                    val file = ImageUtils.saveBitmapToFile(getApplication(), heatmap, "gradcam_")
                    gradCamPath = file.absolutePath
                }

                // Persist screening to Room Database
                val quality = _uiState.value.qualityResult
                val patient = _uiState.value.patientInfo
                val probabilitiesJson = result.probabilityDistribution.entries
                    .joinToString(",") { "${it.key.grade}:${String.format("%.3f", it.value)}" }

                val entity = ScreeningEntity(
                    patientId = patient.patientId,
                    patientName = patient.fullName,
                    patientAge = patient.age,
                    eyeSide = patient.eyeSide.name,
                    imagePath = _uiState.value.localImagePath ?: "",
                    gradCamPath = gradCamPath,
                    qualityStatus = quality?.overallStatus?.name ?: "PASS",
                    focusScore = quality?.focusMetric?.score ?: 90f,
                    illuminationScore = quality?.illuminationMetric?.score ?: 90f,
                    predictedGrade = result.predictedGrade.grade,
                    gradeTitle = result.predictedGrade.title,
                    confidence = result.confidence,
                    isReferable = result.predictedGrade.isReferable,
                    probabilitiesJson = probabilitiesJson,
                    reviewStatus = ReviewStatus.PENDING.name
                )

                val id = repository.saveScreening(entity)

                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisResult = result,
                    currentStep = ScreenStep.RESULTS,
                    savedScreeningId = id
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    currentStep = ScreenStep.QUALITY_CHECK,
                    errorMessage = "Error during on-device analysis: ${e.message}"
                )
            }
        }
    }

    fun retakeImage() {
        _uiState.value = _uiState.value.copy(
            currentStep = ScreenStep.CAPTURE,
            capturedBitmap = null,
            capturedImageUri = null,
            qualityResult = null,
            analysisResult = null,
            clinicianReview = ClinicianReview()
        )
    }

    fun setStep(step: ScreenStep) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    fun setGradCamOpacity(opacity: Float) {
        _uiState.value = _uiState.value.copy(gradCamOpacity = opacity)
    }

    fun setGradCamDisplayMode(mode: GradCamDisplayMode) {
        _uiState.value = _uiState.value.copy(gradCamDisplayMode = mode)
    }

    fun toggleLesionMarkers() {
        _uiState.value = _uiState.value.copy(showLesionMarkers = !_uiState.value.showLesionMarkers)
    }

    fun showClinicianReviewDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isClinicianDialogOpen = show)
    }

    fun showRecaptureDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isRecaptureDialogOpen = show)
    }

    fun submitClinicianReview(
        name: String,
        licenseId: String,
        agreed: Boolean,
        overrideGrade: DiabeticRetinopathyGrade?,
        notes: String
    ) {
        val review = ClinicianReview(
            status = if (agreed) ReviewStatus.CONFIRMED else ReviewStatus.MODIFIED,
            clinicianName = name,
            clinicianId = licenseId,
            agreedWithAi = agreed,
            assignedGrade = overrideGrade,
            clinicalNotes = notes,
            reviewTimestamp = System.currentTimeMillis()
        )

        _uiState.value = _uiState.value.copy(
            clinicianReview = review,
            isClinicianDialogOpen = false
        )

        // Update in Room Database if saved
        val id = _uiState.value.savedScreeningId ?: return
        viewModelScope.launch {
            val existing = repository.getScreeningById(id)
            if (existing != null) {
                val updated = existing.copy(
                    reviewStatus = review.status.name,
                    clinicianName = review.clinicianName,
                    clinicianNotes = review.clinicalNotes,
                    clinicianAssignedGrade = review.assignedGrade?.grade,
                    reviewTimestamp = review.reviewTimestamp
                )
                repository.updateScreening(updated)
            }
        }
    }

    fun loadSampleFundus(drawableId: Int, grade: DiabeticRetinopathyGrade) {
        try {
            val bitmap = BitmapFactory.decodeResource(getApplication<Application>().resources, drawableId)
            if (bitmap != null) {
                onImageCaptured(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun viewPastScreening(screening: ScreeningEntity) {
        _uiState.value = _uiState.value.copy(
            selectedHistoryScreening = screening
        )
    }

    fun clearSelectedHistoryScreening() {
        _uiState.value = _uiState.value.copy(selectedHistoryScreening = null)
    }

    fun deleteScreening(id: Long) {
        viewModelScope.launch {
            repository.deleteScreening(id)
            if (_uiState.value.selectedHistoryScreening?.id == id) {
                _uiState.value = _uiState.value.copy(selectedHistoryScreening = null)
            }
        }
    }
}
