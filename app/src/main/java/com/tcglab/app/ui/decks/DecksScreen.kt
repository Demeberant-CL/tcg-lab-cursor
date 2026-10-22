package com.tcglab.app.ui.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcglab.app.R
import com.tcglab.app.folio.deckTotal
import com.tcglab.app.folio.energyLabel
import com.tcglab.app.ui.components.BrandHeader
import com.tcglab.app.ui.components.EnergyRow
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted

@Composable
fun DecksScreen(
    viewModel: DecksViewModel,
    onNewDeck: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        BrandHeader(stringResource(R.string.decks_title))
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Los mazos guardados y los creados con IA aparecen aquí.",
            color = LabInkMuted,
        )
        Spacer(Modifier.height(12.dp))
        LabPrimaryButton(
            text = "Crear mazo con IA",
            onClick = onNewDeck,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        if (state.decks.isEmpty()) {
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Todavía no hay mazos", color = LabInk)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Crea tu primer mazo con IA usando las cartas de la colección.",
                        color = LabInkMuted,
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.decks, key = { it.id }) { deck ->
                    LabSurfaceCard {
                        Column(Modifier.padding(16.dp)) {
                            Text(deck.name, color = LabInk)
                            Spacer(Modifier.height(4.dp))
                            Text("${deckTotal(deck.cards)}/20", color = LabInkMuted)
                            if (deck.energies.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                EnergyRow(deck.energies)
                                Text(energyLabel(deck.energies), color = LabInkMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
