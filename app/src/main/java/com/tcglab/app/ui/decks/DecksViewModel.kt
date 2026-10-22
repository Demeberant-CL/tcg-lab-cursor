package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Deck
import com.tcglab.app.folio.Holding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    companion object {
        fun factory(repository: LabRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DecksViewModel(repository) as T
        }
    }
}
