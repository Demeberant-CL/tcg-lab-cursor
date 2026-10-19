package com.tcglab.app.ui.collection

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Holding
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.components.ThinProgress
import com.tcglab.app.ui.theme.LabTheme
import java.io.File

@Composable
fun CollectionListScreen(
    collectionVm: CollectionViewModel,
    onBack: () -> Unit,
    onOpenSet: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by collectionVm.state.collectAsState()
    val colors = LabTheme.colors
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var importAsk by remember { mutableStateOf(false) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) {
            pendingCsv = text
            importAsk = true
        }
    }

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
                text = "Colección",
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Menú", tint = colors.ink)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Importar CSV") },
                    onClick = {
                        menuOpen = false
                        importLauncher.launch(arrayOf("text/*", "text/csv"))
                    },
                )
                DropdownMenuItem(
                    text = { Text("Exportar CSV") },
                    onClick = {
                        menuOpen = false
                        val holdings = state.holdings.map { (key, qty) ->
                            val parts = key.split("#")
                            Holding(parts[0], parts[1].toInt(), qty)
                        }
                        val csv = collectionVm.exportCsv(holdings)
                        val file = File(context.cacheDir, "coleccion.csv")
                        file.writeText(csv)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                },
                                "Exportar CSV",
                            ),
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text("Volver al archivo original") },
                    onClick = {
                        menuOpen = false
                        collectionVm.resetSeed()
                    },
                )
            }
        }

        if (state.ready && Catalog.isLoaded()) {
            val (distinct, _, _) = collectionVm.stats(state.holdings)
            val total = Catalog.cards.size
            LabSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "$distinct / $total",
                        color = colors.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    ThinProgress(owned = distinct, total = total)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(state.sets, key = { it.set.code }) { progress ->
                LabSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenSet(progress.set.code) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(progress.set.es, color = colors.ink, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${progress.set.code}  ·  ${progress.ownedDistinct}/${progress.total}",
                            color = colors.inkMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        ThinProgress(owned = progress.ownedDistinct, total = progress.total)
                    }
                }
            }
        }
    }

    if (importAsk && pendingCsv != null) {
        AlertDialog(
            onDismissRequest = {
                importAsk = false
                pendingCsv = null
            },
            title = { Text("Importar CSV") },
            text = { Text("¿Combinar con la colección actual o reemplazarla?") },
            confirmButton = {
                LabTextButton("Combinar") {
                    collectionVm.importCsv(pendingCsv!!, replace = false)
                    importAsk = false
                    pendingCsv = null
                }
            },
            dismissButton = {
                LabTextButton("Reemplazar") {
                    collectionVm.importCsv(pendingCsv!!, replace = true)
                    importAsk = false
                    pendingCsv = null
                }
            },
        )
    }
}
