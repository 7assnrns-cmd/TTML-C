package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = AppleMusicRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A0E18),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = AppleMusicCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003644),
    onSecondaryContainer = Color(0xFFBCEBFF),
    tertiary = AppleMusicPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF381559),
    onTertiaryContainer = Color(0xFFF2DAFF),
    background = StudioDarkBg,
    onBackground = TextWhite,
    surface = StudioDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = Color(0xFF333B5C),
    outlineVariant = Color(0xFF22283E)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
