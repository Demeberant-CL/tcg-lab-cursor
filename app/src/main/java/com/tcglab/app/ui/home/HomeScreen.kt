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
}@Composable\nfun HomeScreen(\n    collectionVm: CollectionViewModel,\n    onOpenCollection: () -> Unit,\n    onOpenDeck: () -> Unit,\n    onOpenStrategy: () -> Unit,\n    onOpenSettings: () -> Unit,\n    modifier: Modifier = Modifier,\n) {\n    val state by collectionVm.state.collectAsState()\n    val colors = LabTheme.colors\n\n    Column(\n        modifier = modifier\n            .fillMaxSize()\n            .padding(horizontal = 18.dp, vertical = 14.dp),\n        verticalArrangement = Arrangement.spacedBy(12.dp),\n    ) {\n        Row(\n            modifier = Modifier.fillMaxWidth(),\n            verticalAlignment = Alignment.CenterVertically,\n        ) {\n            Column(Modifier.weight(1f)) {\n                Text("TCG LAB", color = colors.ink, fontWeight = FontWeight.Bold)\n                Text("Tu colección, tus mazos, tu asistente", color = colors.inkMuted)\n            }\n            IconButton(onClick = onOpenSettings) {\n                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = colors.ink)\n            }\n        }\n\n        if (state.ready && Catalog.isLoaded()) {\n            val (distinct, copies, ex) = collectionVm.stats(state.holdings)\n            val totalCards = Catalog.cards.size\n            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {\n                Column(Modifier.padding(16.dp)) {\n                    Row(\n                        modifier = Modifier.fillMaxWidth(),\n                        verticalAlignment = Alignment.CenterVertically,\n                    ) {\n                        Column(Modifier.weight(1f)) {\n                            Text("Tu colección", color = colors.ink, fontWeight = FontWeight.SemiBold)\n                            Text("$distinct de $totalCards cartas distintas", color = colors.inkMuted)\n                        }\n                        Text(\n                            text = "${if (totalCards == 0) 0 else distinct * 100 / totalCards}%",\n                            color = colors.ink,\n                            fontWeight = FontWeight.Bold,\n                        )\n                    }\n                    Spacer(Modifier.height(10.dp))\n                    ThinProgress(owned = distinct, total = totalCards)\n                    Spacer(Modifier.height(10.dp))\n                    Row(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.SpaceBetween,\n                    ) {\n                        HomeStat("Cartas", distinct.toString())\n                        HomeStat("Copias", copies.toString())\n                        HomeStat("ex", ex.toString())\n                    }\n                }\n            }\n        }\n\n        Text("Accesos rápidos", color = colors.ink, fontWeight = FontWeight.SemiBold)\n\n        Row(\n            modifier = Modifier.fillMaxWidth(),\n            horizontalArrangement = Arrangement.spacedBy(10.dp),\n        ) {\n            HomeActionCard(\n                modifier = Modifier.weight(1f),\n                icon = Icons.Outlined.CollectionsBookmark,\n                title = "Colección",\n                subtitle = "Ver cartas",\n                onClick = onOpenCollection,\n            )\n            HomeActionCard(\n                modifier = Modifier.weight(1f),\n                icon = Icons.Outlined.Style,\n                title = "Mis mazos",\n                subtitle = "Crear y abrir",\n                onClick = onOpenDeck,\n            )\n        }\n\n        LabSurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenStrategy) {\n            Row(\n                modifier = Modifier.padding(16.dp),\n                verticalAlignment = Alignment.CenterVertically,\n            ) {\n                Icon(\n                    imageVector = Icons.Outlined.AutoAwesome,\n                    contentDescription = null,\n                    tint = colors.ink,\n                    modifier = Modifier.size(28.dp),\n                )\n                Spacer(Modifier.size(14.dp))\n                Column(Modifier.weight(1f)) {\n                    Text("TCG LAB AI", color = colors.ink, fontWeight = FontWeight.Bold)\n                    Text("Crea y mejora mazos usando solo tu colección", color = colors.inkMuted)\n                }\n            }\n        }\n\n        Text(\n            "La IA puede analizar tus cartas y preparar un mazo de 20 cartas listo para generar su QR.",\n            color = colors.inkMuted,\n        )\n    }\n}\n\n
