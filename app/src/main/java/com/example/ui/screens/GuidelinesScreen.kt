package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoSunkWell
import com.example.ui.theme.*

@Composable
fun GuidelinesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("guidelines_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoInk
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Clinical Guidelines",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
                Text(
                    text = "Imaging protocols & ophthalmic workflows",
                    fontSize = 12.sp,
                    color = NeoInkSoft
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Before Capture
        GuidelineSectionCard(
            title = "1. Before Image Capture",
            icon = Icons.Default.Checklist
        ) {
            GuidelineBullet("Patient Positioning: Seat patient comfortably with chin resting squarely and forehead pressed against headrest.")
            GuidelineBullet("Pupillary State: Ensure adequate physiological pupil dilation (minimum 3.5mm–4.0mm) or low ambient room illumination.")
            GuidelineBullet("Ocular Surface: Instruct patient to blink gently once or twice before capture to stabilize tear film.")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 2: Image Positioning
        GuidelineSectionCard(
            title = "2. Optical & Field Positioning",
            icon = Icons.Default.CenterFocusStrong
        ) {
            GuidelineBullet("Standard 45° Retinal Field: Center optic disc approximately 2 disc diameters nasal to the macula.")
            GuidelineBullet("Macula Centering: Ensure foveal reflex is clearly visible and centered within the optical guide circle.")
            GuidelineBullet("Arcade Coverage: Superior and inferior major vascular arcades should be fully encompassed within frame.")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 3: Focus & Illumination Quality
        GuidelineSectionCard(
            title = "3. Focus & Illumination",
            icon = Icons.Default.WbSunny
        ) {
            GuidelineBullet("Focus Calibration: Retinal nerve fiber layer and fine terminal capillaries must be sharply defined.")
            GuidelineBullet("Illumination Uniformity: Avoid excessive central glare or crescent-shaped corneal edge reflections.")
            GuidelineBullet("Exposure: Balanced exposure prevents blowout in peripapillary region or underexposure in periphery.")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 4: Common Imaging Problems
        GuidelineSectionCard(
            title = "4. Common Problems & Retake Conditions",
            icon = Icons.Default.WarningAmber
        ) {
            GuidelineBullet("Corneal Glare / Ring Reflex: Re-angle camera lens slightly off-axis to avoid specular reflections.")
            GuidelineBullet("Motion Blur / Saccades: Remind patient to fixate steadily on the internal fixation target.")
            GuidelineBullet("Eyelash / Lid Artifacts: Gently assist upper lid elevation if prominent ptosis or dermatochalasis exists.")
            GuidelineBullet("Cataractous Haziness: Note nuclear sclerosis in clinical review notes when media opacity is present.")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 5: Clinical Workflow
        GuidelineSectionCard(
            title = "5. Clinical Screening Workflow",
            icon = Icons.Default.Timeline
        ) {
            GuidelineBullet("Stage 1 — Capture: Acquire quality fundus photograph on-device.")
            GuidelineBullet("Stage 2 — Validation: Algorithmic assessment of focus, illumination, and coverage.")
            GuidelineBullet("Stage 3 — Inference: EfficientNet-B0 grades 5 ICDR classes locally.")
            GuidelineBullet("Stage 4 — Explainability: Class Activation Map (CAM) identifies saliency regions.")
            GuidelineBullet("Stage 5 — Clinician Sign-Off: Professional oversight and referral recommendation.")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 6: Research Prototype Disclaimer
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DrishtiRoseSubtle,
            borderColor = DrishtiRoseBorder
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MedicalInformation,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Regulatory & Clinical Disclaimer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DrishtiBurgundy
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Drishti AI is a research decision-support prototype. It does not replace a comprehensive dilated eye examination performed by a qualified ophthalmologist. Referable findings require formal in-person clinical follow-up.",
                fontSize = 11.5.sp,
                color = NeoInk,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GuidelineSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DrishtiBurgundy,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeoInk
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

@Composable
private fun GuidelineBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(DrishtiBurgundy)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = NeoInkSoft,
            lineHeight = 17.sp
        )
    }
}
