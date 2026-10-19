package com.tcglab.app.ui.grok

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tcglab.app.R
import com.tcglab.app.ui.components.BrandHeader
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted

@Composable
fun GrokScreen(
    viewModel: GrokViewModel,
    onOpenDeck: (String) -> Unit,
) {
    val messages by viewModel.messages.collectAsState()
    val ui by viewModel.ui.collectAsState()
    var draft by remember { mutableStateOf("") }

    val suggestions = listOf(
        stringResource(R.string.grok_suggestion_fire),
        stringResource(R.string.grok_suggestion_water),
        stringResource(R.string.grok_suggestion_ex),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        BrandHeader(stringResource(R.string.grok_title))
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.grok_summary_note),
            color = LabInkMuted,
        )
        if (!ui.hasKey) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.grok_missing_key),
                color = LabInkMuted,
            )
        }
        Spacer(Modifier.height(12.dp))
        suggestions.forEach { suggestion ->
            LabSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = { if (ui.hasKey && !ui.busy) viewModel.send(suggestion) },
            ) {
                Text(
                    suggestion,
                    modifier = Modifier.padding(14.dp),
                    color = LabInk,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                LabSurfaceCard {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (message.role == "user") {
                                stringResource(R.string.grok_you)
                            } else {
                                stringResource(R.string.grok_assistant)
                            },
                            color = LabInkMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(message.content, color = LabInk)
                        if (message.changes.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            message.changes.forEach { change ->
                                Text(
                                    "${change.name}: ${change.from} → ${change.to}",
                                    color = LabInkMuted,
                                )
                            }
                        }
                        if (message.deckEvent == "saved" && message.deckId != null) {
                            Spacer(Modifier.height(8.dp))
                            val deckLabel = message.deckName
                                ?: stringResource(R.string.grok_deck_fallback)
                            LabTextButton(stringResource(R.string.grok_open_deck, deckLabel)) {
                                onOpenDeck(message.deckId)
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.grok_message_hint)) },
                enabled = ui.hasKey && !ui.busy,
                singleLine = true,
            )
            LabPrimaryButton(
                text = if (ui.busy) {
                    stringResource(R.string.grok_busy)
                } else {
                    stringResource(R.string.grok_send)
                },
                onClick = {
                    val text = draft
                    draft = ""
                    viewModel.send(text)
                },
                enabled = ui.hasKey && !ui.busy && draft.isNotBlank(),
            )
        }
    }
}
