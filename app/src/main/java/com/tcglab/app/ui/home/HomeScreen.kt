package com.tcglab.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
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
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("TCG LAB", color = colors.ink, fontWeight = FontWeight.Bold)
                Text("Tu colección, tus mazos, tu asistente", color = colors.inkMuted)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = colors.ink)
            }
        }

        if (state.ready && Catalog.isLoaded()) {
            val (distinct, copies, ex) = collectionVm.stats(state.holdings)
            val totalCards = Catalog.cards.size
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Tu colección", color = colors.ink, fontWeight = FontWeight.SemiBold)
                            Text("$distinct de $totalCards cartas distintas", color = colors.inkMuted)
                        }
                        Text(
                            text = "${if (totalCards == 0) 0 else distinct * 100 / totalCards}%",
                            color = colors.ink,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ThinProgress(owned = distinct, total = totalCards)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HomeStat("Cartas", distinct.toString())
                        HomeStat("Copias", copies.toString())
                        HomeStat("ex", ex.toString())
                    }
                }
            }
        }

        Text("Accesos rápidos", color = colors.ink, fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CollectionsBookmark,
                title = "Colección",
                subtitle = "Ver cartas",
                onClick = onOpenCollection,
            )
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Style,
                title = "Mis mazos",
                subtitle = "Crear y abrir",
                onClick = onOpenDeck,
            )
        }

        LabSurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenStrategy) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("TCG LAB AI", color = colors.ink, fontWeight = FontWeight.Bold)
                    Text("Crea y mejora mazos usando tu colección", color = colors.inkMuted)
                }
            }
        }
    }
}

@Composable
private fun HomeStat(label: String, value: String) {
    val colors = LabTheme.colors
    Column {
        Text(value, color = colors.ink, fontWeight = FontWeight.Bold)
        Text(label, color = colors.inkMuted)
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = LabTheme.colors
    LabSurfaceCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(10.dp))
            Column {
                Text(title, color = colors.ink, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = colors.inkMuted)
            }
        }
    }
}
