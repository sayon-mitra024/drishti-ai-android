package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.components.ClinicalHeader
import com.example.ui.components.ClinicianReviewDialog
import com.example.ui.components.RecaptureGuidanceDialog
import com.example.ui.screens.*
import com.example.ui.theme.DrishtiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrishtiTheme(darkTheme = false) {
                DrishtiMainApp()
            }
        }
    }
}

@Composable
fun DrishtiMainApp(
    viewModel: DrishtiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allScreenings by viewModel.allScreenings.collectAsStateWithLifecycle()

    // Handle Android system back gesture navigation
    BackHandler(enabled = uiState.currentStep != ScreenStep.CAPTURE) {
        when (uiState.currentStep) {
            ScreenStep.QUALITY_CHECK -> viewModel.setStep(ScreenStep.CAPTURE)
            ScreenStep.ANALYSIS_RUNNING -> viewModel.setStep(ScreenStep.QUALITY_CHECK)
            ScreenStep.RESULTS -> viewModel.setStep(ScreenStep.CAPTURE)
            ScreenStep.HISTORY -> viewModel.setStep(ScreenStep.CAPTURE)
            ScreenStep.GUIDELINES -> viewModel.setStep(ScreenStep.CAPTURE)
            ScreenStep.CAPTURE -> { /* Root screen */ }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            if (uiState.currentStep != ScreenStep.ANALYSIS_RUNNING) {
                ClinicalHeader(
                    onInfoClick = { viewModel.setStep(ScreenStep.GUIDELINES) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentStep) {
                ScreenStep.CAPTURE -> {
                    CaptureScreen(
                        patientInfo = uiState.patientInfo,
                        onPatientInfoChange = { name, age, eye, dmYears ->
                            viewModel.updatePatientInfo(name, age, eye, dmYears)
                        },
                        onImageSelected = { bitmap, uri ->
                            viewModel.onImageCaptured(bitmap, uri)
                        },
                        onViewHistory = { viewModel.setStep(ScreenStep.HISTORY) },
                        onViewGuidelines = { viewModel.setStep(ScreenStep.GUIDELINES) }
                    )
                }

                ScreenStep.QUALITY_CHECK -> {
                    QualityCheckScreen(
                        bitmap = uiState.capturedBitmap,
                        qualityResult = uiState.qualityResult,
                        patientInfo = uiState.patientInfo,
                        onProceed = { viewModel.proceedToAnalysis() },
                        onRetake = { viewModel.retakeImage() },
                        onOpenGuidance = { viewModel.showRecaptureDialog(true) }
                    )
                }

                ScreenStep.ANALYSIS_RUNNING -> {
                    AnalysisLoadingScreen(
                        bitmap = uiState.capturedBitmap,
                        progressText = uiState.analysisProgressText,
                        progressPercent = uiState.analysisProgressPercent
                    )
                }

                ScreenStep.RESULTS -> {
                    uiState.analysisResult?.let { result ->
                        ResultsScreen(
                            analysisResult = result,
                            originalBitmap = uiState.capturedBitmap,
                            patientInfo = uiState.patientInfo,
                            gradCamOpacity = uiState.gradCamOpacity,
                            gradCamDisplayMode = uiState.gradCamDisplayMode,
                            showLesionMarkers = uiState.showLesionMarkers,
                            clinicianReview = uiState.clinicianReview,
                            onOpacityChange = { viewModel.setGradCamOpacity(it) },
                            onDisplayModeChange = { viewModel.setGradCamDisplayMode(it) },
                            onToggleMarkers = { viewModel.toggleLesionMarkers() },
                            onOpenClinicianReview = { viewModel.showClinicianReviewDialog(true) },
                            onNewScreening = { viewModel.retakeImage() },
                            onViewHistory = { viewModel.setStep(ScreenStep.HISTORY) }
                        )
                    }
                }

                ScreenStep.HISTORY -> {
                    HistoryScreen(
                        screenings = allScreenings,
                        onBack = { viewModel.setStep(ScreenStep.CAPTURE) },
                        onDeleteScreening = { viewModel.deleteScreening(it) }
                    )
                }

                ScreenStep.GUIDELINES -> {
                    GuidelinesScreen(
                        onBack = { viewModel.setStep(ScreenStep.CAPTURE) }
                    )
                }
            }

            // Clinician Review & Sign-Off Modal Dialog
            if (uiState.isClinicianDialogOpen && uiState.analysisResult != null) {
                ClinicianReviewDialog(
                    initialGrade = uiState.analysisResult!!.predictedGrade,
                    onDismiss = { viewModel.showClinicianReviewDialog(false) },
                    onSubmitReview = { name, license, agreed, overrideGrade, notes ->
                        viewModel.submitClinicianReview(name, license, agreed, overrideGrade, notes)
                    }
                )
            }

            // Recapture & Ophthalmic Imaging Guidance Modal Dialog
            if (uiState.isRecaptureDialogOpen) {
                RecaptureGuidanceDialog(
                    onDismiss = { viewModel.showRecaptureDialog(false) },
                    onRetakeNow = {
                        viewModel.showRecaptureDialog(false)
                        viewModel.retakeImage()
                    }
                )
            }
        }
    }
}
