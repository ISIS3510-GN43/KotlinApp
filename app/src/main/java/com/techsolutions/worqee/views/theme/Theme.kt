package com.techsolutions.worqee.views.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WorqeeColorScheme = lightColorScheme(
    primary = PrimaryActionBlue,
    onPrimary = Night,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,

    secondary = CaribbeanCurrent,
    onSecondary = AntiFlashWhite,

    tertiary = MidnightGreen,
    onTertiary = AntiFlashWhite
)

@Composable
fun WorqeeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WorqeeColorScheme,
        typography = AppTypography, 
        content = content
    )
}