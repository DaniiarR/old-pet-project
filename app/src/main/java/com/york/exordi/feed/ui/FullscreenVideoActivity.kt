package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
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
import com.york.exordi.models.PostId
import com.york.exordi.repository.AppRepository
import com.york.exordi.shared.Const
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_fullscreen_video.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*
import kotlin.properties.Delegates

class FullscreenVideoActivity : BaseActivity() {

    companion object {
        private const val TAG = "FullscreenVideoActivity"
    }

    private lateinit var exoPlayer: SimpleExoPlayer

    private var seconds: Long = 0
    private var isWatched = false
    var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        handler = Handler(Looper.getMainLooper())
        val videoUrl = intent.getStringExtra(Const.EXTRA_VIDEO_URL)
        val playbackPosition = intent.getLongExtra(Const.EXTRA_PLAYBACK_POSITION, 0)
        val postId = intent.getStringExtra(Const.EXTRA_POST_ID)
        seconds = intent.getLongExtra(Const.EXTRA_SECONDS, 0)
        exo_fullscreen.setOnClickListener {
            onBackPressed()
        }

        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        youtube_overlay
            .performListener(object : YouTubeOverlay.PerformListener {
                override fun onAnimationStart() {
                    // Do UI changes when circle scaling animation starts (e.g. hide controller views)
                    youtube_overlay.visibility = View.VISIBLE
                }

                override fun onAnimationEnd() {
                    // Do UI changes when circle scaling animation starts (e.g. show controller views)
                    youtube_overlay.visibility = View.GONE
                }
            })
        youtube_overlay.player(exoPlayer)
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))

        exoPlayer.addListener(object : Player.EventListener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    handler!!.post(object : Runnable {
                        override fun run() {
                            Log.e(TAG, "onIsPlayingChanged: handler started:$seconds")
                            if (++seconds >= exoPlayer.duration / 1000 / 2) {
                                watchVideo(PostId(postId!!))
                            } else {
                                handler!!.postDelayed(this, 1000)
                            }
                        }
                    }
                    )
                } else {
                    Log.e(
                        TAG,
                        "onIsPlayingChanged: " + "handler stopped"
                    )
                    handler!!.removeCallbacksAndMessages(null)
                }
            }
        })
        //2
        DefaultDataSourceFactory(this, userAgent)
        var mediaSource: MediaSource? = null
        when (intent.getStringExtra(Const.EXTRA_FULLSCREEN_MODE)) {
            Const.EXTRA_FULLSCREEN_MODE_HLS -> {
                val dataSourceFactory = DefaultHttpDataSourceFactory(
                    userAgent,
                    DefaultBandwidthMeter(),
                    DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                    DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                    true
                )
                mediaSource = HlsMediaSource
                    .Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(Uri.parse(videoUrl))
            }
            Const.EXTRA_FULLSCREEN_MODE_LOCAL -> {
                val dataSourceFactory = DefaultDataSourceFactory(this, userAgent)
                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl))
            }
        }
        //3
        mediaSource?.let {
            exoPlayer.prepare(mediaSource)
        }
        exoPlayer.seekTo(playbackPosition)
        //4
        makeInternetSafeRequest { exoPlayer.playWhenReady = true }
        playerView.player = exoPlayer
    }

    private fun watchVideo(postId: PostId) {
        AppRepository.getInstance(application).watchVideo(postId)
        isWatched = true
    }

    private fun savePlaybackPosition() {
        val intent = Intent().apply {
            putExtra(Const.EXTRA_PLAYBACK_POSITION, exoPlayer.currentPosition)
            putExtra(Const.EXTRA_SECONDS, seconds)
            putExtra(Const.EXTRA_IS_WATCHED, isWatched)
        }
        setResult(Activity.RESULT_OK, intent)
        seconds = 0
        handler!!.removeCallbacksAndMessages(null)
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