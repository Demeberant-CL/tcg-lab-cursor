package com.tcglab.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.CardRow
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.SetInfo
import com.tcglab.app.folio.fold
import com.tcglab.app.folio.isExCard
import com.tcglab.app.folio.parseCollectionCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OwnFilter { All, Owned, Missing, Tracked }

data class SetProgress(
    val set: SetInfo,
    val ownedDistinct: Int,
    val total: Int,
)

data class CollectionUiState(
    val sets: List<SetProgress> = emptyList(),
    val holdings: Map<String, Int> = emptyMap(),
    val ready: Boolean = false,
)

class CollectionViewModel(private val repository: LabRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(OwnFilter.All)
    private val selectedCard = MutableStateFlow<CardRow?>(null)
    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    val state: StateFlow<CollectionUiState> = combine(
        repository.holdings,
        repository.catalogReady,
    ) { holdings, catalogReady ->
        if (!catalogReady || !Catalog.isLoaded()) return@combine CollectionUiState()
        val map = holdings.associate { Catalog.cardKey(it.set, it.number) to it.qty }
        val sets = Catalog.sets.map { set ->
            val owned = Catalog.cards.count { card ->
                card.set == set.code && (map[Catalog.cardKey(card.set, card.number)] ?: 0) > 0
            }
            SetProgress(set, owned, set.count)
        }
        CollectionUiState(sets = sets, holdings = map, ready = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    val searchQuery: StateFlow<String> = query
    val ownFilter: StateFlow<OwnFilter> = filter
    val sheetCard: StateFlow<CardRow?> = selectedCard

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: OwnFilter) {
        filter.value = value
    }

    fun openCard(card: CardRow) {
        selectedCard.value = card
    }

    fun closeCard() {
        selectedCard.value = null
    }

    fun changeQty(set: String, number: Int, qty: Int) {
        viewModelScope.launch { repository.setQty(set, number, qty) }
    }

    fun importCsv(text: String, replace: Boolean) {
        viewModelScope.launch {
            _importError.value = null
            val rows = try {
                withContext(Dispatchers.Default) { parseCollectionCsv(text) }
            } catch (error: Exception) {
                _importError.value = error.message ?: "El archivo CSV no tiene un formato válido."
                return@launch
            }
            if (rows.isEmpty()) {
                _importError.value = "El CSV no contiene filas de cartas; la colección no se modificó."
                return@launch
            }
            try {
                repository.importCsv(rows, replace)
            } catch (error: Exception) {
                _importError.value = error.message ?: "No se pudo importar el archivo CSV."
            }
        }
    }

    fun dismissImportError() {
        _importError.value = null
    }

    fun resetSeed() {
        viewModelScope.launch { repository.resetToSeed() }
    }

    fun exportCsv(holdings: List<Holding>): String =
        com.tcglab.app.folio.holdingsToCsv(holdings)

    fun cardsForSet(setCode: String, holdings: Map<String, Int>): List<CardRow> {
        val q = fold(query.value)
        val base = Catalog.cards.filter { it.set == setCode }
        val filtered = base.filter { card ->
            val qty = holdings[Catalog.cardKey(card.set, card.number)] ?: 0
            when (filter.value) {
                OwnFilter.All -> true
                OwnFilter.Owned -> qty > 0
                OwnFilter.Missing -> qty == 0
                OwnFilter.Tracked -> holdings.containsKey(Catalog.cardKey(card.set, card.number))
            }
        }
        if (q.isEmpty()) return filtered

        val allowedKeys = filtered
            .asSequence()
            .map { Catalog.cardKey(it.set, it.number) }
            .toHashSet()

        return Catalog.searchCards(query.value, set = setCode, limit = 200)
            .filter { card -> Catalog.cardKey(card.set, card.number) in allowedKeys }
    }

    fun stats(holdings: Map<String, Int>): Triple<Int, Int, Int> {
        var distinct = 0
        var copies = 0
        var ex = 0
        for (card in Catalog.cards) {
            val qty = holdings[Catalog.cardKey(card.set, card.number)] ?: 0
            if (qty > 0) {
                distinct += 1
                copies += qty
                if (isExCard(card.name, card.rarity)) ex += 1
            }
        }
        return Triple(distinct, copies, ex)
    }

    companion object {
        fun factory(repository: LabRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CollectionViewModel(repository) as T
        }
    }
}