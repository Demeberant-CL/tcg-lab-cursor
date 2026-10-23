package com.tcglab.app.ui.decks

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.deckProblems
import com.tcglab.app.folio.deckTotal
import com.tcglab.app.folio.encodeResolved
import com.tcglab.app.folio.resolveDeck
import com.tcglab.app.ui.components.BrandHeader
import com.tcglab.app.ui.components.EnergyRow
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeckDetailScreen(
    repository: LabRepository,
    deckId: String,
    onBack: () -> Unit,
) {
    val viewModel: DeckDetailViewModel = viewModel(
        key = deckId,
        factory = DeckDetailViewModel.factory(repository, deckId),
    )
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = LabInk,
                )
            }
            BrandHeader("Detalle del mazo")
        }

        when {
            state.loading -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.weight(1f))
            }

            state.error != null -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    state.error.orEmpty(),
                    color = LabInkMuted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            state.deck != null -> {
                val deck = state.deck
                DeckDetailContent(state.deck!!)
            }
        }
    }
}

@Composable
private fun DeckDetailContent(deck: Deck) {
    val resolved = resolveDeck(deck.cards)
    val problems = deckProblems(resolved, deck.energies)
    val payload = if (problems.isEmpty()) encodeResolved(resolved, deck.energies) else null
    val qrState by produceState<Bitmap?>(initialValue = null, payload) {
        value = payload?.let {
            withContext(Dispatchers.Default) {
                encodeQrBitmap(it, 768)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp,
        ),
    ) {
        item {
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(deck.name, color = LabInk)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${deckTotal(deck.cards)}/20 cartas",
                        color = LabInkMuted,
                    )
                    Spacer(Modifier.height(10.dp))
                    EnergyRow(deck.energies)
                }
            }
        }

        if (problems.isNotEmpty()) {
            item {
                LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mazo no válido para QR", color = LabInk)
                        problems.forEach {
                            Spacer(Modifier.height(4.dp))
                            Text(it, color = LabInkMuted)
                        }
                    }
                }
            }
        } else {
            item {
                LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Código QR", color = LabInk)
                        Spacer(Modifier.height(10.dp))
                        if (qrState != null) {
                            Image(
                                bitmap = qrState!!.asImageBitmap(),
                                contentDescription = "Código QR del mazo",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth,
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        item {
            Text("Cartas", color = LabInk)
        }

        items(resolved) { card ->
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(card.name, color = LabInk)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${card.set} ${card.number}",
                            color = LabInkMuted,
                        )
                    }
                    Text("×${card.count}", color = LabInk)
                }
            }
        }
    }
}
