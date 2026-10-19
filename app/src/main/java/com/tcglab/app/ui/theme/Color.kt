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
    background = Color(0xFFE7EEF3),
    surface = Color(0xFFF5F9FC),
    surfaceRaised = Color(0xFFFFFFFF),
    ink = Color(0xFF1C2833),
    inkMuted = Color(0xFF5A6B7A),
    outline = Color(0xFFD5E0E8),
    primary = Color(0xFF1C2833),
    onPrimary = Color(0xFFF5F9FC),
    progressTrack = Color(0xFFD8E4ED),
    progressFill = Color(0xFF1C2833),
    emptySlot = Color(0xFFD5DEE6),
)

val DarkLabPalette = LabPalette(
    background = Color(0xFF121820),
    surface = Color(0xFF1A222C),
    surfaceRaised = Color(0xFF232D3A),
    ink = Color(0xFFE8EEF4),
    inkMuted = Color(0xFF9AABBC),
    outline = Color(0xFF3A4654),
    primary = Color(0xFFD7E3EE),
    onPrimary = Color(0xFF121820),
    progressTrack = Color(0xFF2A3542),
    progressFill = Color(0xFF8FB4D4),
    emptySlot = Color(0xFF1E2833),
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
