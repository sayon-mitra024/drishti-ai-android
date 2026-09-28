package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoSunkWell
import com.example.ui.theme.*

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
            .padding(horizontal = 18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("about_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoInk
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "About Drishti AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
                Text(
                    text = "System specs, offline AI model & clinical ethics",
                    fontSize = 12.sp,
                    color = NeoInkSoft
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Model Architecture Card
        NeoCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inference Architecture",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AboutMetricRow("Model", "EfficientNet-B0 (drishti_model.onnx)")
            AboutMetricRow("Runtime", "ONNX Runtime Android (Float32)")
            AboutMetricRow("Input Resolution", "224 × 224 px (Bilinear)")
            AboutMetricRow("ICDR Classes", "5 Classes (Grade 0 to 4)")
            AboutMetricRow("Explainability", "Mathematical CAM (1280-channel layer)")
            AboutMetricRow("Weights Format", "Pre-extracted Float32 binary (25.6 KB)")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy & Local Execution
        NeoCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zero-Cloud Offline Privacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeoInk
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AboutMetricRow("Network Requirement", "100% Offline (Airplane mode ready)")
            AboutMetricRow("Cloud Telemetry", "Disabled (No images transmitted)")
            AboutMetricRow("Local Storage", "Room SQLite database on-device")
            AboutMetricRow("Report Verification", "Local HMAC-SHA256 QR signature")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Regulatory & Clinical Prototype Disclaimer
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DrishtiRoseSubtle,
            borderColor = DrishtiRoseBorder
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = DrishtiBurgundy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clinical Research Prototype",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DrishtiBurgundy
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Drishti AI is developed as an AI-assisted screening decision support system. It is not an FDA/CE-cleared standalone diagnostic device. All screening recommendations, grades, and Class Activation Maps must be reviewed and signed off by a qualified ophthalmologist, optometrist, or trained retinal screening specialist.",
                fontSize = 12.sp,
                color = NeoInk,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AboutMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = NeoInkSoft)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeoInk)
    }
}
