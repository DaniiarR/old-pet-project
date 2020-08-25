package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.adapters.UserPostAdapter
import com.york.exordi.events.FollowUnfollowEvent
import com.york.exordi.events.UpvoteEvent
import com.york.exordi.feed.viewmodel.OtherUserProfileViewModel
import com.york.exordi.models.OtherProfileData
import com.york.exordi.models.Result
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class OtherUserProfileActivity : AppCompatActivity() {

    companion object {
        const val RC_SINGLE_POST = 100
    }
    private val viewModel by viewModels<OtherUserProfileViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)
        setSupportActionBar(otherProfileToolbar)
        otherProfileToolbar.setNavigationOnClickListener { finish() }

        viewModel.username = intent.getStringExtra(Const.EXTRA_USERNAME) ?: ""
        viewModel.getNewProfile()
        viewModel.profile.observe(this) {
            setupViews(it)
        }
        otherUserFollowingsLayout.setOnClickListener {
            startFollowingsActivity()
        }
    }

    private fun startFollowingsActivity() {
        registerActivityForEvents()
        startActivity(Intent(this, FollowersOrFollowingsActivity::class.java).apply {
            putExtra(Const.EXTRA_ACTIVITY_MODE, Const.EXTRA_MODE_FOLLOWINGS)
            putExtra(Const.EXTRA_USERNAME, viewModel.profile.value?.username)
        })
    }

    private fun setupViews(profile: OtherProfileData?) {
        profile?.let {
            if (!it.photo.isNullOrEmpty()) {
                Glide.with(this).load(it.photo).placeholder(getCircularProgressDrawable()).into(otherProfilePictureIv)
            }
            otherUsernameTv.text = it.username
            otherRatingTv.text = it.rating.toString()
            otherProfileNumberOfFollowingsTv.text = it.numberOfFollowings.toString()
            otherProfileNumberOfPostsTv.text = it.numberOfPosts.toString()
            otherProfileDescriptionTv.text = it.bio
            setupFollowButton(it.followedByMe)
            if (it.numberOfPosts > 0) {
                populatePosts()
            }
        }
    }

    private fun populatePosts() {
        makeInternetSafeRequest {
            val adapter = UserPostAdapter(object : OnItemClickListener {
                override fun <T> onItemClick(listItem: T) {
                    registerActivityForEvents()
                    launchSinglePostActivity(listItem as Result)
                }
            })
            otherProfilePostsRv.layoutManager = GridLayoutManager(this, 3)
            otherProfilePostsRv.adapter = adapter
            viewModel.getPosts()?.observe(this) {
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActivityFromEvents()
    }

    @Subscribe
    fun onUpvotePost(event: UpvoteEvent) {
        viewModel.upvotePostLocally(event.postId)
    }


    @Subscribe
    fun onFollowUnfollow(event: FollowUnfollowEvent) {
        viewModel.getNewProfile()
        populatePosts()
    }

    private fun launchSinglePostActivity(result: Result) {
        startActivityForResult(Intent(this, SinglePostActivity::class.java).apply {
            putExtra(Const.EXTRA_POST, result)
        }, RC_SINGLE_POST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SINGLE_POST && resultCode == Activity.RESULT_OK) {
            viewModel.upvotePostLocally(data!!.getStringExtra(Const.EXTRA_POST_ID)!!)
        }
    }

    private fun setupFollowButton(followedByMe: Boolean) {
        otherProfileUnfollowBtn.setOnClickListener { showConfirmUnfollowDialog() }
        otherProfileFollowBtn.setOnClickListener { followUser() }
        if (followedByMe) {
            setUnfollowMode()
        } else {
            setFollowMode()
        }
    }

    private fun setFollowMode() {
        otherProfileFollowBtn.isEnabled = true
        otherProfileFollowBtn.visibility = View.VISIBLE
        otherProfileUnfollowBtn.visibility = View.INVISIBLE
    }

    private fun setUnfollowMode() {
        otherProfileUnfollowBtn.isEnabled = true
        otherProfileUnfollowBtn.visibility = View.VISIBLE
        otherProfileFollowBtn.visibility = View.INVISIBLE
    }

    private fun showConfirmUnfollowDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Confirm action")
            setMessage("Are you sure you want to unfollow? ${viewModel.profile.value?.username}")
                setPositiveButton("Unfollow") { p0, p1 -> unfollowUser() }
            setNegativeButton("Cancel", null)
            setCancelable(true)
        }.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }
        dialog.show()
    }

    private fun followUser() {
        otherProfileFollowBtn.isEnabled = false
        viewModel.followUser().observe(this) {
            it?.let {
                if (it) {
                    EventBus.getDefault().post(FollowUnfollowEvent())
                    setUnfollowMode()
                } else {
                    Toast.makeText(this, "Could not follow user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun unfollowUser() {
        otherProfileUnfollowBtn.isEnabled = false
        viewModel.followUser().observe(this) {
            it?.let {
                if (it) {
                    EventBus.getDefault().post(FollowUnfollowEvent())
                    setFollowMode()
                } else {
                    Toast.makeText(this, "Could not unfollow user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}