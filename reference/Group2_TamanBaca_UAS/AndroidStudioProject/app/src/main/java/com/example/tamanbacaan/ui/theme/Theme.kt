package com.example.tamanbacaan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    secondary = OliveGreen,
    background = Cream,
    surface = Cream,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnLight,
    onSurface = OnLight
)

@Composable
fun TamanBacaanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}