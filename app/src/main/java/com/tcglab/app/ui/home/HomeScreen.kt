package com.tcglab.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.folio.Catalog
import com.tcglab.app.ui.collection.CollectionViewModel
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.ThinProgress
import com.tcglab.app.ui.theme.LabTheme

private val LabGold = Color(0xFFFFC857)
private val LabNavy = Color(0xFF172536)

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "TCG LAB",
                    color = colors.inkMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    "Centro de juego",
                    color = colors.ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = colors.ink)
            }
        }

        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(LabNavy),
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(LabGold),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = LabNavy,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Text(
                        "SIGUIENTE JUGADA",
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Arma un mazo\nque vaya contigo",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Grok lo prepara con las cartas de tu colección.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onOpenStrategy,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LabGold,
                        contentColor = LabNavy,
                    ),
                ) {
                    Text("Crear mazo con IA", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (state.ready && Catalog.isLoaded()) {
            val (distinct, copies, ex) = collectionVm.stats(state.holdings)
            val totalCards = Catalog.cards.size
            val percent = if (totalCards == 0) 0 else distinct * 100 / totalCards
            Spacer(Modifier.height(18.dp))
            LabSurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenCollection) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Colección",
                                color = colors.ink,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "$distinct de $totalCards cartas distintas",
                                color = colors.inkMuted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            "$percent%",
                            color = colors.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ThinProgress(owned = distinct, total = totalCards, modifier = Modifier.height(6.dp))
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HomeStat("Cartas", distinct.toString())
                        HomeStat("Copias", copies.toString())
                        HomeStat("Pokémon ex", ex.toString())
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Colección y mazos",
            color = colors.ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CollectionsBookmark,
                title = "Colección",
                subtitle = "Explora cartas",
                onClick = onOpenCollection,
            )
            HomeActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Style,
                title = "Mazos",
                subtitle = "Ver y editar",
                onClick = onOpenDeck,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeStat(label: String, value: String) {
    val colors = LabTheme.colors
    Column {
        Text(value, color = colors.ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, color = colors.inkMuted, style = MaterialTheme.typography.labelMedium)
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
    LabSurfaceCard(modifier = modifier.height(104.dp), onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                     .size(34.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(19.dp))
            }
            Column {
                Text(title, color = colors.ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = colors.inkMuted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

