package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3D2100),
    onPrimaryContainer = JarvisGold,
    secondary = JarvisCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00365A),
    onSecondaryContainer = JarvisCyan,
    tertiary = JarvisGreen,
    onTertiary = Color.Black,
    background = JarvisBlack,
    onBackground = JarvisTextPrimary,
    surface = JarvisPanelDark,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisPanelSecondary,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    outlineVariant = JarvisBorderGlow,
    error = JarvisDanger,
    onError = Color.White
)

val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECCF),
    onPrimaryContainer = Color(0xFF5A2B00),
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001B3E),
    tertiary = Color(0xFF00897B),
    onTertiary = Color.White,
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF0F1A28),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F1A28),
    surfaceVariant = Color(0xFFE5EDF5),
    onSurfaceVariant = Color(0xFF4A5D70),
    outline = Color(0xFFCBD6E2),
    outlineVariant = Color(0xFFB0C0D0),
    error = JarvisDanger,
    onError = Color.White
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = true, // Default to futuristic dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
