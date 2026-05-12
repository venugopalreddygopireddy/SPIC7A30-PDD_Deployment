package com.cortisense.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784), // Brighter Sage/Green for Dark Mode
    secondary = Color(0xFF90CAF9), // Brighter Blue for Dark Mode
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF0F101A), // Even darker background for better contrast
    surface = Color(0xFF1E1F2E),
    onPrimary = Color(0xFF0F101A),
    onSecondary = Color(0xFF0F101A),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFFBBBBBB),
    outlineVariant = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    secondary = DustyBlue,
    tertiary = SoftSage,
    background = WarmCream,
    surface = Color.White,
    onPrimary = TextDark,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun CortiSenseTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}