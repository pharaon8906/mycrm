package com.caloriecam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF12805C)
private val GreenDark = Color(0xFF0E8FA8)
private val Background = Color(0xFFE9EDF3)
private val Surface = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = GreenDark,
    background = Background,
    surface = Surface
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FC79F),
    secondary = GreenDark
)

@Composable
fun CalorieCamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
