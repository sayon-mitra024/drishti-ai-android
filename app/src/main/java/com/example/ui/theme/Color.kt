package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Official Drishti AI Palette — Extracted from drishti.sayonedu.in
// Sophisticated Red + White Primary with Deep Obsidian Dark Glass
// =========================================================================

// Primary Brand Colors (Signature Drishti Crimson Red)
val DrishtiPrimary = Color(0xFF6B0119)              // Official Website Primary Crimson
val DrishtiOnPrimary = Color(0xFFFFFFFF)
val DrishtiPrimaryContainer = Color(0xFF8B1E2D)     // Vibrant Deep Crimson Container
val DrishtiOnPrimaryContainer = Color(0xFFFFDADA)   // Soft Rose White

// Secondary & Accent Red Highlights
val DrishtiSecondary = Color(0xFFAE2D40)            // Clinical Rose-Red Accent
val DrishtiOnSecondary = Color(0xFFFFFFFF)
val DrishtiSecondaryContainer = Color(0xFFFF6978)
val DrishtiOnSecondaryContainer = Color(0xFFFFFFFF)
val DrishtiSecondaryFixed = Color(0xFFFFDADA)

// Tertiary (Neutral Clinical Slate for metadata)
val DrishtiTertiary = Color(0xFF574142)
val DrishtiOnTertiary = Color(0xFFFFFFFF)

// Severity Badges (Extracted directly from drishti.sayonedu.in CSS tokens)
val Grade0Color = Color(0xFF1B7F52)                 // No DR - Clinical Emerald Green
val Grade0Bg = Color(0xFFEDF7F2)
val Grade1Color = Color(0xFF2B4C7E)                 // Mild DR - Clinical Steel Blue
val Grade1Bg = Color(0xFFEEF6F9)
val Grade2Color = Color(0xFFA36B00)                 // Moderate DR - Amber Ochre
val Grade2Bg = Color(0xFFFEF8EC)
val Grade3Color = Color(0xFFAE2D40)                 // Severe DR - Vivid Crimson
val Grade3Bg = Color(0xFFFDF2F3)
val Grade4Color = Color(0xFF6B0119)                 // Proliferative DR - Deep Blood Crimson
val Grade4Bg = Color(0xFFFDF2F3)

// Deep Clinical Dark Glassmorphism Surfaces
val DrishtiDarkBackground = Color(0xFF090607)       // Deep Obsidian Dark Surface
val DrishtiDarkSurface = Color(0xFF130D0F)          // Level 1 Dark Glass Surface
val DrishtiDarkSurfaceVariant = Color(0xFF1C1316)   // Level 2 Elevated Glass
val DrishtiDarkBorder = Color(0x33DEBFBF)           // Ultra-fine glass border (20% rose-white)
val DrishtiDarkBorderActive = Color(0x668B1E2D)     // Active crimson border (40% crimson)

val DrishtiDarkTextPrimary = Color(0xFFFBF8F8)      // Crisp High-Contrast White
val DrishtiDarkTextSecondary = Color(0xFFC7B6B8)    // Muted Rose-Slate
val DrishtiDarkTextMuted = Color(0xFF8B7173)        // Subdued Captions

// Light Theme Clinical Surfaces (matching web #FCF9F8)
val DrishtiLightBackground = Color(0xFFFCF9F8)      // Official Web Background
val DrishtiLightSurface = Color(0xFFFFFFFF)
val DrishtiLightSurfaceVariant = Color(0xFFF0EDEC)  // Official Web Surface Container
val DrishtiLightBorder = Color(0xFFE5E2E1)
val DrishtiLightTextPrimary = Color(0xFF1C1B1B)     // Official Web On-Surface
val DrishtiLightTextSecondary = Color(0xFF574142)   // Official Web On-Surface-Variant

// Glassmorphism Brushes & Overlays
val DrishtiGlassSurface = Color(0xE6140D10)         // 90% Translucent Dark Glass
val DrishtiGlassSurfaceLight = Color(0xF2FFFFFF)    // 95% Translucent White Glass
val DrishtiGlassHighlight = Color(0x1FFFFFFF)       // Specular Top Edge
val DrishtiRedGlow = Color(0x336B0119)              // Soft Retinal Crimson Glow

val DrishtiCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1C1215),
        Color(0xFF120B0D)
    )
)

val DrishtiHeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF280B10),
        Color(0xFF0F080A),
        Color(0xFF090607)
    )
)

val DrishtiRedAccentGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6B0119),
        Color(0xFF8B1E2D),
        Color(0xFFAE2D40)
    )
)
