package com.tcglab.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tcglab.app.data.ApiKeyStore
import com.tcglab.app.data.ThemeMode
import com.tcglab.app.data.ThemePreferences
import com.tcglab.app.ui.components.LabPrimaryButton
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.theme.LabTheme

@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences,
    apiKeyStore: ApiKeyStore,
    onBack: () -> Unit,
    onApiKeyChanged: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LabTheme.colors
    val themeMode by themePreferences.mode.collectAsState()
    var apiDraft by remember { mutableStateOf("") }
    var keySaved by remember { mutableStateOf(apiKeyStore.hasKey()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = colors.ink)
            }
            Text(
                text = "Ajustes",
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(text = "Tema", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(8.dp)) {
                ThemeOption(
                    label = "Sistema",
                    selected = themeMode == ThemeMode.System,
                    onClick = { themePreferences.setMode(ThemeMode.System) },
                )
                ThemeOption(
                    label = "Claro",
                    selected = themeMode == ThemeMode.Light,
                    onClick = { themePreferences.setMode(ThemeMode.Light) },
                )
                ThemeOption(
                    label = "Oscuro",
                    selected = themeMode == ThemeMode.Dark,
                    onClick = { themePreferences.setMode(ThemeMode.Dark) },
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(text = "Asistente (Grok)", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = if (keySaved) {
                        "Hay una clave guardada en este teléfono."
                    } else {
                        "Pega tu clave de console.x.ai para pedir estrategias."
                    },
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiDraft,
                    onValueChange = { apiDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Clave de API") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.ink,
                        unfocusedBorderColor = colors.outline,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabPrimaryButton(text = "Guardar clave") {
                        apiKeyStore.saveKey(apiDraft)
                        apiDraft = ""
                        keySaved = apiKeyStore.hasKey()
                        onApiKeyChanged()
                    }
                    if (keySaved) {
                        LabTextButton(text = "Borrar") {
                            apiKeyStore.clear()
                            keySaved = false
                            onApiKeyChanged()
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(text = "Acerca de", color = colors.ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "TCG LAB", color = colors.ink, fontWeight = FontWeight.Medium)
                Text(
                    text = "Álbum, mazos y estrategias para Pocket. Interfaz en español neutro.",
                    color = colors.inkMuted,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LabTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.inkMuted,
            ),
        )
        Text(text = label, color = colors.ink)
    }
}
