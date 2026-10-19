package com.tcglab.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.folio.Catalog
import com.tcglab.app.ui.collection.CollectionViewModel
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.ThinProgress
import com.tcglab.app.ui.theme.LabTheme

@Composable
fun HomeScreen(
    collectionVm: CollectionViewModel,
    onOpenCollection: () -> Unit,
    onOpenDeck: () -> Unit,
    onOpenStrategy: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by collectionVm.state.collectAsState()
    val colors = LabTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "TCG LAB",
                    color = colors.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Tu álbum y mazos de Pocket",
                    color = colors.inkMuted,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ajustes",
                    tint = colors.ink,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (state.ready && Catalog.isLoaded()) {
            val (distinct, copies, ex) = collectionVm.stats(state.holdings)
            val totalCards = Catalog.cards.size
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Progreso de la colección",
                        color = colors.ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HomeStat("Cartas", "$distinct/$totalCards")
                        HomeStat("Copias", copies.toString())
                        HomeStat("ex", ex.toString())
                    }
                    Spacer(Modifier.height(14.dp))
                    ThinProgress(owned = distinct, total = totalCards)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$distinct de $totalCards cartas distintas",
                        color = colors.inkMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        HomeActionCard(
            title = "Ver colección",
            subtitle = "Explora expansiones y marca lo que tienes",
            onClick = onOpenCollection,
        )
        Spacer(Modifier.height(12.dp))
        HomeActionCard(
            title = "Construir un mazo",
            subtitle = "Arma 20 cartas y genera el código QR",
            onClick = onOpenDeck,
        )
        Spacer(Modifier.height(12.dp))
        HomeActionCard(
            title = "Pedir una estrategia",
            subtitle = "El asistente sugiere mazos con tu colección",
            onClick = onOpenStrategy,
        )
    }
}

@Composable
private fun HomeStat(label: String, value: String) {
    val colors = LabTheme.colors
    Column {
        Text(text = value, color = colors.ink, fontWeight = FontWeight.Bold)
        Text(text = label, color = colors.inkMuted)
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = LabTheme.colors
    LabSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 20.dp)) {
            Text(text = title, color = colors.ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, color = colors.inkMuted)
        }
    }
}
