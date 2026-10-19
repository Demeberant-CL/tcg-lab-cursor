package com.tcglab.app.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.padNumber
import com.tcglab.app.ui.components.CardArt
import com.tcglab.app.ui.components.ElementDot
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.theme.LabSheetShape
import com.tcglab.app.ui.theme.LabTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetDetailScreen(
    setCode: String,
    viewModel: CollectionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.ownFilter.collectAsState()
    val sheetCard by viewModel.sheetCard.collectAsState()
    val colors = LabTheme.colors
    val set = Catalog.setInfo(setCode)
    val cards = viewModel.cardsForSet(setCode, state.holdings)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = colors.ink)
            }
            Column(Modifier.weight(1f)) {
                Text(text = set?.es ?: setCode, color = colors.ink, fontWeight = FontWeight.SemiBold)
                Text(text = setCode, color = colors.inkMuted)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nombre o A1 36") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.ink,
                unfocusedBorderColor = colors.outline,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedTextColor = colors.ink,
                unfocusedTextColor = colors.ink,
            ),
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                OwnFilter.All to "Todas",
                OwnFilter.Owned to "Tengo",
                OwnFilter.Missing to "Me faltan",
                OwnFilter.Tracked to "Rastreadas",
            ).forEach { (value, label) ->
                FilterChip(
                    selected = filter == value,
                    onClick = { viewModel.setFilter(value) },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cards.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { card ->
                        val qty = state.holdings[Catalog.cardKey(card.set, card.number)] ?: 0
                        LabSurfaceCard(
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.openCard(card) },
                        ) {
                            Column(Modifier.padding(6.dp)) {
                                if (qty > 0 || filter != OwnFilter.Missing) {
                                    CardArt(
                                        set = card.set,
                                        number = card.number,
                                        name = card.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.72f),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.72f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.emptySlot),
                                    )
                                }
                                Text(card.name, color = colors.ink, maxLines = 2)
                                Text(
                                    if (qty > 0) "×$qty" else "—",
                                    color = colors.inkMuted,
                                )
                            }
                        }
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    val open = sheetCard
    if (open != null) {
        val qty = state.holdings[Catalog.cardKey(open.set, open.number)] ?: 0
        ModalBottomSheet(
            onDismissRequest = viewModel::closeCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = LabSheetShape,
            containerColor = colors.surfaceRaised,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CardArt(
                    set = open.set,
                    number = open.number,
                    name = open.name,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(0.72f),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (open.element.isNotBlank() && !open.trainer) {
                        ElementDot(open.element)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                    }
                    Text(open.name, color = colors.ink, fontWeight = FontWeight.SemiBold)
                }
                Text("${open.set} ${padNumber(open.number)}", color = colors.inkMuted)
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabPrimaryButton("−", enabled = qty > 0) {
                        viewModel.changeQty(open.set, open.number, qty - 1)
                    }
                    Text("$qty", color = colors.ink)
                    LabPrimaryButton("+") {
                        viewModel.changeQty(open.set, open.number, qty + 1)
                    }
                }
                LabTextButton("Cerrar", onClick = viewModel::closeCard)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
