/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.comscore

import android.util.Log
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.util.Size
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.comscore.streaming.ContentMetadata
import com.comscore.streaming.StreamingAnalytics

/**
 * A [MediaItemTracker] implementation for ComScore analytics.
 *
 * @param streamingAnalytics The [StreamingAnalytics] instance to use for tracking.
 */
class ComScoreTracker internal constructor(
    private val streamingAnalytics: StreamingAnalytics = StreamingAnalytics()
) : MediaItemTracker<ComScoreTracker.Data> {
    /**
     * Represents data to be sent to ComScore.
     *
     * @property assets A map of labels to be sent to ComScore.
     */
    data class Data(val assets: Map<String, String>)

    private val component = PlayerComponent()
    private val window = Window()
    private lateinit var latestData: Data

    /**
     * A surface is connected to the player when its [ExoPlayer.getSurfaceSize] is different from [Size.ZERO].
     * When used with MediaSessionService or MediaBrowser the size is always [Size.UNKNOWN]. When not connected the size is [Size.ZERO].
     */
    private var isSurfaceConnected: Boolean = false
    private var isBuffering: Boolean = false

    init {
        streamingAnalytics.setMediaPlayerName(MEDIA_PLAYER_NAME)
        streamingAnalytics.setMediaPlayerVersion(BuildConfig.VERSION_NAME)
    }

    override fun start(player: ExoPlayer, data: Data) {
        isSurfaceConnected = player.surfaceSize != Size.ZERO
        streamingAnalytics.createPlaybackSession()
        setMetadata(data)
        handleStart(player)
        player.addAnalyticsListener(component)
    }

    override fun stop(player: ExoPlayer) {
        player.removeAnalyticsListener(component)
        notifyEnd()
    }

    private fun setMetadata(data: Data) {
        DebugLogger.debug(TAG, "SetMetadata $data")
        val assets = ContentMetadata.Builder()
            .customLabels(data.assets)
            .build()
        streamingAnalytics.setMetadata(assets)
        latestData = data
    }

    private fun handleStart(player: ExoPlayer) {
        streamingAnalytics.notifyChangePlaybackRate(player.getPlaybackSpeed())
        when {
            player.isPlaying -> {
                player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
                notifyPlay(player.currentPosition, window)
            }

            player.playbackState == Player.STATE_BUFFERING -> notifyBufferStart()
        }
    }

    private fun notifyPause() {
        DebugLogger.debug(TAG, "notifyPause")
        streamingAnalytics.notifyPause()
    }

    private fun notifyPlay(position: Long, window: Window) {
        if (!isSurfaceConnected) return
        notifyPosition(position, window)
        DebugLogger.debug(TAG, "notifyPlay")
        streamingAnalytics.notifyPlay()
    }

    private fun notifyEnd() {
        DebugLogger.debug(TAG, "notifyEnd")
        streamingAnalytics.notifyEnd()
    }

    /**
     * Notify buffer start
     * ComScore SDK : Also for a buffering to be taken into account, the time elapsed between a call to notifyBufferStart and notifyBufferStop has to
     * be bigger than 500ms. Otherwise our SDK will just ignore the notifyBufferStart.
     */
    private fun notifyBufferStart() {
        DebugLogger.debug(TAG, "notifyBufferStart")
        streamingAnalytics.notifyBufferStart()
        isBuffering = true
    }

    /**
     * According to the comprehension of the documentation and after validation with ComScore team,
     * [StreamingAnalytics.notifyBufferStop] isn't required.
     *
     * No! We shall call it for case seeking while in pause.
     */
    private fun notifyBufferStop() {
        DebugLogger.debug(TAG, "notifyBufferStop")
        streamingAnalytics.notifyBufferStop()
        isBuffering = false
    }

    /**
     * Notify position
     *
     * @param position
     * @param window
     */
    private fun notifyPosition(position: Long, window: Window) {
        if (!window.isLive) {
            DebugLogger.debug(TAG, "notifyPosition $position")
            streamingAnalytics.startFromPosition(position)
        } else {
            notifyLiveInformation(position, window)
        }
    }

    private fun notifySeek() {
        DebugLogger.debug(TAG, "notifySeek")
        streamingAnalytics.notifySeekStart()
    }

    private fun notifyLiveInformation(position: Long, window: Window) {
        val length = if (window.isSeekable) window.durationMs else LIVE_ONLY_WINDOW_LENGTH
        val windowOffset = if (window.isSeekable) length - position else LIVE_ONLY_WINDOW_OFFSET
        DebugLogger.debug(TAG, "notifyLiveInformation offset = $windowOffset length = $length")
        streamingAnalytics.setDvrWindowLength(length)
        streamingAnalytics.startFromDvrWindowOffset(windowOffset)
    }

    private inner class PlayerComponent : AnalyticsListener {
        override fun onPlaybackParametersChanged(
            eventTime: AnalyticsListener.EventTime,
            playbackParameters: PlaybackParameters
        ) {
            streamingAnalytics.notifyChangePlaybackRate(playbackParameters.speed)
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> notifyBufferStart()
                Player.STATE_READY -> notifyBufferStop()
            }
        }

        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            when (reason) {
                Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                    if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) return
                    notifySeek()
                    eventTime.timeline.getWindow(eventTime.windowIndex, window)
                    notifyPosition(newPosition.positionMs, window)
                }
            }
        }

        override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                eventTime.timeline.getWindow(eventTime.windowIndex, window)
                if (window.isLive) {
                    notifyLiveInformation(eventTime.eventPlaybackPositionMs, window)
                }
            }
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            val position = eventTime.eventPlaybackPositionMs
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            if (isPlaying) {
                notifyPlay(position, window)
            } else {
                if (!isBuffering) {
                    notifyPause()
                }
            }
        }

        override fun onSurfaceSizeChanged(eventTime: AnalyticsListener.EventTime, width: Int, height: Int) {
            val isCurrentSurfaceConnected = Size(width, height) != Size.ZERO
            if (isCurrentSurfaceConnected != isSurfaceConnected) {
                Log.d(TAG, "Surface connected change $isSurfaceConnected -> $isCurrentSurfaceConnected")
                isSurfaceConnected = isCurrentSurfaceConnected
                if (isCurrentSurfaceConnected) {
                    val position = eventTime.eventPlaybackPositionMs
                    eventTime.timeline.getWindow(eventTime.windowIndex, window)
                    notifyPlay(position, window)
                } else {
                    notifyPause()
                }
            }
        }
    }

    /**
     * A factory class responsible for creating instances of [ComScoreTracker].
     */
    class Factory : MediaItemTracker.Factory<Data> {
        override fun create(): ComScoreTracker {
            return ComScoreTracker()
        }
    }

    private companion object {
        private const val MEDIA_PLAYER_NAME = "Pillarbox"
        private const val TAG = "ComScoreTracker"
        private const val LIVE_ONLY_WINDOW_OFFSET = 0L
        private const val LIVE_ONLY_WINDOW_LENGTH = 0L
    }
}
