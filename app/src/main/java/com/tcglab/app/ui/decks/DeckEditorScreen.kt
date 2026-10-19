package com.tcglab.app.ui.decks

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tcglab.app.R
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.ENERGIES
import com.tcglab.app.folio.fold
import com.tcglab.app.ui.components.ElementDot
import com.tcglab.app.ui.components.EnergyDot
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted
import com.tcglab.app.ui.theme.LabOutline
import com.tcglab.app.ui.theme.LabProgressFill
import com.tcglab.app.ui.theme.LabSheetShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeckEditorScreen(
    deckId: String?,
    viewModel: DecksViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val defaultName = stringResource(R.string.deck_default_name)
    var name by remember { mutableStateOf(defaultName) }
    var energies by remember { mutableStateOf(listOf<Int>()) }
    var cards by remember { mutableStateOf(listOf<DeckCard>()) }
    var addFilter by remember { mutableStateOf("all") }
    var search by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(deckId, state.decks) {
        if (deckId != null) {
            val deck = state.decks.find { it.id == deckId }
            if (deck != null) {
                name = deck.name
                energies = deck.energies
                cards = deck.cards
            }
        }
    }

    val problems = viewModel.problems(cards, energies)
    val ready = viewModel.ready(cards, energies)
    val total = viewModel.total(cards)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.deck_editor_title), color = LabInk, modifier = Modifier.weight(1f))
            LabTextButton(stringResource(R.string.save)) {
                viewModel.saveDeck(deckId, name, energies, cards)
                onBack()
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.deck_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.energies), color = LabInk)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ENERGIES.forEach { energy ->
                    val selected = energy.id in energies
                    FilterChip(
                        selected = selected,
                        onClick = {
                            energies = when {
                                selected -> energies - energy.id
                                energies.size >= 3 -> energies
                                else -> energies + energy.id
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EnergyDot(energy.id)
                                Spacer(Modifier.width(6.dp))
                                Text(energy.es)
                            }
                        },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("$total/20", color = LabInk)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(20) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = 12.dp, height = 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (index < total) LabProgressFill else LabOutline),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.deck_cards), color = LabInk)
            Spacer(Modifier.height(8.dp))
            cards.forEach { entry ->
                val card = Catalog.findCard(entry.set, entry.number)
                LabSurfaceCard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (card != null && card.element.isNotBlank() && !card.trainer) {
                                    ElementDot(card.element)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(card?.name ?: "${entry.set} ${entry.number}", color = LabInk)
                            }
                            Text("×${entry.count}", color = LabInkMuted)
                        }
                        LabTextButton("−") {
                            cards = cards.mapNotNull {
                                if (it.set == entry.set && it.number == entry.number) {
                                    if (it.count <= 1) null else it.copy(count = it.count - 1)
                                } else it
                            }
                        }
                    }
                }
            }

            if (problems.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(problems.first(), color = LabInkMuted)
            }

            Spacer(Modifier.height(16.dp))
            LabPrimaryButton(
                text = stringResource(R.string.qr_code),
                onClick = {
                    qrBitmap = encodeQrBitmap(viewModel.qrPayload(cards, energies))
                    showQr = true
                },
                enabled = ready,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.add_from_collection), color = LabInk)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "all" to stringResource(R.string.filter_all),
                    "grass" to stringResource(R.string.energy_grass),
                    "fire" to stringResource(R.string.energy_fire),
                    "water" to stringResource(R.string.energy_water),
                    "lightning" to stringResource(R.string.energy_lightning),
                    "psychic" to stringResource(R.string.energy_psychic),
                    "fighting" to stringResource(R.string.energy_fighting),
                    "darkness" to stringResource(R.string.energy_darkness),
                    "metal" to stringResource(R.string.energy_metal),
                    "trainer" to stringResource(R.string.filter_trainer),
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = addFilter == key,
                        onClick = { addFilter = key },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val owned = viewModel.ownedCards(state.holdings).filter { card ->
                val matchesFilter = when (addFilter) {
                    "all" -> true
                    "trainer" -> card.trainer
                    else -> !card.trainer && card.element == addFilter
                }
                val q = fold(search)
                val matchesSearch = q.isEmpty() ||
                    fold(card.name).contains(q) ||
                    "${card.set} ${card.number}".lowercase().contains(q)
                matchesFilter && matchesSearch
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(owned, key = { "${it.set}-${it.number}" }) { card ->
                    val enabled = viewModel.canAdd(cards, state.holdings, card.set, card.number)
                    LabSurfaceCard(
                        onClick = {
                            if (!enabled) return@LabSurfaceCard
                            val existing = cards.find { it.set == card.set && it.number == card.number }
                            cards = if (existing == null) {
                                cards + DeckCard(card.set, card.number, 1)
                            } else {
                                cards.map {
                                    if (it.set == card.set && it.number == card.number) {
                                        it.copy(count = it.count + 1)
                                    } else it
                                }
                            }
                        },
                    ) {
                        Column(Modifier.padding(6.dp)) {
                            AsyncImage(
                                model = Catalog.cardImageUrl(card.set, card.number),
                                contentDescription = card.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f),
                            )
                            Text(card.name, color = if (enabled) LabInk else LabInkMuted, maxLines = 2)
                        }
                    }
                }
            }
        }
    }

    if (showQr && qrBitmap != null) {
        ModalBottomSheet(
            onDismissRequest = { showQr = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = LabSheetShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code),
                    modifier = Modifier.size(280.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.qr_help),
                    color = LabInkMuted,
                )
                Spacer(Modifier.height(16.dp))
                LabPrimaryButton(stringResource(R.string.download), onClick = {
                    saveBitmap(context, qrBitmap!!, "${name}-qr.png")
                })
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun saveBitmap(context: android.content.Context, bitmap: Bitmap, filename: String) {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
    resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
}
