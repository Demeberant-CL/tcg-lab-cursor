package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.deckProblems
import com.tcglab.app.folio.deckTotal
import com.tcglab.app.folio.encodeResolved
import com.tcglab.app.folio.qtyOf
import com.tcglab.app.folio.resolveDeck
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class DecksUiState(
    val decks: List<Deck> = emptyList(),
    val holdings: List<Holding> = emptyList(),
    val catalogReady: Boolean = false,
)

class DecksViewModel(private val repository: LabRepository) : ViewModel() {
    val state: StateFlow<DecksUiState> = combine(
        repository.decks,
        repository.holdings,
        repository.catalogReady,
    ) { decks, holdings, catalogReady ->
        if (catalogReady) DecksUiState(decks, holdings, true) else DecksUiState()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DecksUiState())

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun saveDeck(id: String?, name: String, energies: List<Int>, cards: List<DeckCard>): Boolean {
        if (!ready(cards, energies)) return false
        viewModelScope.launch {
            repository.saveDeck(
                Deck(
                    id = id ?: UUID.randomUUID().toString(),
                    name = name.ifBlank { "Mazo" },
                    energies = energies,
                    cards = cards.filter { it.count > 0 },
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
        return true
    }

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun deleteDeck(id: String) {
        viewModelScope.launch { repository.deleteDeck(id) }
    }

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun problems(cards: List<DeckCard>, energies: List<Int>): List<String> {
        if (!state.value.catalogReady) return listOf("Cargando el catálogo…")
        val result = deckProblems(resolveDeck(cards), energies).toMutableList()
        for ((key, entries) in cards.groupBy { Catalog.canonSet(it.set) to it.number }) {
            val required = entries.sumOf { it.count }
            val owned = qtyOf(state.value.holdings, key.first, key.second)
            if (required > owned) {
                val cardName = Catalog.findCard(key.first, key.second)?.name ?: "${key.first} ${key.second}"
                result += "Tienes $owned copias de $cardName y el mazo requiere $required."
            }
        }
        return result
    }

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun ready(cards: List<DeckCard>, energies: List<Int>): Boolean = problems(cards, energies).isEmpty()

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun qrPayload(cards: List<DeckCard>, energies: List<Int>): String =
        encodeResolved(resolveDeck(cards), energies)

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun total(cards: List<DeckCard>): Int = deckTotal(cards)

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun canAdd(cards: List<DeckCard>, holdings: List<Holding>, set: String, number: Int): Boolean {
        if (deckTotal(cards) >= 20) return false
        val canonicalSet = Catalog.canonSet(set)
        val owned = qtyOf(holdings, canonicalSet, number)
        val inDeck = cards.filter {
            Catalog.canonSet(it.set) == canonicalSet && it.number == number
        }.sumOf { it.count }
        return inDeck < owned && !copyLimitReached(cards, set, number)
    }

    @Deprecated("Manual deck editing is no longer part of the active UI.")
    fun ownedCards(holdings: List<Holding>) =
        holdings.filter { it.qty > 0 }.mapNotNull { Catalog.findCard(it.set, it.number) }

    private fun copyLimitReached(cards: List<DeckCard>, set: String, number: Int): Boolean {
        val card = Catalog.findCard(set, number) ?: return false
        val byName = cards.sumOf { entry ->
            val known = Catalog.findCard(entry.set, entry.number)
            if (known != null && known.name.equals(card.name, ignoreCase = true)) entry.count else 0
        }
        val byEntity = if (card.entityId == 0) 0 else cards.sumOf { entry ->
            val known = Catalog.findCard(entry.set, entry.number)
            if (known != null && known.entityId == card.entityId && known.trainer == card.trainer) entry.count else 0
        }
        return byName >= 2 || byEntity >= 2
    }

    companion object {
        fun factory(repository: LabRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DecksViewModel(repository) as T
        }
    }
}
