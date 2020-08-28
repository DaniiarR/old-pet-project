package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.york.exordi.R
import com.york.exordi.adapters.CommentAdapter
import com.york.exordi.events.DeletePostEvent
import com.york.exordi.events.UpvoteEvent
import com.york.exordi.models.CommentResult
import com.york.exordi.models.Result
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.feed_list_item.*
import kotlinx.android.synthetic.main.feed_list_item.view.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.view.*
import org.greenrobot.eventbus.EventBus

class SinglePostActivity : AppCompatActivity() {

    companion object {
        const val RC_FULLSCREEN_ACTIVITY = 10
        private const val TAG = "SinglePostActivity"
    }

    private val viewModel by viewModels<SinglePostViewModel>()

    private var exoPlayer: SimpleExoPlayer? = null
    private var videoSurfaceView: PlayerView? = null
    private var fullscreenButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_post)
        viewModel.post = intent.getSerializableExtra(Const.EXTRA_POST) as Result

        setupViews()

    }

    override fun onBackPressed() {
        if (feedCommentsLayout.isVisible) {
            feedCommentsLayout.visibility = View.INVISIBLE
        } else {
            finish()
        }
    }

    private fun setupViews() {
        viewModel.post.also {
            if (!it.author.photo.isNullOrEmpty()) {
                Glide.with(this).load(it.author.photo).placeholder(getCircularProgressDrawable()).into(feedProfilePictureIv)
            }
            if (it.author.username == PrefManager.getMyPrefs(applicationContext).getString(Const.PREF_USERNAME, "")) {
                feedDeletePost.visibility = View.VISIBLE
                feedDeletePost.setOnClickListener { v ->
                    showDeletePostDialog(it.id)
                }
            } else {
                feedDeletePost.visibility = View.GONE
            }
            feedUsernameTv.text = it.author.username
            feedPublicationDateTv.text = it.postedOn.toHoursAgo()
            feedCommentsTv.text = it.commentsAmount.toString() + " comments"
            feedDescriptionTv.text = it.text
            if (it.files[0].type == "video") {
                setupVideo()
            } else if (it.files[0].type == "image") {
                Glide.with(this).load(it.files[0].file).placeholder(getCircularProgressDrawable()).into(feedPhotoIv)
            }
            feedProfilePictureIv.setOnClickListener { launchOtherUserProfileActivity(viewModel.post.author.username) }
            feedUsernameTv.setOnClickListener { launchOtherUserProfileActivity(viewModel.post.author.username) }
            feedUpvoteBtn.setImageResource(if (it.upvotedByUser) R.drawable.ic_upvote_filled else R.drawable.ic_upvote)
            feedUpvoteBtn.setOnClickListener { v ->
                makeInternetSafeRequest {
                    toggleUpvoteButton(it.upvotedByUser)
                    it.upvotedByUser = !it.upvotedByUser
                    viewModel.toggleUpvote(it.id)
                    viewModel.isUpvoteSuccessful.observe(this) {
                        it?.let {upvoted ->
                            if (upvoted) {
                                viewModel.post.upvotedByUser = !viewModel.post.upvotedByUser
//                                EventBus.getDefault().post(UpvoteEvent(viewModel.post.id))
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra(Const.EXTRA_POST_ID, viewModel.post.id)
                                })
                            }
                        }
                    }
                }
            }
            feedCommentsBtn.setOnClickListener {
                makeInternetSafeRequest {
                    showComments(viewModel.post)
                }
            }
        }
    }

    private fun showDeletePostDialog(postId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.isDeletePostSuccessful.observe(this) {
                    it?.let {
                        if (it) {
                            Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                            EventBus.getDefault().post(DeletePostEvent())
                            finish()
                        } else {
                            Toast.makeText(this, "Could not delete this post", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                viewModel.deletePost(postId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showComments(post: Result) {
        viewModel.comments = null
        // show the recyclerView, editText, and blank space here
        // but hide them in PostAdapter
        scrollToComments()
        feedCommentsLayout.feedCommentSendBtn.setOnClickListener {
            if (feedCommentsLayout.feedCommentsEt.text.isNotEmpty()) {
                postComment(feedCommentsLayout.feedCommentsEt.text.toString().trim(), post.id)
            } else {
                Toast.makeText(this, "Cannot post an empty comment!", Toast.LENGTH_SHORT).show()
            }
        }
        feedCommentsLayout.visibility = View.VISIBLE
        feedCommentsRv.visibility = View.VISIBLE
        feedBlankSpace.visibility = View.VISIBLE
        feedCommentsEmptyView.visibility = View.GONE
        commentsPb.visibility = View.VISIBLE
        val commentAdapter = PrefManager.getMyPrefs(applicationContext).getString(Const.PREF_USERNAME, null)
            ?.let {
                CommentAdapter(post.isCurrentUserPost, it, object : OnCommentClickListener {
                    override fun onItemClick(comment: CommentResult, tag: String) {
                        when (tag) {
                            Const.TAG_PROFILE -> launchOtherUserProfileActivity(comment.author.username)
                            Const.TAG_COMMENT_DETAILS -> showDeleteCommentDialog(comment)
                        }
                    }
                })
            }
        feedCommentsRv.adapter = commentAdapter
        feedCommentsRv.layoutManager = LinearLayoutManager(feedCommentsRv.context)
        viewModel.getNewComments(post.id)?.observe(this) {
            commentAdapter?.submitList(it)
            commentsPb.visibility = View.INVISIBLE
        }
        viewModel.isDeleteCommentSuccessful.observe(this) {
            it?.let {
                if (it) {
                    viewModel.getNewComments(post.id)
                }
            }
        }
        if (post.commentsAmount == 0) {
            feedCommentsEmptyView.visibility = View.VISIBLE
        }
    }

    private fun showDeleteCommentDialog(comment: CommentResult) {
        AlertDialog.Builder(this)
            .setTitle("Delete comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                deleteComment(comment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComment(comment: CommentResult) {
        viewModel.deleteComment(comment)
    }


    private fun scrollToComments() {
        feedListItemScrollView.post {
            feedListItemScrollView.smoothScrollTo(0, feedCommentsRv.top)
        }
    }

    private fun launchOtherUserProfileActivity(username: String) {
        if (username == PrefManager.getMyPrefs(applicationContext).getString(Const.PREF_USERNAME, "")) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            startActivity(Intent(this, OtherUserProfileActivity::class.java).apply {
                putExtra(Const.EXTRA_USERNAME, username)
            })
        }
    }

    private fun postComment(comment: String, postId: String) {
        viewModel.postComment(comment, postId).observe(this) {
            it?.let {
                if (it) {
                    feedCommentsLayout.feedCommentsEt.text = null
                    viewModel.getNewComments(postId)
                    feedCommentsEmptyView.visibility = View.GONE
                } else {
                    Toast.makeText(this, "Could not comment this post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleUpvoteButton(upvotedByUser: Boolean) {
        if (!upvotedByUser) {
            feedUpvoteBtn.setImageResource(R.drawable.ic_upvote_filled)
        } else {
            feedUpvoteBtn.setImageResource(R.drawable.ic_upvote)
        }
    }

    private fun setupVideo() {
        videoSurfaceView = PlayerView(this)
        videoSurfaceView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        fullscreenButton = videoSurfaceView?.findViewById<ImageButton>(R.id.exo_fullscreen)
        videoSurfaceView?.findViewById<View>(R.id.exoTimelineControls)?.visibility = View.GONE
        videoSurfaceView?.useController = true
        videoSurfaceView?.findViewById<ImageButton>(R.id.exo_fullscreen)?.setOnClickListener {
            exoPlayer?.let {
                exoPlayer?.playWhenReady = false
                launchFullscreenVideoActivity(viewModel.post.files[0].file, it.currentPosition)
            }
        }
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        exoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> feedListItemPb.visibility = View.VISIBLE
                    Player.STATE_ENDED -> exoPlayer?.seekTo(0)
                    Player.STATE_IDLE -> {}
                    Player.STATE_READY -> {
                        feedListItemPb.visibility = View.GONE
                        if (!viewModel.isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                }
            }
        })
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        //2

        val mediaUrl = viewModel.post.files[0].file
//        val mediaUrl =
//            "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4"
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            userAgent,
            DefaultBandwidthMeter(),
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
            true
        )

        val mediaSource = HlsMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(mediaUrl))
        //3
        exoPlayer?.prepare(mediaSource)
        //4
        exoPlayer?.playWhenReady = true
        videoSurfaceView?.player = exoPlayer

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    private fun launchFullscreenVideoActivity(
        videoUrl: String,
        currentPosition: Long
    ) {
        startActivityForResult(Intent(this, FullscreenVideoActivity::class.java).apply {
            putExtra(Const.EXTRA_VIDEO_URL, videoUrl)
            putExtra(Const.EXTRA_PLAYBACK_POSITION, currentPosition)
        }, RC_FULLSCREEN_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FeedFragment.RC_FULLSCREEN_ACTIVITY && resultCode == Activity.RESULT_OK) {
            setPlaybackPosition(data!!.getLongExtra(Const.EXTRA_PLAYBACK_POSITION, 0))
        }
    }

    private fun setPlaybackPosition(position: Long) {
        exoPlayer?.seekTo(position)
        exoPlayer?.playWhenReady = true
    }

    private fun addVideoView() {
        feedListItemFrameLayout.addView(videoSurfaceView)
        viewModel.isVideoViewAdded = true
        videoSurfaceView?.requestFocus()
        videoSurfaceView?.visibility = View.VISIBLE
        videoSurfaceView?.alpha = 1f
    }
}