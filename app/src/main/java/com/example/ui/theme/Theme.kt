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

private val DarkColorScheme = darkColorScheme(
    primary = DrishtiPrimary,
    onPrimary = DrishtiOnPrimary,
    primaryContainer = DrishtiPrimaryContainer,
    onPrimaryContainer = DrishtiOnPrimaryContainer,
    secondary = DrishtiSecondary,
    onSecondary = DrishtiOnSecondary,
    secondaryContainer = DrishtiSecondaryContainer,
    onSecondaryContainer = DrishtiOnSecondaryContainer,
    tertiary = DrishtiTertiary,
    onTertiary = DrishtiOnTertiary,
    background = DrishtiDarkBackground,
    onBackground = DrishtiDarkTextPrimary,
    surface = DrishtiDarkSurface,
    onSurface = DrishtiDarkTextPrimary,
    surfaceVariant = DrishtiDarkSurfaceVariant,
    onSurfaceVariant = DrishtiDarkTextSecondary,
    outline = DrishtiDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DrishtiPrimary,
    onPrimary = DrishtiOnPrimary,
    primaryContainer = DrishtiOnPrimaryContainer,
    onPrimaryContainer = DrishtiPrimaryContainer,
    secondary = DrishtiSecondary,
    onSecondary = DrishtiOnSecondary,
    secondaryContainer = DrishtiOnSecondaryContainer,
    onSecondaryContainer = DrishtiSecondaryContainer,
    tertiary = DrishtiTertiary,
    onTertiary = DrishtiOnTertiary,
    background = DrishtiLightBackground,
    onBackground = DrishtiLightTextPrimary,
    surface = DrishtiLightSurface,
    onSurface = DrishtiLightTextPrimary,
    surfaceVariant = DrishtiLightSurfaceVariant,
    onSurfaceVariant = DrishtiLightTextSecondary,
    outline = DrishtiLightBorder
)

@Composable
fun DrishtiTheme(
    darkTheme: Boolean = false, // Default to clinical white theme matching drishti.sayonedu.in
    dynamicColor: Boolean = false, // Keep consistent clinical branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = DrishtiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
