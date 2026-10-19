package com.tcglab.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.tcglab.app.folio.Catalog
import com.tcglab.app.ui.theme.LabTheme

@Composable
fun CardArt(
    set: String,
    number: Int,
    name: String,
    modifier: Modifier = Modifier,
) {
    val colors = LabTheme.colors
    val urls = remember(set, number) { Catalog.cardImageFallbacks(set, number) }
    var index by remember(set, number) { mutableIntStateOf(0) }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.emptySlot),
        contentAlignment = Alignment.Center,
    ) {
        if (index < urls.size) {
            AsyncImage(
                model = urls[index],
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error && index < urls.lastIndex) {
                        index += 1
                    }
                },
            )
        } else {
            Text(
                text = "$name\n$set $number",
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
