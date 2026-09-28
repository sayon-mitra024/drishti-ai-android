package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Drishti AI — Unified Spacing & Dimension Design System
 * Consistent standard grid: 4dp, 8dp, 16dp, 24dp (with 12dp & 32dp accents).
 */
object DrishtiDimens {
    val Space4: Dp = 4.dp
    val Space8: Dp = 8.dp
    val Space12: Dp = 12.dp
    val Space16: Dp = 16.dp
    val Space24: Dp = 24.dp
    val Space32: Dp = 32.dp

    // Semantic Spacing
    val ScreenPaddingHorizontal: Dp = 16.dp
    val ScreenPaddingVertical: Dp = 16.dp
    val CardPadding: Dp = 16.dp
    val CardPaddingCompact: Dp = 12.dp
    val SectionSpacing: Dp = 16.dp
    val ItemSpacing: Dp = 8.dp
    val TightSpacing: Dp = 4.dp
    val WideSpacing: Dp = 24.dp

    // Element Dimensions
    val CardCornerRadius: Dp = 16.dp
    val ButtonHeight: Dp = 50.dp
    val IconSizeSmall: Dp = 16.dp
    val IconSizeMedium: Dp = 20.dp
    val IconSizeLarge: Dp = 24.dp
}

// Top-level aliases for direct, clean usage across Composables
val Space4: Dp = DrishtiDimens.Space4
val Space8: Dp = DrishtiDimens.Space8
val Space12: Dp = DrishtiDimens.Space12
val Space16: Dp = DrishtiDimens.Space16
val Space24: Dp = DrishtiDimens.Space24
val Space32: Dp = DrishtiDimens.Space32
val ScreenPadding: Dp = DrishtiDimens.ScreenPaddingHorizontal
val CardPadding: Dp = DrishtiDimens.CardPadding
val SectionSpacing: Dp = DrishtiDimens.SectionSpacing
