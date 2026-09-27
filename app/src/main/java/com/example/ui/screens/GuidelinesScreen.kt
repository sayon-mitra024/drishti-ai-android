package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.theme.DrishtiDarkBorder
import com.example.ui.theme.DrishtiPrimary

@Composable
fun GuidelinesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090607))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("guidelines_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFCF9F8)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Clinical Guidelines & Model Specs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCF9F8)
                )
                Text(
                    text = "Drishti AI • drishti.sayonedu.in Documentation",
                    fontSize = 12.sp,
                    color = Color(0xFFC7B6B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Official Branding Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
            border = BorderStroke(1.dp, DrishtiDarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFCF9F8),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.drishti_icon_official),
                        contentDescription = "Official Drishti Icon",
                        modifier = Modifier
                            .size(44.dp)
                            .padding(6.dp)
                    )
                }
                Column {
                    Text(
                        text = "Drishti AI — Retinal Screening",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFCF9F8)
                    )
                    Text(
                        text = "AI-assisted screening prototype designed by Sayon Mitra",
                        fontSize = 11.sp,
                        color = Color(0xFFC7B6B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prominent Regulatory Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0E12)),
            border = BorderStroke(1.dp, DrishtiPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFFF8A95),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clinical Screening Tool Notice",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCF9F8),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Drishti AI is strictly designed as an automated screening support tool for triage and referral determination in primary healthcare centers and rural clinics. It is NOT a clinically validated standalone diagnostic device. All AI assessments must be reviewed and countersigned by a licensed ophthalmologist or reading center clinician before clinical intervention.",
                    fontSize = 11.5.sp,
                    color = Color(0xFFD4BFC4),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deployed Model Specifications (From drishti.sayonedu.in)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
            border = BorderStroke(1.dp, DrishtiDarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFFFF8A95),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deployed Model Specifications",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCF9F8),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                SpecRow("Architecture", "EfficientNet-B0")
                SpecRow("Task", "Diabetic Retinopathy Classification (5-Class)")
                SpecRow("Classes", "No DR · Mild · Moderate · Severe · Proliferative DR")
                SpecRow("Input Size", "224 × 224 Standardized Tensor")
                SpecRow("Explainability", "Grad-CAM (Class Activation Mapping)")
                SpecRow("Serving", "Autonomous Mobile Inference (Offline-capable)")
                SpecRow("Input Format", "Fundus Photography (JPG/PNG)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4:2:1 Rule for Severe NPDR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
            border = BorderStroke(1.dp, Color(0xFFAE2D40).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "The ICDR 4:2:1 Rule (Severe NPDR)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFFFFB4BA)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Severe Non-Proliferative Diabetic Retinopathy (Grade 3) is diagnosed when the fundus presents any ONE of the following three criteria without signs of proliferative neovascularization:",
                    fontSize = 11.5.sp,
                    color = Color(0xFFC7B6B8),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                RuleBullet("4", "Severe intraretinal hemorrhages and microaneurysms in all 4 quadrants")
                RuleBullet("2", "Definite venous beading in 2 or more quadrants")
                RuleBullet("1", "Prominent intraretinal microvascular abnormalities (IRMA) in 1 or more quadrants")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ICDR 5-Class Scale
        Text(
            text = "International Clinical Severity Scale",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFCF9F8)
        )
        Spacer(modifier = Modifier.height(10.dp))

        DiabeticRetinopathyGrade.entries.forEach { grade ->
            GradeDetailCard(grade = grade)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Known Limitations & Disclaimer (from website)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
            border = BorderStroke(1.dp, DrishtiDarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF8B7173),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Known Limitations",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCF9F8),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• The mobile inference pipeline operates entirely on-device for rural resilience without requiring internet connectivity.\n• Substandard pupil dilation, cataract haze, or motion artifacts degrade sensitivity.\n• Final interpretation must always be performed by a qualified ophthalmologist.",
                    fontSize = 11.5.sp,
                    color = Color(0xFFC7B6B8),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF8B7173))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFCF9F8))
    }
}

@Composable
private fun RuleBullet(num: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B1E2D)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = num, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 11.sp, color = Color(0xFFFCF9F8))
    }
}

@Composable
private fun GradeDetailCard(grade: DiabeticRetinopathyGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
        border = BorderStroke(1.dp, grade.severityColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(grade.severityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GRADE ${grade.grade}",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = grade.severityColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (grade.isReferable) Color(0xFF6B0119) else Color(0xFF1B7F52),
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (grade.isReferable) "REFERABLE" else "NON-REFERABLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = grade.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFCF9F8)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = grade.description,
                fontSize = 11.sp,
                color = Color(0xFFC7B6B8),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            grade.clinicalFindings.forEach { finding ->
                Row(
                    modifier = Modifier.padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = grade.severityColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = finding, fontSize = 11.sp, color = Color(0xFFE5E2E1))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1A1013),
                border = BorderStroke(1.dp, DrishtiDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recommendation: ${grade.referralRecommendation}",
                    fontSize = 10.5.sp,
                    color = Color(0xFFFFB4BA),
                    modifier = Modifier.padding(8.dp),
                    lineHeight = 14.sp
                )
            }
        }
    }
}
