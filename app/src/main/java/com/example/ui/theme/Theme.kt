package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NeoClinicalColorScheme = lightColorScheme(
    primary = DrishtiBurgundy,
    onPrimary = Color.White,
    primaryContainer = DrishtiCrimson,
    onPrimaryContainer = Color.White,
    secondary = DrishtiCrimsonLight,
    onSecondary = Color.White,
    secondaryContainer = DrishtiRoseSubtle,
    onSecondaryContainer = DrishtiBurgundy,
    tertiary = DrishtiTertiary,
    onTertiary = Color.White,
    background = NeoBg,
    onBackground = NeoInk,
    surface = NeoSurface,
    onSurface = NeoInk,
    surfaceVariant = NeoSurfaceSunk,
    onSurfaceVariant = NeoInkSoft,
    outline = NeoBorder,
    outlineVariant = NeoBorderStrong
)

@Composable
fun DrishtiTheme(
    darkTheme: Boolean = false, // Clinical soft off-white theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NeoClinicalColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = DrishtiTheme(darkTheme = false, dynamicColor = false, content = content)
