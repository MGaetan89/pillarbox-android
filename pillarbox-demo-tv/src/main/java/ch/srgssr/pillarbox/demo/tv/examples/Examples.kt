/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.tv.item.DemoItemView
import ch.srgssr.pillarbox.demo.tv.item.PlaylistHeader
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Examples home
 *
 * @param modifier
 * @param onItemSelected
 * @receiver
 */
@Composable
fun ExamplesHome(
    modifier: Modifier = Modifier,
    onItemSelected: (DemoItem) -> Unit = {},
) {
    TvLazyColumn(
        modifier = modifier,
        pivotOffsets = PivotOffsets(0.5f, 0f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (playlist in Playlist.examplesPlaylists) {
            item {
                PlaylistHeader(
                    modifier = Modifier.padding(vertical = 6.dp),
                    title = playlist.title
                )
            }
            items(playlist.items) { item ->
                DemoItemView(
                    modifier = Modifier.fillMaxWidth(),
                    title = item.title,
                    subtitle = item.description,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExamplesPreview() {
    PillarboxTheme {
        ExamplesHome()
    }
}
