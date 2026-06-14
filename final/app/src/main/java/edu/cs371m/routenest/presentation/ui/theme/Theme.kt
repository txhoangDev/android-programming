package edu.cs371m.routenest.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    background = white,
    primary = blue200,
    secondary = blue500,
    tertiary = blue700,
    onPrimary = white,
    onSecondary = white,
    onTertiary = white,
    onBackground = blue900,
    onSurface = blue900,
    errorContainer = blue900,
)

@Composable
fun RouteNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = LightTypography,
        content = content
    )
}