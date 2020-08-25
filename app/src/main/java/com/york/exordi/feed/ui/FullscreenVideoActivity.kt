package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.exordi.R
import com.york.exordi.base.BaseActivity
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_fullscreen_video.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*

class FullscreenVideoActivity : BaseActivity() {

    private lateinit var exoPlayer: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        val videoUrl = intent.getStringExtra(Const.EXTRA_VIDEO_URL)
        val playbackPosition = intent.getLongExtra(Const.EXTRA_PLAYBACK_POSITION, 0)
        exo_fullscreen.setOnClickListener {
            onBackPressed()
        }
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        //2
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            userAgent,
            DefaultBandwidthMeter(),
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
            true
        )
        val mediaSource = HlsMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl))
        //3
        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(playbackPosition)
        //4
        exoPlayer.playWhenReady = true
        fullscreenPlayerView.player = exoPlayer
    }

    private fun savePlaybackPosition() {
        val intent = Intent().apply {
            putExtra(Const.EXTRA_PLAYBACK_POSITION, exoPlayer.currentPosition)
        }
        setResult(Activity.RESULT_OK, intent)
        exoPlayer.release()
        finish()
    }
    override fun onPause() {
        super.onPause()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        savePlaybackPosition()
        super.onDestroy()
    }

    override fun onBackPressed() {
        savePlaybackPosition()
        super.onBackPressed()
    }
}