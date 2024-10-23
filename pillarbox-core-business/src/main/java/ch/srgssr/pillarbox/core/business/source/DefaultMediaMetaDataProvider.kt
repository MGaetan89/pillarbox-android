/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.player.extension.setChapters
import ch.srgssr.pillarbox.player.extension.setCredits

/**
 * Filling [MediaMetadata] from [Chapter].
 * [MediaMetadata] provided fields are not replaced.
 */
val DefaultMediaMetaDataProvider: suspend MediaMetadata.Builder.(MediaMetadata, Chapter, MediaComposition) -> Unit =
    { metadata, chapter, mediaComposition ->
        metadata.title ?: setTitle(chapter.title)
        metadata.subtitle ?: setSubtitle(chapter.lead)
        metadata.description ?: setDescription(chapter.description)
        metadata.artworkUri ?: run {
            val artworkUri = imageScalingService.getScaledImageUrl(
                imageUrl = chapter.imageUrl
            ).toUri()
            setArtworkUri(artworkUri)
        }
        ChapterAdapter.getChapters(mediaComposition)
            .takeIf { it.isNotEmpty() }
            ?.let { setChapters(it) }
        TimeIntervalAdapter.getCredits(chapter.timeIntervalList)
            .takeIf { it.isNotEmpty() }
            ?.let { setCredits(it) }
    }

private val imageScalingService = ImageScalingService()
