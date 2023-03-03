/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.PlayerSurface
import ch.srgssr.pillarbox.ui.ScaleMode

/**
 * Demo default player
 *
 * @param player The player to display.
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The scale mode to use.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content.
 * @param content The Composable surface content.
 */
@Composable
fun DemoDefaultPlayer(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    defaultAspectRatio: Float? = null,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PlayerSurface(
            modifier = Modifier,
            player = player,
            scaleMode = scaleMode,
            defaultAspectRatio = defaultAspectRatio,
            surfaceContent = content
        )
        ExoPlayerSubtitleView(modifier = Modifier.matchParentSize(), player = player)
    }
}