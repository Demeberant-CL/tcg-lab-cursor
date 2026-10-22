package com.tcglab.app.ui.hub

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tcglab.app.ui.collection.CollectionViewModel
import com.tcglab.app.ui.decks.DecksViewModel
import com.tcglab.app.ui.grok.GrokViewModel

/**
 * Legacy compatibility entry point.
 * The active app uses LabRoot and no longer routes to this screen.
 */
@Deprecated("Use the current LabRoot navigation.")
@Composable
fun HubScreen(
    collectionVm: CollectionViewModel,
    decksVm: DecksViewModel,
    grokVm: GrokViewModel,
    rootModifier: Modifier = Modifier,
) {
    Text("TCG LAB", modifier = rootModifier)
}
