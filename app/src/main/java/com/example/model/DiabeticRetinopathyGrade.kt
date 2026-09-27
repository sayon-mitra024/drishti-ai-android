package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * International Clinical Diabetic Retinopathy (ICDR) Disease Severity Scale
 */
enum class DiabeticRetinopathyGrade(
    val grade: Int,
    val title: String,
    val shortName: String,
    val description: String,
    val clinicalFindings: List<String>,
    val referralRecommendation: String,
    val isReferable: Boolean,
    val followUpWindow: String,
    val severityColor: Color
) {
    GRADE_0(
        grade = 0,
        title = "No Apparent DR",
        shortName = "Grade 0 (Normal)",
        description = "No retinal abnormalities detected associated with diabetic retinopathy.",
        clinicalFindings = listOf("Clear optic disc margin", "Normal vascular caliber", "Foveal avascular zone intact"),
        referralRecommendation = "Routine annual rescreening. Maintain tight glycemic and blood pressure control.",
        isReferable = false,
        followUpWindow = "Rescreen in 12 months",
        severityColor = Grade0Color
    ),
    GRADE_1(
        grade = 1,
        title = "Mild Non-Proliferative DR",
        shortName = "Grade 1 (Mild NPDR)",
        description = "Early stage characterized exclusively by microaneurysms.",
        clinicalFindings = listOf("Isolated microaneurysms", "No hemorrhages or exudates", "Macula unaffected"),
        referralRecommendation = "Schedule routine dilated follow-up. Reinforce systemic glycemic monitoring with primary care physician.",
        isReferable = false,
        followUpWindow = "Rescreen in 6–12 months",
        severityColor = Grade1Color
    ),
    GRADE_2(
        grade = 2,
        title = "Moderate Non-Proliferative DR",
        shortName = "Grade 2 (Moderate NPDR)",
        description = "More than just microaneurysms but less than Severe NPDR.",
        clinicalFindings = listOf("Multiple blot hemorrhages", "Hard exudates (lipid deposits)", "Cotton wool spots (nerve fiber infarcts)"),
        referralRecommendation = "Refer to comprehensive ophthalmologist for dilated fundus examination and OCT staging.",
        isReferable = true,
        followUpWindow = "Referral within 4–6 weeks",
        severityColor = Grade2Color
    ),
    GRADE_3(
        grade = 3,
        title = "Severe Non-Proliferative DR",
        shortName = "Grade 3 (Severe NPDR)",
        description = "Pre-proliferative state fulfilling the 4:2:1 international grading rule.",
        clinicalFindings = listOf("Severe hemorrhages in 4 quadrants", "Definite venous beading in 2+ quadrants", "Intraretinal microvascular abnormalities (IRMA) in 1+ quadrant"),
        referralRecommendation = "High risk of rapid progression to PDR. Urgent ophthalmology consultation required for potential anti-VEGF or PRP laser therapy.",
        isReferable = true,
        followUpWindow = "Referral within 1–2 weeks",
        severityColor = Grade3Color
    ),
    GRADE_4(
        grade = 4,
        title = "Proliferative DR",
        shortName = "Grade 4 (PDR)",
        description = "Advanced stage with active pathological neovascularization.",
        clinicalFindings = listOf("Neovascularization of the disc (NVD) or elsewhere (NVE)", "Preretinal or vitreous hemorrhage", "Fibrovascular proliferation"),
        referralRecommendation = "CRITICAL: Immediate vitreoretinal specialist referral. Urgent intervention (panretinal photocoagulation / anti-VEGF) to prevent severe vision loss.",
        isReferable = true,
        followUpWindow = "Immediate evaluation (< 48–72 hours)",
        severityColor = Grade4Color
    );

    companion object {
        fun fromGrade(grade: Int): DiabeticRetinopathyGrade {
            return entries.find { it.grade == grade } ?: GRADE_0
        }
    }
}
