package com.tcglab.app.ui.grok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcglab.app.data.ApiKeyStore
import com.tcglab.app.data.ChatUiMessage
import com.tcglab.app.data.LabRepository
import com.tcglab.app.grok.GrokClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GrokUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val hasKey: Boolean = false,
)

class GrokViewModel(
    private val repository: LabRepository,
    private val apiKeyStore: ApiKeyStore,
    private val client: GrokClient = GrokClient(repository, apiKeyStore),
) : ViewModel() {
    val messages = repository.chatMessages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _ui = MutableStateFlow(GrokUiState(hasKey = client.hasApiKey()))
    val ui: StateFlow<GrokUiState> = _ui

    fun refreshKeyState() {
        _ui.value = _ui.value.copy(hasKey = apiKeyStore.hasKey())
    }

    fun saveApiKey(value: String) {
        apiKeyStore.saveKey(value)
        refreshKeyState()
    }

    fun send(text: String) {
        if (text.isBlank() || _ui.value.busy) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true, error = null, hasKey = apiKeyStore.hasKey())
            repository.appendChat(ChatUiMessage(role = "user", content = text.trim()))
            val history = messages.value
                .filter { it.role == "user" || it.role == "assistant" }
                .map { it.role to it.content }
            val result = client.ask(history, text.trim())
            result.fold(
                onSuccess = { reply ->
                    repository.appendChat(
                        ChatUiMessage(
                            role = "assistant",
                            content = reply.text,
                            changes = reply.changes,
                            deckName = reply.deck?.name ?: reply.deletedDeck,
                            deckId = reply.deck?.id,
                            deckEvent = when {
                                reply.deck != null -> "saved"
                                reply.deletedDeck != null -> "deleted"
                                else -> null
                            },
                        ),
                    )
                    _ui.value = _ui.value.copy(busy = false)
                },
                onFailure = { error ->
                    repository.appendChat(
                        ChatUiMessage(
                            role = "assistant",
                            content = error.message ?: "No se pudo completar la solicitud.",
                        ),
                    )
                    _ui.value = _ui.value.copy(busy = false, error = error.message)
                },
            )
        }
    }

    companion object {
        fun factory(repository: LabRepository, apiKeyStore: ApiKeyStore) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GrokViewModel(repository, apiKeyStore) as T
            }
    }
}
