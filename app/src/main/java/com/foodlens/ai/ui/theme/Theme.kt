package com.foodlens.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FoodLensDarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = BackgroundDark,

    secondary = GoldLight,
    onSecondary = BackgroundDark,

    tertiary = GoldDark,
    onTertiary = TextPrimary,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondary,

    outline = DividerDark
)

@Composable
fun FoodLensAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FoodLensDarkColorScheme,
        typography = Typography,
        content = content
    )
}