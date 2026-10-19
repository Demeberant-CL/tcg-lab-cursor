package com.tcglab.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import com.tcglab.app.data.ThemeMode

private val LightScheme = lightColorScheme(
    primary = LightLabPalette.primary,
    onPrimary = LightLabPalette.onPrimary,
    secondary = LightLabPalette.inkMuted,
    onSecondary = LightLabPalette.onPrimary,
    background = LightLabPalette.background,
    onBackground = LightLabPalette.ink,
    surface = LightLabPalette.surfaceRaised,
    onSurface = LightLabPalette.ink,
    surfaceVariant = LightLabPalette.surface,
    onSurfaceVariant = LightLabPalette.inkMuted,
    outline = LightLabPalette.outline,
)

private val DarkScheme = darkColorScheme(
    primary = DarkLabPalette.primary,
    onPrimary = DarkLabPalette.onPrimary,
    secondary = DarkLabPalette.inkMuted,
    onSecondary = DarkLabPalette.onPrimary,
    background = DarkLabPalette.background,
    onBackground = DarkLabPalette.ink,
    surface = DarkLabPalette.surfaceRaised,
    onSurface = DarkLabPalette.ink,
    surfaceVariant = DarkLabPalette.surface,
    onSurfaceVariant = DarkLabPalette.inkMuted,
    outline = DarkLabPalette.outline,
)

val LabCardShape = RoundedCornerShape(20.dp)
val LabSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

object LabTheme {
    val colors: LabPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalLabPalette.current
}

@Composable
fun TcgLabTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val palette = if (dark) DarkLabPalette else LightLabPalette
    CompositionLocalProvider(LocalLabPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = LabTypography,
            content = content,
        )
    }
}
