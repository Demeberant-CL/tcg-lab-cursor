package com.tcglab.app.ui.hub

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.ENERGIES
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.energyLabel
import com.tcglab.app.folio.fold
import com.tcglab.app.folio.padNumber
import com.tcglab.app.ui.collection.CollectionViewModel
import com.tcglab.app.ui.collection.OwnFilter
import com.tcglab.app.ui.components.CardArt
import com.tcglab.app.ui.components.ElementDot
import com.tcglab.app.ui.components.EnergyDot
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.components.ThinProgress
import com.tcglab.app.ui.decks.DecksViewModel
import com.tcglab.app.ui.decks.encodeQrBitmap
import com.tcglab.app.ui.grok.GrokViewModel
import com.tcglab.app.ui.theme.LabBackground
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted
import com.tcglab.app.ui.theme.LabOutline
import com.tcglab.app.ui.theme.LabProgressFill
import com.tcglab.app.ui.theme.LabSheetShape
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HubScreen(
    collectionVm: CollectionViewModel,
    decksVm: DecksViewModel,
    grokVm: GrokViewModel,
    rootModifier: Modifier = Modifier,
) {
    val collection by collectionVm.state.collectAsState()
    val decksState by decksVm.state.collectAsState()
    val grokMessages by grokVm.messages.collectAsState()
    val grokUi by grokVm.ui.collectAsState()
    val query by collectionVm.searchQuery.collectAsState()
    val ownFilter by collectionVm.ownFilter.collectAsState()
    val sheetCard by collectionVm.sheetCard.collectAsState()

    var selectedSet by remember { mutableStateOf<String?>(null) }
    var deckId by remember { mutableStateOf<String?>(null) }
    var deckName by remember { mutableStateOf("Mazo") }
    var energies by remember { mutableStateOf(listOf<Int>()) }
    var cards by remember { mutableStateOf(listOf<DeckCard>()) }
    var addFilter by remember { mutableStateOf("all") }
    var addSearch by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var apiKeyDraft by remember { mutableStateOf("") }
    var showQr by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var importAsk by remember { mutableStateOf(false) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val ready = decksVm.ready(cards, energies)
    val total = decksVm.total(cards)
    val problems = decksVm.problems(cards, energies)

    LaunchedEffect(deckId, decksState.decks) {
        val deck = decksState.decks.find { it.id == deckId }
        if (deck != null) {
            deckName = deck.name
            energies = deck.energies
            cards = deck.cards
        }
    }
    LaunchedEffect(Unit) { grokVm.refreshKeyState() }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) {
            pendingCsv = text
            importAsk = true
        }
    }

    Column(
        modifier = rootModifier
            .fillMaxSize()
            .background(LabBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(text = "TCG LAB", color = LabInk, fontWeight = FontWeight.Bold)
        Text(text = "Álbum, mazo y asistente en un solo lugar", color = LabInkMuted)
        Spacer(modifier = Modifier.height(14.dp))

        if (collection.ready) {
            val stats = collectionVm.stats(collection.holdings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                HubStat(label = "Cartas", value = stats.first.toString())
                HubStat(label = "Copias", value = stats.second.toString())
                HubStat(label = "ex", value = stats.third.toString())
            }
            Spacer(Modifier.height(12.dp))
        }

        Text(text = "Álbum", color = LabInk, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabTextButton(text = "Importar CSV") {
                importLauncher.launch(arrayOf("text/*", "text/csv"))
            }
            LabTextButton(text = "Exportar CSV") {
                val holdings = collection.holdings.map { (key, qty) ->
                    val parts = key.split("#")
                    Holding(parts[0], parts[1].toInt(), qty)
                }
                val csv = collectionVm.exportCsv(holdings)
                val file = File(context.cacheDir, "coleccion.csv")
                file.writeText(csv)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        "Exportar CSV",
                    ),
                )
            }
            LabTextButton(text = "Original") { collectionVm.resetSeed() }
        }

        val currentSet = selectedSet
        if (currentSet == null) {
            collection.sets.forEach { progress ->
                LabSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    onClick = { selectedSet = progress.set.code },
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = progress.set.es, color = LabInk, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${progress.set.code}  ·  ${progress.ownedDistinct}/${progress.total}",
                            color = LabInkMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        ThinProgress(owned = progress.ownedDistinct, total = progress.total)
                    }
                }
            }
        } else {
            val set = Catalog.setInfo(currentSet)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabTextButton(text = "Volver") { selectedSet = null }
                Text(text = set?.es ?: currentSet, color = LabInk, fontWeight = FontWeight.SemiBold)
            }
            OutlinedTextField(
                value = query,
                onValueChange = collectionVm::setQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre o A1 36") },
                singleLine = true,
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
                        selected = ownFilter == value,
                        onClick = { collectionVm.setFilter(value) },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val gridCards = collectionVm.cardsForSet(currentSet, collection.holdings)
            CardGrid(
                cards = gridCards.map { card ->
                    val qty = collection.holdings[Catalog.cardKey(card.set, card.number)] ?: 0
                    Triple(card, qty, true)
                },
                onClick = { card -> collectionVm.openCard(card) },
            )
        }

        Spacer(Modifier.height(22.dp))
        Text(text = "Mazo y asistente", color = LabInk, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Arma el mazo aquí y pide ayuda a la IA en el mismo bloque.",
            color = LabInkMuted,
        )
        Spacer(Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabPrimaryButton(text = "Nuevo mazo") {
                deckId = null
                deckName = "Mazo"
                energies = emptyList()
                cards = emptyList()
            }
            decksState.decks.take(6).forEach { deck ->
                FilterChip(
                    selected = deckId == deck.id,
                    onClick = { deckId = deck.id },
                    label = { Text("${deck.name} ${deck.cards.sumOf { it.count }}/20") },
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = deckName,
            onValueChange = { deckName = it },
            label = { Text("Nombre del mazo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Text(text = "Energías", color = LabInk)
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
                            EnergyDot(id = energy.id)
                            Spacer(Modifier.width(4.dp))
                            Text(energy.es)
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "$total/20", color = LabInk, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(20) { index ->
                Box(
                    modifier = Modifier
                        .size(width = 11.dp, height = 7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < total) LabProgressFill else LabOutline),
                )
            }
        }
        if (energies.isNotEmpty()) {
            Text(text = energyLabel(energies), color = LabInkMuted)
        }
        Spacer(Modifier.height(8.dp))
        cards.forEach { entry ->
            val card = Catalog.findCard(entry.set, entry.number)
            LabSurfaceCard(modifier = Modifier.padding(bottom = 6.dp)) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (card != null && card.element.isNotBlank() && !card.trainer) {
                        ElementDot(element = card.element)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = "${card?.name ?: "${entry.set} ${entry.number}"} x${entry.count}",
                        modifier = Modifier.weight(1f),
                        color = LabInk,
                    )
                    LabTextButton(text = "-") {
                        cards = cards.mapNotNull {
                            if (it.set == entry.set && it.number == entry.number) {
                                if (it.count <= 1) null else it.copy(count = it.count - 1)
                            } else {
                                it
                            }
                        }
                    }
                }
            }
        }
        if (problems.isNotEmpty()) {
            Text(text = problems.first(), color = LabInkMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabPrimaryButton(text = "Guardar mazo", enabled = ready) {
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

        Spacer(Modifier.height(12.dp))
        Text(text = "Sumar desde la colección", color = LabInk)
        OutlinedTextField(
            value = addSearch,
            onValueChange = { addSearch = it },
            placeholder = { Text("Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
        val owned = decksVm.ownedCards(decksState.holdings).filter { card ->
            val okFilter = when (addFilter) {
                "all" -> true
                "trainer" -> card.trainer
                else -> !card.trainer && card.element == addFilter
            }
            val q = fold(addSearch)
            okFilter && (q.isEmpty() || fold(card.name).contains(q))
        }
        CardGrid(
            cards = owned.map { card ->
                Triple(card, 0, decksVm.canAdd(cards, decksState.holdings, card.set, card.number))
            },
            onClick = { card ->
                if (!decksVm.canAdd(cards, decksState.holdings, card.set, card.number)) return@CardGrid
                val existing = cards.find { it.set == card.set && it.number == card.number }
                cards = if (existing == null) {
                    cards + DeckCard(card.set, card.number, 1)
                } else {
                    cards.map {
                        if (it.set == card.set && it.number == card.number) {
                            it.copy(count = it.count + 1)
                        } else {
                            it
                        }
                    }
                }
            },
        )

        Spacer(Modifier.height(18.dp))
        Text(text = "Asistente para el mazo", color = LabInk, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Misma pantalla: pide un mazo listo para el QR con lo que tienes.",
            color = LabInkMuted,
        )
        Spacer(Modifier.height(8.dp))

        if (!grokUi.hasKey) {
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Pega tu clave de console.x.ai. Se guarda solo en este teléfono.",
                        color = LabInkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Clave de Grok") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    LabPrimaryButton(text = "Guardar clave") {
                        grokVm.saveApiKey(apiKeyDraft)
                        apiKeyDraft = ""
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        } else {
            Text(text = "Clave configurada", color = LabInkMuted)
            LabTextButton(text = "Cambiar clave") { grokVm.saveApiKey("") }
        }

        listOf(
            "Arma un mazo de fuego con lo que tengo y déjalo listo para el QR",
            "Arma un mazo de agua de 20 cartas, solo con lo que tengo",
            "¿Qué ex de rayo tengo?",
        ).forEach { suggestion ->
            LabSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                onClick = { if (grokUi.hasKey && !grokUi.busy) grokVm.send(suggestion) },
            ) {
                Text(text = suggestion, modifier = Modifier.padding(12.dp), color = LabInk)
            }
        }

        grokMessages.takeLast(8).forEach { message ->
            LabSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (message.role == "user") "Tú" else "Asistente",
                        color = LabInkMuted,
                    )
                    Text(text = message.content, color = LabInk)
                    if (message.deckEvent == "saved" && message.deckId != null) {
                        LabTextButton(text = "Abrir ${message.deckName ?: "mazo"}") {
                            deckId = message.deckId
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pide un mazo…") },
                enabled = grokUi.hasKey && !grokUi.busy,
                singleLine = true,
            )
            LabPrimaryButton(
                text = if (grokUi.busy) "…" else "Enviar",
                enabled = grokUi.hasKey && !grokUi.busy && draft.isNotBlank(),
            ) {
                val text = draft
                draft = ""
                grokVm.send(text)
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    val openCard = sheetCard
    if (openCard != null) {
        val qty = collection.holdings[Catalog.cardKey(openCard.set, openCard.number)] ?: 0
        ModalBottomSheet(
            onDismissRequest = collectionVm::closeCard,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = LabSheetShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CardArt(
                    set = openCard.set,
                    number = openCard.number,
                    name = openCard.name,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .aspectRatio(0.72f),
                )
                Spacer(Modifier.height(10.dp))
                Text(text = openCard.name, color = LabInk, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${openCard.set} ${padNumber(openCard.number)}",
                    color = LabInkMuted,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabPrimaryButton(text = "-", enabled = qty > 0) {
                        collectionVm.changeQty(openCard.set, openCard.number, qty - 1)
                    }
                    Text(text = "$qty", color = LabInk)
                    LabPrimaryButton(text = "+") {
                        collectionVm.changeQty(openCard.set, openCard.number, qty + 1)
                    }
                }
                LabTextButton(text = "Cerrar", onClick = collectionVm::closeCard)
                Spacer(Modifier.height(16.dp))
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
                    contentDescription = "Código QR",
                    modifier = Modifier.size(280.dp),
                )
                Text(text = "En el juego: Mazos, Crear, Escanear código.", color = LabInkMuted)
                Spacer(Modifier.height(12.dp))
                LabPrimaryButton(text = "Descargar") {
                    saveBitmap(context, qrBitmap!!, "$deckName-qr.png")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (importAsk && pendingCsv != null) {
        AlertDialog(
            onDismissRequest = {
                importAsk = false
                pendingCsv = null
            },
            title = { Text("Importar CSV") },
            text = { Text("¿Combinar o reemplazar la colección?") },
            confirmButton = {
                LabTextButton(text = "Combinar") {
                    collectionVm.importCsv(pendingCsv!!, replace = false)
                    importAsk = false
                    pendingCsv = null
                }
            },
            dismissButton = {
                LabTextButton(text = "Reemplazar") {
                    collectionVm.importCsv(pendingCsv!!, replace = true)
                    importAsk = false
                    pendingCsv = null
                }
            },
        )
    }
}

@Composable
private fun HubStat(label: String, value: String) {
    LabSurfaceCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(text = value, color = LabInk, fontWeight = FontWeight.Bold)
            Text(text = label, color = LabInkMuted)
        }
    }
}

@Composable
private fun CardGrid(
    cards: List<Triple<com.tcglab.app.folio.CardRow, Int, Boolean>>,
    onClick: (com.tcglab.app.folio.CardRow) -> Unit,
) {
    val rows = cards.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (card, qty, enabled) ->
                    LabSurfaceCard(
                        modifier = Modifier.weight(1f),
                        onClick = { if (enabled) onClick(card) },
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            CardArt(
                                set = card.set,
                                number = card.number,
                                name = card.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f),
                            )
                            Text(
                                text = card.name,
                                color = if (enabled) LabInk else LabInkMuted,
                                maxLines = 2,
                            )
                            if (qty > 0) {
                                Text(text = "x$qty · ${padNumber(card.number)}", color = LabInkMuted)
                            }
                        }
                    }
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
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
    resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
}
