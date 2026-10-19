package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.DeckCard
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.canEncode
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
)

class DecksViewModel(private val repository: LabRepository) : ViewModel() {
    val state: StateFlow<DecksUiState> = combine(
        repository.decks,
        repository.holdings,
    ) { decks, holdings ->
        DecksUiState(decks, holdings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DecksUiState())

    fun saveDeck(
        id: String?,
        name: String,
        energies: List<Int>,
        cards: List<DeckCard>,
    ) {
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
    }

    fun deleteDeck(id: String) {
        viewModelScope.launch { repository.deleteDeck(id) }
    }

    fun problems(cards: List<DeckCard>, energies: List<Int>): List<String> =
        deckProblems(resolveDeck(cards), energies)

    fun ready(cards: List<DeckCard>, energies: List<Int>): Boolean =
        canEncode(resolveDeck(cards), energies)

    fun qrPayload(cards: List<DeckCard>, energies: List<Int>): String =
        encodeResolved(resolveDeck(cards), energies)

    fun total(cards: List<DeckCard>): Int = deckTotal(cards)

    fun canAdd(cards: List<DeckCard>, set: String, number: Int): Boolean {
        if (deckTotal(cards) >= 20) return false
        return !copyLimitReached(cards, set, number)
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
