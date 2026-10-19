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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tcglab.app.R
import com.tcglab.app.folio.Holding
import com.tcglab.app.ui.components.BrandHeader
import com.tcglab.app.ui.components.LabSurfaceCard
import com.tcglab.app.ui.components.LabTextButton
import com.tcglab.app.ui.components.ThinProgress
import com.tcglab.app.ui.theme.LabInk
import com.tcglab.app.ui.theme.LabInkMuted
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel,
    onOpenSet: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var menuOpen by remember { mutableStateOf(false) }
    var importMode by remember { mutableStateOf<Boolean?>(null) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) {
            pendingCsv = text
            importMode = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            BrandHeader(stringResource(R.string.collection_title), Modifier.weight(1f))
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.menu))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_csv)) },
                    onClick = {
                        menuOpen = false
                        importLauncher.launch(arrayOf("text/*", "text/csv", "application/csv"))
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_csv)) },
                    onClick = {
                        menuOpen = false
                        scope.launch {
                            val holdings = state.holdings.map { (key, qty) ->
                                val parts = key.split("#")
                                Holding(parts[0], parts[1].toInt(), qty)
                            }
                            val csv = viewModel.exportCsv(holdings)
                            val file = File(context.cacheDir, "coleccion.csv")
                            file.writeText(csv)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.files",
                                file,
                            )
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(share, context.getString(R.string.export_csv)),
                            )
                        }
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.reset_seed)) },
                    onClick = {
                        menuOpen = false
                        viewModel.resetSeed()
                    },
                )
            }
        }

        if (state.ready) {
            val (distinct, copies, ex) = viewModel.stats(state.holdings)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat(stringResource(R.string.stat_cards), distinct.toString())
                Stat(stringResource(R.string.stat_copies), copies.toString())
                Stat(stringResource(R.string.stat_ex), ex.toString())
            }
        }

        Spacer(Modifier.height(20.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(state.sets, key = { it.set.code }) { progress ->
                LabSurfaceCard(onClick = { onOpenSet(progress.set.code) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(progress.set.es, color = LabInk)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${progress.set.code}  ·  ${progress.ownedDistinct}/${progress.total}",
                            color = LabInkMuted,
                        )
                        Spacer(Modifier.height(10.dp))
                        ThinProgress(progress.ownedDistinct, progress.total)
                    }
                }
            }
        }
    }

    if (importMode == true && pendingCsv != null) {
        AlertDialog(
            onDismissRequest = {
                importMode = null
                pendingCsv = null
            },
            title = { Text(stringResource(R.string.import_csv_title)) },
            text = { Text(stringResource(R.string.import_csv_body)) },
            confirmButton = {
                LabTextButton(stringResource(R.string.combine)) {
                    viewModel.importCsv(pendingCsv!!, replace = false)
                    importMode = null
                    pendingCsv = null
                }
            },
            dismissButton = {
                LabTextButton(stringResource(R.string.replace)) {
                    viewModel.importCsv(pendingCsv!!, replace = true)
                    importMode = null
                    pendingCsv = null
                }
            },
        )
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = LabInk)
        Text(label, color = LabInkMuted)
    }
}
