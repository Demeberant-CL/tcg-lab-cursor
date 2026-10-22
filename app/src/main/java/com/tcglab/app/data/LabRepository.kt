package com.tcglab.app.data

import android.content.Context
import androidx.room.withTransaction
import com.tcglab.app.data.db.AppDatabase
import com.tcglab.app.data.db.ChatMessageEntity
import com.tcglab.app.data.db.DeckEntity
import com.tcglab.app.data.db.HoldingEntity
import com.tcglab.app.data.db.MetaEntity
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.CollectionChange
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.CsvRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LabRepository(context: Context) {
    private val appContext = context.applicationContext
    val appContextForLogging: Context get() = appContext
    private val db = AppDatabase.get(appContext)
    val catalogReady = MutableStateFlow(false)
    val catalogError = MutableStateFlow<String?>(null)

    val holdings: Flow<List<Holding>> = db.holdings().observeAll().map { list ->
        list
            .groupBy { Catalog.canonSet(it.setCode) to it.number }
            .map { (key, rows) ->
                Holding(key.first, key.second, rows.sumOf { it.qty }.coerceIn(0, 99))
            }
            .sortedWith(compareBy<Holding> { it.set }.thenBy { it.number })
    }

    val decks: Flow<List<Deck>> = db.decks().observeAll().map { list ->
        list.map { it.toDeck() }
    }

    val chatMessages: Flow<List<ChatUiMessage>> = db.chat().observeAll().map { list ->
        list.map {
            ChatUiMessage(
                id = it.id,
                role = it.role,
                content = it.content,
                changes = it.changesJson?.let(::parseChanges).orEmpty(),
                deckName = it.deckName,
                deckId = it.deckEvent?.substringAfter("id:", missingDelimiterValue = "")
                    ?.takeIf { value -> value.isNotBlank() && it.deckEvent.startsWith("id:") },
                deckEvent = when {
                    it.deckEvent == null -> null
                    it.deckEvent.startsWith("id:") -> "saved"
                    else -> it.deckEvent
                },
            )
        }
    }

    suspend fun bootstrap() {
        catalogReady.value = false
        catalogError.value = null
        runCatching {
            CatalogLoader.ensureLoaded(appContext)
            normalizeStoredHoldings()
            val seeded = db.meta().get("seeded")
            if (seeded == null) {
                val seed = CatalogLoader.loadSeed(appContext)
                db.withTransaction {
                    db.holdings().upsertAll(seed.map { it.toEntity() })
                    db.meta().put(MetaEntity("seeded", "1"))
                }
            }
        }.onSuccess {
            catalogReady.value = true
        }.onFailure { error ->
            catalogError.value = error.message ?: error.javaClass.simpleName
            AppLogger.error(appContext, "Error cargando catálogo", error)
        }
    }

    suspend fun setQty(set: String, number: Int, qty: Int) {
        val clamped = qty.coerceIn(0, 99)
        val canonicalSet = Catalog.canonSet(set)
        db.holdings().upsertAll(
            listOf(HoldingEntity(Catalog.cardKey(canonicalSet, number), canonicalSet, number, clamped)),
        )
    }

    suspend fun importCsv(rows: List<CsvRow>, replace: Boolean) {
        if (rows.isEmpty()) return
        try {
            db.withTransaction {
                if (replace) {
                    db.holdings().clear()
                    db.holdings().upsertAll(rows.map {
                        val canonicalSet = Catalog.canonSet(it.set)
                        HoldingEntity(
                            Catalog.cardKey(canonicalSet, it.number),
                            canonicalSet,
                            it.number,
                            it.qty.coerceIn(0, 99),
                        )
                    })
                } else {
                    val current = db.holdings().getAll().map { it.canonicalized() }.associateBy { it.key }.toMutableMap()
                    for (row in rows) {
                        val canonicalSet = Catalog.canonSet(row.set)
                        val key = Catalog.cardKey(canonicalSet, row.number)
                        val existing = current[key]
                        val incomingQty = row.qty.coerceIn(0, 99)
                        val mergedQty =
                            if (existing == null) incomingQty else maxOf(existing.qty, incomingQty)
                        current[key] = HoldingEntity(key, canonicalSet, row.number, mergedQty)
                    }
                    db.holdings().clear()
                    db.holdings().upsertAll(current.values.toList())
                }
            }
        } catch (error: Exception) {
            AppLogger.error(appContext, "Error guardando importación CSV", error)
            throw error
        }
    }

    suspend fun resetToSeed() {
        val seed = CatalogLoader.loadSeed(appContext)
        db.withTransaction {
            db.holdings().clear()
            db.holdings().upsertAll(seed.map { it.toEntity() })
        }
    }

    suspend fun saveDeck(deck: Deck) {
        db.decks().upsert(deck.canonicalized().toEntity())
    }

    suspend fun deleteDeck(id: String) {
        db.decks().delete(id)
    }

    suspend fun deleteDeckByName(name: String) {
        db.decks().deleteByName(name)
    }

    suspend fun getDeck(id: String): Deck? = db.decks().getById(id)?.toDeck()

    suspend fun applyAiMutations(
        changes: List<CollectionChange>,
        savedDecks: List<Deck>,
        deletedDeckIds: List<String>,
    ) {
        if (changes.isEmpty() && savedDecks.isEmpty() && deletedDeckIds.isEmpty()) return
        db.withTransaction {
            if (changes.isNotEmpty()) {
                db.holdings().upsertAll(
                    changes.map {
                        HoldingEntity(
                            Catalog.cardKey(Catalog.canonSet(it.set), it.number),
                            Catalog.canonSet(it.set),
                            it.number,
                            it.to.coerceIn(0, 99),
                        )
                    },
                )
            }
            savedDecks.forEach { db.decks().upsert(it.toEntity()) }
            deletedDeckIds.distinct().forEach { db.decks().delete(it) }
        }
    }


    suspend fun appendChat(message: ChatUiMessage) {
        db.chat().insert(
            ChatMessageEntity(
                id = message.id,
                role = message.role,
                content = message.content,
                changesJson = if (message.changes.isEmpty()) null else encodeChanges(message.changes),
                deckName = message.deckName,
                deckEvent = when {
                    message.deckId != null -> "id:${message.deckId}"
                    else -> message.deckEvent
                },
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearChat() = db.chat().clear()

    suspend fun snapshotHoldings(): List<Holding> =
        db.holdings().getAll()
            .map { it.canonicalized() }
            .groupBy { it.setCode to it.number }
            .map { (key, rows) ->
                Holding(key.first, key.second, rows.sumOf { it.qty }.coerceIn(0, 99))
            }
            .sortedWith(compareBy<Holding> { it.set }.thenBy { it.number })

    suspend fun snapshotDecks(): List<Deck> =
        db.decks().getAll().map { it.toDeck().canonicalized() }

    private suspend fun normalizeStoredHoldings() {
        val current = db.holdings().getAll()
        if (current.isEmpty()) return
        val normalized = current
            .map { it.canonicalized() }
            .groupBy { it.key }
            .map { (_, rows) ->
                val first = rows.first()
                first.copy(qty = rows.sumOf { it.qty }.coerceIn(0, 99))
            }
        if (normalized.size == current.size && current.zip(normalized).all { (old, new) -> old == new }) return
        db.withTransaction {
            db.holdings().clear()
            db.holdings().upsertAll(normalized)
        }
    }
}

data class ChatUiMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val changes: List<CollectionChange> = emptyList(),
    val deckName: String? = null,
    val deckId: String? = null,
    val deckEvent: String? = null,
)

private fun Holding.toEntity(): HoldingEntity {
    val canonicalSet = Catalog.canonSet(set)
    return HoldingEntity(Catalog.cardKey(canonicalSet, number), canonicalSet, number, qty)
}

private fun HoldingEntity.canonicalized(): HoldingEntity {
    val canonicalSet = Catalog.canonSet(setCode)
    return copy(
        key = Catalog.cardKey(canonicalSet, number),
        setCode = canonicalSet,
        qty = qty.coerceIn(0, 99),
    )
}

private fun Deck.canonicalized(): Deck =
    copy(cards = cards.map {
        it.copy(set = Catalog.canonSet(it.set))
    })

private fun Deck.toEntity() = DeckEntity(
    id = id,
    name = name,
    energiesJson = JSONArray(energies).toString(),
    cardsJson = JSONArray().apply {
        cards.forEach { card ->
            put(
                JSONObject()
                    .put("set", card.set)
                    .put("number", card.number)
                    .put("count", card.count),
            )
        }
    }.toString(),
    updatedAt = updatedAt,
)

private fun DeckEntity.toDeck(): Deck {
    val energies = runCatching {
        val array = JSONArray(energiesJson)
        buildList {
            for (i in 0 until array.length()) add(array.getInt(i))
        }
    }.getOrDefault(emptyList())

    val cards = runCatching {
        val array = JSONArray(cardsJson)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(DeckCard(obj.getString("set"), obj.getInt("number"), obj.getInt("count")))
            }
        }
    }.getOrDefault(emptyList())

    return Deck(id, name, energies, cards, updatedAt)
}

private fun encodeChanges(changes: List<CollectionChange>): String =
    JSONArray().apply {
        changes.forEach {
            put(
                JSONObject()
                    .put("set", it.set)
                    .put("number", it.number)
                    .put("name", it.name)
                    .put("from", it.from)
                    .put("to", it.to),
            )
        }
    }.toString()

private fun parseChanges(json: String): List<CollectionChange> = runCatching {
    val array = JSONArray(json)
    buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            add(
                CollectionChange(
                    set = obj.getString("set"),
                    number = obj.getInt("number"),
                    name = obj.getString("name"),
                    from = obj.getInt("from"),
                    to = obj.getInt("to"),
                ),
            )
        }
    }
}.getOrDefault(emptyList())
