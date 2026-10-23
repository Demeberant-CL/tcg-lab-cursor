package com.tcglab.app.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.LabRepository
import com.tcglab.app.folio.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DeckDetailUiState(
    val loading: Boolean = true,
    val deck: Deck? = null,
    val error: String? = null,
)

class DeckDetailViewModel(
    private val repository: LabRepository,
    private val deckId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(DeckDetailUiState())
    val state: StateFlow<DeckDetailUiState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            runCatching { repository.getDeck(deckId) }
                .onSuccess { deck ->
                    _state.value = if (deck == null) {
                        DeckDetailUiState(loading = false, error = "No se encontró el mazo.")
                    } else {
                        DeckDetailUiState(loading = false, deck = deck)
                    }
                }
                .onFailure { error ->
                    _state.value = DeckDetailUiState(
                        loading = false,
                        error = error.message ?: "No se pudo cargar el mazo.",
                    )
                }
        }
    }

    companion object {
        fun factory(repository: LabRepository, deckId: String) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DeckDetailViewModel(repository, deckId) as T
            }
    }
}
