package com.tcglab.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LabPalette(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val inkMuted: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val progressTrack: Color,
    val progressFill: Color,
    val emptySlot: Color,
)

val LightLabPalette = LabPalette(
    background = Color(0xFFF3F0E8),
    surface = Color(0xFFF9F7F1),
    surfaceRaised = Color(0xFFFFFEFA),
    ink = Color(0xFF172536),
    inkMuted = Color(0xFF667586),
    outline = Color(0xFFE3DED2),
    primary = Color(0xFF172536),
    onPrimary = Color(0xFFFFFEFA),
    progressTrack = Color(0xFFE9E2D4),
    progressFill = Color(0xFFD99A27),
    emptySlot = Color(0xFFE5DFD3),
)

val DarkLabPalette = LabPalette(
    background = Color(0xFF101820),
    surface = Color(0xFF18232E),
    surfaceRaised = Color(0xFF202D38),
    ink = Color(0xFFF0EEE7),
    inkMuted = Color(0xFF9DAAB5),
    outline = Color(0xFF34424E),
    primary = Color(0xFFFFC857),
    onPrimary = Color(0xFF172536),
    progressTrack = Color(0xFF33414B),
    progressFill = Color(0xFFFFC857),
    emptySlot = Color(0xFF202D38),
)

val LocalLabPalette = staticCompositionLocalOf { LightLabPalette }

/** Compatibilidad con pantallas existentes; preferir LocalLabPalette en código nuevo. */
val LabBackground get() = LightLabPalette.background
val LabSurface get() = LightLabPalette.surface
val LabSurfaceRaised get() = LightLabPalette.surfaceRaised
val LabInk get() = LightLabPalette.ink
val LabInkMuted get() = LightLabPalette.inkMuted
val LabOutline get() = LightLabPalette.outline
val LabPrimary get() = LightLabPalette.primary
val LabOnPrimary get() = LightLabPalette.onPrimary
val LabProgressTrack get() = LightLabPalette.progressTrack
val LabProgressFill get() = LightLabPalette.progressFill

val EnergyGrass = Color(0xFF4CAF50)
val EnergyFire = Color(0xFFE53935)
val EnergyWater = Color(0xFF1E88E5)
val EnergyLightning = Color(0xFFF9A825)
val EnergyPsychic = Color(0xFF8E24AA)
val EnergyFighting = Color(0xFFD84315)
val EnergyDarkness = Color(0xFF455A64)
val EnergyMetal = Color(0xFF78909C)

