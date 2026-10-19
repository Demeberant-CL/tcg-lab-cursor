package com.tcglab.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.R
import com.tcglab.app.folio.ENERGIES
import com.tcglab.app.ui.theme.EnergyDarkness
import com.tcglab.app.ui.theme.EnergyFighting
import com.tcglab.app.ui.theme.EnergyFire
import com.tcglab.app.ui.theme.EnergyGrass
import com.tcglab.app.ui.theme.EnergyLightning
import com.tcglab.app.ui.theme.EnergyMetal
import com.tcglab.app.ui.theme.EnergyPsychic
import com.tcglab.app.ui.theme.EnergyWater
import com.tcglab.app.ui.theme.LabCardShape
import com.tcglab.app.ui.theme.LabTheme

fun energyColor(id: Int): Color = when (id) {
    1 -> EnergyGrass
    2 -> EnergyFire
    3 -> EnergyWater
    4 -> EnergyLightning
    5 -> EnergyPsychic
    6 -> EnergyFighting
    7 -> EnergyDarkness
    8 -> EnergyMetal
    else -> Color(0xFF5A6B7A)
}

fun elementColor(element: String): Color = when (element.lowercase()) {
    "grass" -> EnergyGrass
    "fire" -> EnergyFire
    "water" -> EnergyWater
    "lightning" -> EnergyLightning
    "psychic" -> EnergyPsychic
    "fighting" -> EnergyFighting
    "darkness" -> EnergyDarkness
    "metal" -> EnergyMetal
    else -> Color(0xFF5A6B7A)
}

@Composable
fun LabSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LabTheme.colors
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        ),
        shape = LabCardShape,
        color = colors.surfaceRaised,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        content()
    }
}

@Composable
fun LabPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = LabTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.outline,
            disabledContentColor = colors.inkMuted,
        ),
    ) {
        Text(text)
    }
}

@Composable
fun LabTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = modifier.height(48.dp)) {
        Text(text, color = LabTheme.colors.ink)
    }
}

@Composable
fun EnergyDot(id: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(energyColor(id)),
    )
}

@Composable
fun ElementDot(element: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(elementColor(element)),
    )
}

@Composable
fun ThinProgress(owned: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = LabTheme.colors
    val fraction = if (total <= 0) 0f else (owned.toFloat() / total).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.progressTrack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(4.dp)
                .background(colors.progressFill),
        )
    }
}

@Composable
fun BrandHeader(title: String, modifier: Modifier = Modifier) {
    val colors = LabTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.brand),
            color = colors.inkMuted,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
fun EnergyRow(energies: List<Int>, modifier: Modifier = Modifier) {
    val colors = LabTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        energies.forEach { id ->
            val label = ENERGIES.find { it.id == id }?.es ?: id.toString()
            Row(verticalAlignment = Alignment.CenterVertically) {
                EnergyDot(id)
                Spacer(Modifier.width(4.dp))
                Text(label, color = colors.inkMuted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
