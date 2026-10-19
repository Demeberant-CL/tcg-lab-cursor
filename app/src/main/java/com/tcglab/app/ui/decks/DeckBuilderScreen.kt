package com.tcglab.app.ui.decks

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.ENERGIES
import com.tcglab.app.folio.energyLabel
import com.tcglab.app.folio.fold
import com.tcglab.app.ui.components.CardArt
import com.tcglab.app.ui.components.ElementDot
import com.tcglab.app.ui.components.EnergyDot
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.theme.LabSheetShape
import com.tcglab.app.ui.theme.LabTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeckBuilderScreen(
    decksVm: DecksViewModel,
    onBack: () -> Unit,
    initialDeckId: String? = null,
    modifier: Modifier = Modifier,
) {
    val state by decksVm.state.collectAsState()
    val colors = LabTheme.colors
    val context = LocalContext.current

    var deckId by remember { mutableStateOf(initialDeckId) }
    var deckName by remember { mutableStateOf("Mazo") }
    var energies by remember { mutableStateOf(listOf<Int>()) }
    var cards by remember { mutableStateOf(listOf<DeckCard>()) }
    var addFilter by remember { mutableStateOf("all") }
    var addSearch by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(deckId, state.decks) {
        val deck = state.decks.find { it.id == deckId }
        if (deck != null) {
            deckName = deck.name
            energies = deck.energies
            cards = deck.cards
        }
    }

    val ready = decksVm.ready(cards, energies)
    val total = decksVm.total(cards)
    val problems = decksVm.problems(cards, energies)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.ink,
        unfocusedBorderColor = colors.outline,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        focusedTextColor = colors.ink,
        unfocusedTextColor = colors.ink,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = colors.ink)
            }
            Text(
                text = "Construir un mazo",
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabPrimaryButton("Nuevo") {
                deckId = null
                deckName = "Mazo"
                energies = emptyList()
                cards = emptyList()
            }
            state.decks.take(8).forEach { deck ->
                FilterChip(
                    selected = deckId == deck.id,
                    onClick = { deckId = deck.id },
                    label = { Text(deck.name) },
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = deckName,
            onValueChange = { deckName = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
        )
        Spacer(Modifier.height(8.dp))
        Text("Energías", color = colors.ink, fontWeight = FontWeight.Medium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            Spacer(Modifier.width(4.dp))
                            Text(energy.es)
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("$total/20", color = colors.ink, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(20) { i ->
                Box(
                    Modifier
                        .size(width = 11.dp, height = 7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < total) colors.progressFill else colors.outline),
                )
            }
        }
        if (energies.isNotEmpty()) {
            Text(energyLabel(energies), color = colors.inkMuted)
        }

        Spacer(Modifier.height(8.dp))
        cards.forEach { entry ->
            val card = Catalog.findCard(entry.set, entry.number)
            LabSurfaceCard(modifier = Modifier.padding(bottom = 6.dp)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (card != null && card.element.isNotBlank() && !card.trainer) {
                        ElementDot(card.element)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${card?.name ?: "${entry.set} ${entry.number}"} ×${entry.count}",
                        modifier = Modifier.weight(1f),
                        color = colors.ink,
                    )
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
            Text(problems.first(), color = colors.inkMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabPrimaryButton("Guardar") {
                decksVm.saveDeck(deckId, deckName, energies, cards)
            }
            LabPrimaryButton(
                text = "Código QR",
                enabled = ready,
                onClick = {
                    qrBitmap = encodeQrBitmap(decksVm.qrPayload(cards, energies))
                    showQr = true
                },
            )
        }

        Spacer(Modifier.height(14.dp))
        Text("Sumar desde la colección", color = colors.ink, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = addSearch,
            onValueChange = { addSearch = it },
            placeholder = { Text("Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "all" to "Todas",
                "fire" to "Fuego",
                "water" to "Agua",
                "grass" to "Planta",
                "lightning" to "Rayo",
                "psychic" to "Psíquica",
                "fighting" to "Lucha",
                "darkness" to "Oscura",
                "metal" to "Metálica",
                "trainer" to "Entrenador",
            ).forEach { (key, label) ->
                FilterChip(
                    selected = addFilter == key,
                    onClick = { addFilter = key },
                    label = { Text(label) },
                )
            }
        }
        val owned = decksVm.ownedCards(state.holdings).filter { card ->
            val ok = when (addFilter) {
                "all" -> true
                "trainer" -> card.trainer
                else -> !card.trainer && card.element == addFilter
            }
            val q = fold(addSearch)
            ok && (q.isEmpty() || fold(card.name).contains(q))
        }
        owned.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { card ->
                    val enabled = decksVm.canAdd(cards, card.set, card.number)
                    LabSurfaceCard(
                        modifier = Modifier.weight(1f),
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
                            CardArt(
                                set = card.set,
                                number = card.number,
                                name = card.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f),
                            )
                            Text(card.name, color = if (enabled) colors.ink else colors.inkMuted, maxLines = 2)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showQr && qrBitmap != null) {
        ModalBottomSheet(
            onDismissRequest = { showQr = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = LabSheetShape,
            containerColor = colors.surfaceRaised,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(qrBitmap!!.asImageBitmap(), "Código QR", Modifier.size(280.dp))
                Text("En el juego: Mazos, Crear, Escanear código.", color = colors.inkMuted)
                Spacer(Modifier.height(12.dp))
                LabPrimaryButton("Descargar") {
                    saveQr(context, qrBitmap!!, "$deckName-qr.png")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

private fun saveQr(context: android.content.Context, bitmap: Bitmap, filename: String) {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
    resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
}
