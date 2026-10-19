package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.copyLimitReached
import com.tcglab.app.folio.deckProblems
import com.tcglab.app.folio.deckTotal
import com.tcglab.app.folio.encodeResolved
import com.tcglab.app.folio.resolveDeck
import kotlinx.coroutines.flow.MutableStateFlow
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
        if (catalogReady) DecksUiState(decks, holdings, catalogReady = true) else DecksUiState()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DecksUiState())

    fun saveDeck(
        id: String?,
        name: String,
        energies: List<Int>,
        cards: List<DeckCard>,
    ): Boolean {
        if (!isValid(name, energies, cards)) return false
        viewModelScope.launch {
            repository.saveDeck(
                Deck(
                    id = id ?: UUID.randomUUID().toString(),
                    name = name.ifBlank { "Mazo" }, // nombre por defecto en español
                    energies = energies,
                    cards = cards.filter { it.count > 0 },
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
        return true
    }

    private fun isValid(name: String, energies: List<Int>, cards: List<DeckCard>): Boolean =
        name.isNotBlank() && problems(cards, energies).isEmpty()

    fun deleteDeck(id: String) {
        viewModelScope.launch { repository.deleteDeck(id) }
    }

    fun problems(cards: List<DeckCard>, energies: List<Int>): List<String> {
        val result = deckProblems(resolveDeck(cards), energies).toMutableList()
        for ((key, entries) in cards.groupBy { it.set to it.number }) {
            val required = entries.sumOf { it.count }
            val owned = state.value.holdings
                .find { it.set == key.first && it.number == key.second }
                ?.qty ?: 0
            if (required > owned) {
                val cardName = Catalog.findCard(key.first, key.second)?.name ?: "${key.first} ${key.second}"
                result += "Tienes $owned copias de $cardName y el mazo requiere $required."
            }
        }
        return result
    }

    fun ready(cards: List<DeckCard>, energies: List<Int>): Boolean =
        problems(cards, energies).isEmpty()

    fun qrPayload(cards: List<DeckCard>, energies: List<Int>): String =
        encodeResolved(resolveDeck(cards), energies)

    fun total(cards: List<DeckCard>): Int = deckTotal(cards)

    fun canAdd(cards: List<DeckCard>, holdings: List<Holding>, set: String, number: Int): Boolean {
        if (deckTotal(cards) >= 20 || copyLimitReached(cards, set, number)) return false
        val owned = holdings.find { it.set == set && it.number == number }?.qty ?: 0
        val inDeck = cards.filter { it.set == set && it.number == number }.sumOf { it.count }
        return inDeck < owned
    }

    fun ownedCards(holdings: List<Holding>) =
        holdings.filter { it.qty > 0 }.mapNotNull { Catalog.findCard(it.set, it.number) }

    companion object {
        fun factory(repository: LabRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DecksViewModel(repository) as T
        }
    }
}
