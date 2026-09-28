package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiabeticRetinopathyGrade
import com.example.ui.ScreenStep
import com.example.ui.theme.*

/**
 * Raised neo-skeuomorphic card surface.
 * Soft white surface with subtle border and dual shadow feeling.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = NeoSurface,
    borderColor: Color = NeoBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(shapeRadius)
    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = NeoShadowDark,
                spotColor = NeoShadowDark
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Sunk/recessed well container.
 * Used for input fields, progress indicators, or inactive recessed areas.
 */
@Composable
fun NeoSunkWell(
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    backgroundColor: Color = NeoSurfaceSunk,
    borderColor: Color = NeoBorder,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(shapeRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Tactile Primary Button.
 * Rich burgundy gradient with bottom bevel depth.
 */
@Composable
fun NeoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    backgroundColor: Color = DrishtiBurgundy,
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(14.dp)

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        color = if (enabled) {
            if (isPressed) DrishtiBurgundyInk else backgroundColor
        } else {
            NeoBorderStrong
        },
        shadowElevation = if (enabled && !isPressed) 4.dp else 1.dp,
        modifier = modifier
            .height(height)
            .border(
                1.dp,
                if (enabled) DrishtiBurgundyInk.copy(alpha = 0.4f) else NeoBorder,
                shape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else NeoInkFaint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else NeoInkFaint,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Tactile Secondary Button.
 * White raised surface with subtle border and burgundy typography.
 */
@Composable
fun NeoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    contentColor: Color = DrishtiBurgundy
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(14.dp)

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        color = if (isPressed) NeoSurfaceSunk else NeoSurface,
        border = BorderStroke(1.dp, NeoBorderStrong),
        shadowElevation = if (isPressed) 0.dp else 2.dp,
        modifier = modifier.height(height)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else NeoInkFaint,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else NeoInkFaint,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }
    }
}

/**
 * Semantic DR Grade Badge pill.
 */
@Composable
fun SeverityPill(
    grade: DiabeticRetinopathyGrade,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val bg = when (grade) {
        DiabeticRetinopathyGrade.GRADE_0 -> Grade0Color
        DiabeticRetinopathyGrade.GRADE_1 -> Grade1Color
        DiabeticRetinopathyGrade.GRADE_2 -> Grade2Color
        DiabeticRetinopathyGrade.GRADE_3 -> Grade3Color
        DiabeticRetinopathyGrade.GRADE_4 -> Grade4Color
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        shadowElevation = 1.dp
    ) {
        Text(
            text = customText ?: grade.shortName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Clean Clinical Header.
 * Minimal, restrained, showing "DRISHTI AI", role/context, and "Offline ready" status.
 */
@Composable
fun NeoClinicalHeader(
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NeoSurface,
        border = BorderStroke(1.dp, NeoBorder),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(DrishtiBurgundy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Drishti AI",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DRISHTI AI",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = NeoInk,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DrishtiRoseSubtle,
                            border = BorderStroke(1.dp, DrishtiRoseBorder)
                        ) {
                            Text(
                                text = "PROTOTYPE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DrishtiBurgundy,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Clinical Retinal Screening System",
                        fontSize = 11.sp,
                        color = NeoInkSoft
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Offline status pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Grade0Bg,
                    border = BorderStroke(1.dp, Grade0Color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Grade0Color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Offline ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Grade0Color
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Guidelines & Info",
                        tint = NeoInkSoft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern Clinical Bottom Navigation Bar.
 * Clean, rounded, elevated subtly, off-white, thin border, soft shadow, burgundy active indicator.
 */
@Composable
fun DrishtiBottomNav(
    currentStep: ScreenStep,
    onNavigate: (ScreenStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NeoSurface,
        border = BorderStroke(1.dp, NeoBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.CameraAlt,
                label = "Screening",
                isSelected = currentStep == ScreenStep.CAPTURE || currentStep == ScreenStep.QUALITY_CHECK || currentStep == ScreenStep.ANALYSIS_RUNNING || currentStep == ScreenStep.RESULTS,
                onClick = { onNavigate(ScreenStep.CAPTURE) }
            )

            BottomNavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = currentStep == ScreenStep.HISTORY,
                onClick = { onNavigate(ScreenStep.HISTORY) }
            )

            BottomNavItem(
                icon = Icons.Default.MenuBook,
                label = "Guidelines",
                isSelected = currentStep == ScreenStep.GUIDELINES,
                onClick = { onNavigate(ScreenStep.GUIDELINES) }
            )

            BottomNavItem(
                icon = Icons.Default.Info,
                label = "About",
                isSelected = currentStep == ScreenStep.ABOUT,
                onClick = { onNavigate(ScreenStep.ABOUT) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = DrishtiBurgundy
    val inactiveColor = NeoInkFaint

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) DrishtiRoseSubtle else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, DrishtiRoseBorder) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else inactiveColor
            )
        }
    }
}
