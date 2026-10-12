package com.ravi.newfilemanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Soft pastel color palette
val SoftBlue = Color(0xFF7A9ACD)
val SoftSage = Color(0xFF7A9A7A)
val SoftPeach = Color(0xFFD9975A)
val SoftLavender = Color(0xFF9A7ACD)
val SoftRose = Color(0xFFD98A94)
val SoftMint = Color(0xFF6AB8A8)
val SoftButter = Color(0xFFD9B84A)
val SoftSky = Color(0xFF6AA8D9)

private val LightColors = lightColorScheme(
    primary = SoftBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EFFA),
    onPrimaryContainer = Color(0xFF2A3A55),
    secondary = SoftSage,
    background = Color(0xFFFAF8F5),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0EDE8),
    outline = Color(0xFFE0DCD6)
)

private val DarkColors = darkColorScheme(
    primary = SoftBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A3A55),
    onPrimaryContainer = Color(0xFFE8EFFA),
    secondary = SoftSage,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2A2A2A),
    outline = Color(0xFF3A3A3A)
)

@Composable
fun NewFileManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
