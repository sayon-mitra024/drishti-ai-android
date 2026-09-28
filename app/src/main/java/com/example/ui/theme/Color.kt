package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Drishti AI — Clinical Neo-Skeuomorphic Design System Palette
// Soft clinical off-white surfaces, rich burgundy primary, semantic severity
// =========================================================================

// Primary Brand Colors (Burgundy / Crimson)
val DrishtiBurgundy = Color(0xFF6B0119)          // Primary brand color
val DrishtiBurgundyInk = Color(0xFF4C0112)       // Deep bevel / active state
val DrishtiCrimson = Color(0xFF8B1E2D)           // Primary accent
val DrishtiCrimsonLight = Color(0xFFAE2D40)      // Highlight accent
val DrishtiRoseSubtle = Color(0xFFFDF2F3)        // Subtle rose container
val DrishtiRoseBorder = Color(0x408B1E2D)

// Backward-compatible aliases
val DrishtiPrimary = DrishtiBurgundy
val DrishtiOnPrimary = Color(0xFFFFFFFF)
val DrishtiPrimaryContainer = DrishtiCrimson
val DrishtiOnPrimaryContainer = Color(0xFFFFDADA)

val DrishtiSecondary = DrishtiCrimsonLight
val DrishtiOnSecondary = Color(0xFFFFFFFF)
val DrishtiSecondaryContainer = Color(0xFFFFEAEA)
val DrishtiOnSecondaryContainer = DrishtiBurgundy

val DrishtiTertiary = Color(0xFF6B5C58)
val DrishtiOnTertiary = Color(0xFFFFFFFF)

// Clinical Surfaces & Inks (Off-white / Warm White / Sunk wells)
val NeoBg = Color(0xFFFCF9F8)                   // Clinical warm off-white background
val NeoSurface = Color(0xFFFFFFFF)              // Raised white card surface
val NeoSurfaceSunk = Color(0xFFF3EEEC)          // Sunk input well / recessed field
val NeoBorder = Color(0xFFE9E1DE)               // Standard card border (1dp)
val NeoBorderStrong = Color(0xFFDAD0CC)         // Interactive border
val NeoInk = Color(0xFF241412)                  // High-contrast deep charcoal text
val NeoInkSoft = Color(0xFF6B5C58)              // Muted secondary text
val NeoInkFaint = Color(0xFFA6968F)             // Subdued labels / timestamps

// Shadow tones
val NeoShadowDark = Color(0x146B0119)           // Soft burgundy shadow
val NeoShadowLight = Color(0xFFFFFFFF)          // Crisp white top specular highlight

// Backward-compatible surface aliases
val DrishtiLightBackground = NeoBg
val DrishtiLightSurface = NeoSurface
val DrishtiLightSurfaceVariant = NeoSurfaceSunk
val DrishtiLightBorder = NeoBorder
val DrishtiLightTextPrimary = NeoInk
val DrishtiLightTextSecondary = NeoInkSoft

val DrishtiDarkBackground = NeoBg
val DrishtiDarkSurface = NeoSurface
val DrishtiDarkSurfaceVariant = NeoSurfaceSunk
val DrishtiDarkBorder = NeoBorder
val DrishtiDarkBorderActive = NeoBorderStrong
val DrishtiDarkTextPrimary = NeoInk
val DrishtiDarkTextSecondary = NeoInkSoft
val DrishtiDarkTextMuted = NeoInkFaint

// Semantic ICDR Severity Scale (Distinct hue family from brand)
val Grade0Color = Color(0xFF3C8558)             // No DR - Forest Green
val Grade0Bg = Color(0xFFEDF7F2)
val Grade1Color = Color(0xFFC98A2B)             // Mild DR - Warm Gold
val Grade1Bg = Color(0xFFFEF8EC)
val Grade2Color = Color(0xFFD9772E)             // Moderate DR - Amber Ochre
val Grade2Bg = Color(0xFFFEF3EB)
val Grade3Color = Color(0xFFC0392B)             // Severe DR - Vermilion Red
val Grade3Bg = Color(0xFFFDF2F3)
val Grade4Color = Color(0xFF7A1F2B)             // Proliferative DR - Deep Wine Blood
val Grade4Bg = Color(0xFFFDF2F3)

// Clinical Primary Button Gradient
val NeoPrimaryButtonGradient = Brush.verticalGradient(
    colors = listOf(
        DrishtiCrimsonLight,
        DrishtiBurgundy
    )
)

val NeoRaisedCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFCFBFB)
    )
)
