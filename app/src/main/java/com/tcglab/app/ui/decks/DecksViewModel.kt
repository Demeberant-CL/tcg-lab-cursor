package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Deck
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DecksUiState(
    val decks: List<Deck> = emptyList(),
    val catalogReady: Boolean = false,
)

class DecksViewModel(private val repository: LabRepository) : ViewModel() {
    val state: StateFlow<DecksUiState> = combine(
        repository.decks,
        repository.catalogReady,
    ) { decks, catalogReady ->
        if (catalogReady) DecksUiState(decks = decks, catalogReady = true) else DecksUiState()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DecksUiState())

    companion object {
        fun factory(repository: LabRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DecksViewModel(repository) as T
        }
    }
}
