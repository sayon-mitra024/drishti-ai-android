package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.*

@Composable
fun ClinicalHeader(
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {}
) {
    Surface(
        color = NeoSurface,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, ambientColor = NeoShadowDark, spotColor = NeoShadowDark)
            .border(width = 1.dp, color = NeoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space16, vertical = Space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Official Drishti Eye Icon Mark
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFCF9F8))
                            .border(1.dp, Color(0xFFE5E2E1), RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.drishti_icon_official),
                            contentDescription = "Drishti AI Official Icon",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DRISHTI ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = DrishtiPrimary,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            text = "RETINAL SCREENING • RESEARCH PROTOTYPE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Research Badge & Info button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = DrishtiPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DrishtiPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DrishtiPrimary)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ON-DEVICE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DrishtiPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.testTag("clinical_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Clinical Guidelines & Information",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
