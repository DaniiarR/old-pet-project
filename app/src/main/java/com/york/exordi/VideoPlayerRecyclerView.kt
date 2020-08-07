package com.york.exordi

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.york.exordi.models.Post
import com.york.exordi.models.Result

class VideoPlayerRecyclerView : RecyclerView {

    companion object {
        private const val TAG = "VideoPlayerRecyclerView"
    }

    private enum class VolumeState {ON, OFF}

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private lateinit var videoPlayer: SimpleExoPlayer

    private val posts = arrayListOf<Result>()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private lateinit var mContext: Context
    private val playPosition = -1
    private var isVideoViewAdded = false

    private lateinit var volumeState: VolumeState


}