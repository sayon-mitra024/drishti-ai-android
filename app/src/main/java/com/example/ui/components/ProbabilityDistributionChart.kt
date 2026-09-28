package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.theme.*

@Composable
fun ProbabilityDistributionChart(
    probabilities: Map<DiabeticRetinopathyGrade, Float>,
    predictedGrade: DiabeticRetinopathyGrade,
    modifier: Modifier = Modifier
) {
    NeoCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5-Class Probability Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeoInk
            )
            Text(
                text = "ICDR Standard",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NeoInkFaint
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        DiabeticRetinopathyGrade.entries.forEach { grade ->
            val prob = probabilities[grade] ?: 0f
            val isSelected = grade == predictedGrade
            val animatedProgress by animateFloatAsState(targetValue = prob, label = "probProgress")

            val gradeColor = when (grade) {
                DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
                DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
                DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
                DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
                DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) gradeColor.copy(alpha = 0.08f) else Color.Transparent,
                border = if (isSelected) BorderStroke(1.dp, gradeColor.copy(alpha = 0.35f)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(gradeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = grade.shortName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) gradeColor else NeoInk
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = gradeColor
                                ) {
                                    Text(
                                        text = "AI PREDICTED",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${(prob * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) gradeColor else NeoInkSoft
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = gradeColor,
                        trackColor = NeoSurfaceSunk
                    )
                }
            }
        }
    }
}
