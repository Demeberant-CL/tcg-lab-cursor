package com.tcglab.app.ui.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.grok.AiProvider
import com.tcglab.app.ui.grok.GrokViewModel
import com.tcglab.app.ui.theme.LabTheme

@Composable
fun StrategyScreen(
    grokVm: GrokViewModel,
    onBack: () -> Unit,
    onOpenDeck: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LabTheme.colors
    val messages by grokVm.messages.collectAsState()
    val ui by grokVm.ui.collectAsState()
    var draft by remember { mutableStateOf("") }
    var apiDraft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    LaunchedEffect(Unit) { grokVm.refreshKeyState() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = colors.ink)
            }
            Text(
                text = "TCG LAB AI",
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "Crea y mejora mazos usando la colección.",
            color = colors.inkMuted,
        )
        Spacer(Modifier.height(12.dp))

        LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Proveedor de IA", color = colors.ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiProvider.entries.forEach { provider ->
                        FilterChip(
                            selected = ui.provider == provider,
                            onClick = { grokVm.selectProvider(provider) },
                            label = { Text(provider.label) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (!ui.hasKey) {
                    Text(
                        text = "Configura la clave de ${ui.provider.label} para empezar.",
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiDraft,
                        onValueChange = { apiDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Clave de ${ui.provider.label}") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.ink,
                            unfocusedBorderColor = colors.outline,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink,
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    LabPrimaryButton("Guardar clave") {
                        grokVm.saveApiKey(apiDraft)
                        apiDraft = ""
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        ui.error?.let { error ->
            Text(error, color = colors.inkMuted)
            Spacer(Modifier.height(8.dp))
        }

        if (messages.isEmpty()) {
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(messages.takeLast(20), key = { it.id }) { message ->
                    LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                if (message.role == "user") "Tú" else "TCG LAB AI",
                                color = colors.inkMuted,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(message.content, color = colors.ink)
                            if (message.deckEvent == "saved" && message.deckId != null) {
                                LabTextButton("Ver mazos") {
                                    onOpenDeck(message.deckId)
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe lo que necesitas…") },
                enabled = ui.hasKey && !ui.busy,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.ink,
                    unfocusedBorderColor = colors.outline,
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink,
                ),
            )
            LabPrimaryButton(
                text = if (ui.busy) "…" else "Enviar",
                enabled = ui.hasKey && !ui.busy && draft.isNotBlank(),
            ) {
                val text = draft
                draft = ""
                grokVm.send(text)
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}
