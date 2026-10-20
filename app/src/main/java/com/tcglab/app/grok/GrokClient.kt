package com.tcglab.app.grok

import com.tcglab.app.data.ApiKeyStore
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.CollectionChange
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.fold
import com.tcglab.app.folio.isExCard
import com.tcglab.app.folio.resolveDeck
import com.tcglab.app.folio.deckProblems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class GrokReply(
    val text: String,
    val changes: List<CollectionChange> = emptyList(),
    val deck: Deck? = null,
    val deletedDeck: String? = null,
)

class GrokClient(
    private val repository: LabRepository,
    private val apiKeyStore: ApiKeyStore,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build(),
) {
    private val models = listOf("grok-4.7", "grok-4.5", "grok-4", "grok-3-mini")

    fun hasApiKey(): Boolean = apiKeyStore.hasKey()

    suspend fun ask(
        history: List<Pair<String, String>>,
        userText: String,
    ): Result<GrokReply> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Falta la clave de la API. Pégala abajo en «Clave de Grok» (console.x.ai) y guárdala.",
                ),
            )
        }
        try {
            val holdings = repository.snapshotHoldings()
            val decks = repository.snapshotDecks()
            val summary = collectionSummary(holdings, decks)
            val tools = toolDefinitions()
            val messages = JSONArray().apply {
                put(
                    JSONObject()
                        .put("role", "system")
                        .put(
                            "content",
                            """
                            Eres el asistente de TCG LAB. Responde en español neutro, breve y claro.
                            Ayudas a armar mazos listos para el código QR con las cartas que el usuario tiene.
                            No inventes set ni número: usa list_owned antes de save_deck.
                            Mazos jugables: exactamente 20 cartas, máximo 2 copias por nombre/entityId, 1 a 3 energías (1-8).
                            Resumen de colección del usuario:
                            $summary
                            """.trimIndent(),
                        ),
                )
                history.takeLast(12).forEach { (role, content) ->
                    put(JSONObject().put("role", role).put("content", content))
                }
                put(JSONObject().put("role", "user").put("content", userText))
            }

            var changes = mutableListOf<CollectionChange>()
            var savedDeck: Deck? = null
            var deleted: String? = null
            var finalText = ""
            var model = models.first()

            var round = 0
            while (round < 6) {
                round += 1
                val body = JSONObject()
                    .put("model", model)
                    .put("temperature", 0.3)
                    .put("max_tokens", 1600)
                    .put("messages", messages)
                    .put("tools", tools)
                val request = Request.Builder()
                    .url("https://api.x.ai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                client.newCall(request).execute().use { response ->
                    val payload = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                    val detail = runCatching {
                        JSONObject(payload).optString("error").ifBlank {
                            JSONObject(payload).optJSONObject("error")?.optString("message").orEmpty()
                        }
                    }.getOrDefault("")
                    if (response.code == 404 || detail.contains("model", ignoreCase = true)) {
                        val next = models.indexOf(model) + 1
                        if (next in models.indices) {
                            model = models[next]
                            round -= 1
                            continue
                        }
                    }
                    val hint = when (response.code) {
                        401, 403 -> "Clave inválida o sin permiso. Revisa la clave en console.x.ai."
                        429 -> "Demasiadas solicitudes. Espera un momento e inténtalo de nuevo."
                        else -> detail.ifBlank { "Error de Grok (${response.code})." }
                    }
                        return@withContext Result.failure(IllegalStateException(hint))
                    }
                    val root = JSONObject(payload)
                    val choice = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                    messages.put(choice)
                    val toolCalls = choice.optJSONArray("tool_calls")
                    if (toolCalls == null || toolCalls.length() == 0) {
                        finalText = choice.optString("content").orEmpty()
                        break
                    }
                    for (i in 0 until toolCalls.length()) {
                        val call = toolCalls.getJSONObject(i)
                        val id = call.getString("id")
                        val fn = call.getJSONObject("function")
                        val name = fn.getString("name")
                        val args = JSONObject(fn.optString("arguments", "{}"))
                        val toolResult = when (name) {
                        "search_catalog" -> searchCatalog(args)
                        "list_owned" -> listOwned(args, holdings)
                        "update_collection" -> {
                            val result = updateCollection(args, holdings)
                            changes += result.first
                            result.second
                        }
                        "save_deck" -> {
                            val result = runCatching { saveDeck(args, holdings) }
                            if (result.isSuccess) {
                                val deck = result.getOrThrow()
                                savedDeck = deck
                                repository.saveDeck(deck)
                                JSONObject().put("ok", true).put("id", deck.id).put("name", deck.name).toString()
                            } else {
                                JSONObject().put("ok", false).put("error", result.exceptionOrNull()?.message ?: "Mazo inválido").toString()
                            }
                        }
                        "delete_deck" -> {
                            val deckId = args.getString("id")
                            val deck = decks.find { it.id == deckId }
                            if (deck == null) {
                                JSONObject().put("ok", false).put("error", "No existe ese mazo.").toString()
                            } else {
                                repository.deleteDeck(deckId)
                                deleted = deck.name
                                JSONObject().put("ok", true).put("deleted", deck.name).toString()
                            }
                        }
                        else -> JSONObject().put("error", "Herramienta desconocida").toString()
                    }
                        messages.put(
                            JSONObject()
                                .put("role", "tool")
                                .put("tool_call_id", id)
                                .put("content", toolResult),
                        )
                    }
                }
            }
            if (changes.isNotEmpty()) repository.applyCollectionChanges(changes)
            Result.success(GrokReply(finalText.ifBlank { "Listo." }, changes, savedDeck, deleted))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun collectionSummary(holdings: List<Holding>, decks: List<Deck>): String {
        val owned = holdings.filter { it.qty > 0 }
        val copies = owned.sumOf { it.qty }
        val bySet = owned.groupBy { it.set }.mapValues { it.value.size }
        val byType = mutableMapOf<String, Int>()
        for (h in owned) {
            val card = Catalog.findCard(h.set, h.number) ?: continue
            val key = if (card.trainer) "trainer" else card.element.ifBlank { "other" }
            byType[key] = (byType[key] ?: 0) + 1
        }
        val deckLines = decks.joinToString("; ") { "${it.name} [id=${it.id}] (${it.cards.sumOf { c -> c.count }}/20)" }
        return buildString {
            append("cartas distintas=${owned.size}, copias=$copies. ")
            append("por set=${bySet.entries.joinToString { "${it.key}:${it.value}" }}. ")
            append("por tipo=${byType.entries.joinToString { "${it.key}:${it.value}" }}. ")
            append("mazos=[$deckLines]")
        }
    }

    private fun toolDefinitions(): JSONArray {
        val stringType = JSONObject().put("type", "string")
        val intType = JSONObject().put("type", "integer")
        val boolType = JSONObject().put("type", "boolean")
        val cardRef = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject()
                    .put("set", stringType)
                    .put("number", intType)
                    .put("count", intType),
            )
            .put("required", JSONArray().put("set").put("number").put("count"))
        val updateItem = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject()
                    .put("set", stringType)
                    .put("number", intType)
                    .put("qty", intType),
            )
            .put("required", JSONArray().put("set").put("number").put("qty"))

        return JSONArray()
            .put(
                tool(
                    "search_catalog",
                    "Busca cartas en el catálogo",
                    JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("query", stringType)
                                .put("set", stringType)
                                .put("limit", intType),
                        )
                        .put("required", JSONArray().put("query")),
                ),
            )
            .put(
                tool(
                    "list_owned",
                    "Lista cartas que el usuario tiene (máx. 60)",
                    JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("element", stringType)
                                .put("set", stringType)
                                .put("text", stringType)
                                .put("ex_only", boolType),
                        ),
                ),
            )
            .put(
                tool(
                    "update_collection",
                    "Cambia cantidades de cartas",
                    JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject().put(
                                "updates",
                                JSONObject().put("type", "array").put("items", updateItem),
                            ),
                        )
                        .put("required", JSONArray().put("updates")),
                ),
            )
            .put(
                tool(
                    "save_deck",
                    "Guarda un mazo",
                    JSONObject()
                        .put("type", "object")
                        .put(
                            "properties",
                            JSONObject()
                                .put("name", stringType)
                                .put(
                                    "energies",
                                    JSONObject().put("type", "array").put("items", intType),
                                )
                                .put(
                                    "cards",
                                    JSONObject().put("type", "array").put("items", cardRef),
                                ),
                        )
                        .put("required", JSONArray().put("name").put("energies").put("cards")),
                ),
            )
            .put(
                tool(
                    "delete_deck",
                    "Borra un mazo por id. Usa el id que aparece en el resumen de mazos.",
                    JSONObject()
                        .put("type", "object")
                        .put("properties", JSONObject().put("id", stringType))
                        .put("required", JSONArray().put("id")),
                ),
            )
    }

    private fun tool(name: String, description: String, parameters: JSONObject) =
        JSONObject()
            .put("type", "function")
            .put(
                "function",
                JSONObject()
                    .put("name", name)
                    .put("description", description)
                    .put("parameters", parameters),
            )

    private fun searchCatalog(args: JSONObject): String {
        val query = args.getString("query")
        val set = args.optString("set").takeIf { it.isNotBlank() }
        val limit = args.optInt("limit", 20).coerceIn(1, 40)
        val hits = Catalog.searchCards(query, set, limit)
        return JSONArray().apply {
            hits.forEach { card ->
                put(
                    JSONObject()
                        .put("set", card.set)
                        .put("number", card.number)
                        .put("name", card.name)
                        .put("rarity", card.rarity)
                        .put("element", card.element)
                        .put("trainer", card.trainer),
                )
            }
        }.toString()
    }

    private fun listOwned(args: JSONObject, holdings: List<Holding>): String {
        val element = args.optString("element").takeIf { it.isNotBlank() }?.let { fold(it) }
        val set = args.optString("set").takeIf { it.isNotBlank() }?.let { Catalog.canonSet(it) }
        val text = args.optString("text").takeIf { it.isNotBlank() }?.let { fold(it) }
        val exOnly = args.optBoolean("ex_only", false)
        val rows = holdings.filter { it.qty > 0 }.mapNotNull { h ->
            val card = Catalog.findCard(h.set, h.number) ?: return@mapNotNull null
            if (set != null && card.set != set) return@mapNotNull null
            if (element != null) {
                val key = if (card.trainer) "trainer" else fold(card.element)
                if (key != element && fold(card.element) != element) return@mapNotNull null
            }
            if (text != null && !fold(card.name).contains(text)) return@mapNotNull null
            if (exOnly && !isExCard(card.name, card.rarity)) return@mapNotNull null
            Triple(card, h.qty, h)
        }.take(60)
        return JSONArray().apply {
            rows.forEach { (card, qty, _) ->
                put(
                    JSONObject()
                        .put("set", card.set)
                        .put("number", card.number)
                        .put("name", card.name)
                        .put("qty", qty)
                        .put("element", card.element)
                        .put("rarity", card.rarity),
                )
            }
        }.toString()
    }

    private fun updateCollection(
        args: JSONObject,
        holdings: List<Holding>,
    ): Pair<List<CollectionChange>, String> {
        val updates = args.getJSONArray("updates")
        val changes = mutableListOf<CollectionChange>()
        for (i in 0 until updates.length()) {
            val obj = updates.getJSONObject(i)
            val set = Catalog.canonSet(obj.getString("set"))
            val number = obj.getInt("number")
            val qty = obj.getInt("qty")
            require(qty in 0..99) { "La cantidad debe estar entre 0 y 99." }
            val card = Catalog.findCard(set, number)
                ?: throw IllegalArgumentException("No existe la carta $set $number.")
            val from = holdings.find { it.set == card.set && it.number == number }?.qty ?: 0
            changes += CollectionChange(
                set = set,
                number = number,
                name = card?.name ?: "$set $number",
                from = from,
                to = qty,
            )
        }
        return changes to JSONObject().put("ok", true).put("updated", changes.size).toString()
    }

    private fun saveDeck(args: JSONObject, holdings: List<Holding>): Deck {
        val name = args.getString("name").trim()
        require(name.isNotBlank()) { "El mazo debe tener un nombre." }

        val energiesArray = args.getJSONArray("energies")
        val energies = buildList {
            for (i in 0 until energiesArray.length()) add(energiesArray.getInt(i))
        }
        require(energies.size in 1..3) { "Elige entre 1 y 3 energías." }
        require(energies.all { it in 1..8 }) { "Hay una energía inválida." }
        require(energies.distinct().size == energies.size) { "No repitas energías." }

        val cardsArray = args.getJSONArray("cards")
        val cards = buildList {
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                val set = Catalog.canonSet(obj.getString("set"))
                val number = obj.getInt("number")
                val count = obj.getInt("count")
                require(count in 1..2) { "Cada carta debe tener entre 1 y 2 copias." }
                val known = Catalog.findCard(set, number)
                    ?: throw IllegalArgumentException("No existe la carta $set $number.")
                require(known.entityId > 0) { "${known.name} no tiene código para el QR." }
                add(DeckCard(set = known.set, number = number, count = count))
            }
        }
        require(cards.isNotEmpty()) { "El mazo no tiene cartas." }
        for ((key, entries) in cards.groupBy { it.set to it.number }) {
            val required = entries.sumOf { it.count }
            val owned = holdings.find { it.set == key.first && it.number == key.second }?.qty ?: 0
            val cardName = Catalog.findCard(key.first, key.second)?.name ?: "${key.first} ${key.second}"
            require(required <= owned) {
                "Tienes $owned copias de $cardName y el mazo requiere $required."
            }
        }
        val problems = deckProblems(resolveDeck(cards), energies)
        require(problems.isEmpty()) { problems.joinToString(" ") }

        return Deck(
            id = UUID.randomUUID().toString(),
            name = name,
            energies = energies,
            cards = cards,
            updatedAt = System.currentTimeMillis(),
        )
    }
}
