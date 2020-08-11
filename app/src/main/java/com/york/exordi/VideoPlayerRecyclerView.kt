//package com.york.exordi
//
//import android.content.Context
//import android.graphics.Point
//import android.util.AttributeSet
//import android.view.View
//import android.view.WindowManager
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.ProgressBar
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.exoplayer2.ExoPlayerFactory
//import com.google.android.exoplayer2.Player
//import com.google.android.exoplayer2.SimpleExoPlayer
//import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
//import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
//import com.google.android.exoplayer2.ui.PlayerView
//import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
//import com.york.exordi.adapters.PostViewHolder
//import com.york.exordi.models.Post
//import com.york.exordi.models.Result
//
//class VideoPlayerRecyclerView : RecyclerView {
//
//    companion object {
//        private const val TAG = "VideoPlayerRecyclerView"
//    }
//
//    private enum class VolumeState {ON, OFF}
//
//    constructor(context: Context) : super(context)
//
//    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
//
//    private var viewHolderParent: View? = null
//    private var thumbNail: ImageView? = null
//    private var progressBar: ProgressBar? = null
//    private var frameLayout: FrameLayout? = null
//
//    private lateinit var videoPlayer: SimpleExoPlayer
//    private var videoSurfaceView: PlayerView? = null
//    private val posts = arrayListOf<Result>()
//    private var videoSurfaceDefaultHeight = 0
//    private var screenDefaultHeight = 0
//    private lateinit var mContext: Context
//    private var playPosition = -1
//    private var isVideoViewAdded = false
//
//    private lateinit var volumeState: VolumeState
//
//    private fun init() {
//        mContext = context.applicationContext
//        val display = context.display
//        val point = Point()
//        display?.getRealSize(point)
//        videoSurfaceDefaultHeight = point.x
//        screenDefaultHeight = point.y
//
//        videoSurfaceView = PlayerView(mContext)
//        videoSurfaceView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//
//        val bandwidthMeter = DefaultBandwidthMeter()
////        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
//        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
//        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
//
//        videoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
//        videoSurfaceView?.useController = false
//        videoSurfaceView?.player = videoPlayer
//        setVolumeControl(VolumeState.ON)
//
//        addOnScrollListener(object : OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    thumbNail?.let { it.visibility = View.VISIBLE }
//                }
//
//                if (!recyclerView.canScrollVertically(1)) {
//                    playVideo(true)
//                } else {
//                    playVideo(false)
//                }
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//            }
//        })
//        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
//            override fun onChildViewDetachedFromWindow(view: View) {
//                if (viewHolderParent != null && viewHolderParent == view) {
//                    resetVideoView()
//                }
//            }
//
//            override fun onChildViewAttachedToWindow(view: View) {
//                TODO("Not yet implemented")
//            }
//        })
//        videoPlayer.addListener(object : Player.EventListener {
//            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//                when (playbackState) {
//                    Player.STATE_BUFFERING -> progressBar?.visibility = View.VISIBLE
//                    Player.STATE_ENDED -> videoPlayer.seekTo(0)
//                    Player.STATE_IDLE -> {}
//                    Player.STATE_READY -> {
//                        progressBar?.visibility = View.GONE
//                        if (!isVideoViewAdded) {
//                            addVideoView()
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    private fun playVideo(isEndOfList: Boolean) {
//        var targetPosition: Int?
//        if (!isEndOfList) {
//            val startPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//            var endPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
//
//            if (endPosition - startPosition > 1) {
//                endPosition = startPosition + 1
//            }
//
//            if (startPosition < 0 || endPosition < 0) {
//                return
//            }
//
//            if (startPosition != endPosition) {
//                // my custom stuff
//                val startPositionVideoWidth = getVisibleVideoSurfaceWidth(startPosition)
//                val endPositionVideoWidth = getVisibleVideoSurfaceWidth(endPosition)
//                targetPosition = if (startPositionVideoWidth > endPositionVideoWidth) startPosition else endPosition
//            } else {
//                targetPosition = startPosition
//            }
//        } else {
//            targetPosition = posts.size - 1
//        }
//
//        if (targetPosition == playPosition) {
//            return
//        }
//
//        playPosition = targetPosition
//        if (videoSurfaceView == null) return
//
//        videoSurfaceView?.visibility = View.INVISIBLE
//        removeVideoView(videoSurfaceView)
//
//        val currentPosition = targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//        val child = getChildAt(currentPosition) ?: return
//
//        val holder: PostViewHolder? = child.tag as PostViewHolder
//        if (holder == null) {
//            playPosition = -1
//            return
//        }
//        // thumbnail
//        progressBar = holder.progressBar
//        viewHolderParent = holder.itemView
//        frameLayout = holder.frameLayout
//
//        videoSurfaceView!!.player = videoPlayer
//
//    }
//
//    private fun addVideoView() {
//        frameLayout?.addView(videoSurfaceView)
//        isVideoViewAdded = true
//        videoSurfaceView?.requestFocus()
//        videoSurfaceView?.visibility = View.VISIBLE
//        videoSurfaceView?.alpha = 1F
//        thumbNail?.visibility = View.GONE
//    }
//
//    private fun resetVideoView() {
//        if (isVideoViewAdded) {
//            removeVideoView(videoSurfaceView)
//            playPosition = -1
//            videoSurfaceView?.visibility = View.INVISIBLE
//            thumbNail?.visibility = View.VISIBLE
//        }
//    }
//    private fun setVolumeControl(state: VolumeState) {
//        volumeState = state
//        if (state == VolumeState.OFF) {
//            videoPlayer.volume = 0F
//            animateVolumeControl()
//        } else if (state == VolumeState.ON) {
//            videoPlayer.volume = 1F
//            animateVolumeControl()
//        }
//    }
//}