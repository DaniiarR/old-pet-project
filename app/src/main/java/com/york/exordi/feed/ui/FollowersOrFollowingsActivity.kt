package com.york.exordi.feed.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.york.exordi.R
import com.york.exordi.feed.viewmodel.FollowersViewModel
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.york.exordi.adapters.FollowerAdapter
import com.york.exordi.base.BaseActivity
import com.york.exordi.events.FollowUnfollowEvent
import com.york.exordi.models.FollowerResult
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.activity_followers_or_followings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class FollowersOrFollowingsActivity : BaseActivity() {

    private val viewModel by viewModels<FollowersViewModel>()

    lateinit var adapter: FollowerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers_or_followings)

        viewModel.activityMode = intent.getStringExtra(Const.EXTRA_ACTIVITY_MODE)
        viewModel.otherUserUsername = intent.getStringExtra(Const.EXTRA_USERNAME)

        setSupportActionBar(followersToolbar)
        followersToolbar.setNavigationOnClickListener { finish() }
        followersToolbar.title = viewModel.activityMode

        adapter = FollowerAdapter()
        adapter.clickListener = object : OnItemWithTagClickListener {
            override fun <T> onItemClick(listItem: T, tag: String, position: Int) {
                when (tag) {
                    Const.TAG_WHOLE_VIEW -> makeInternetSafeRequest { startOtherUserActivity(listItem as FollowerResult) }
                    Const.TAG_FOLLOW -> makeInternetSafeRequest { followOrUnfollowUser(listItem as FollowerResult, position) }
                    Const.TAG_UNFOLLOW -> makeInternetSafeRequest { followOrUnfollowUser(listItem as FollowerResult, position) }
                }
            }
        }

        followersRv.layoutManager = LinearLayoutManager(this)
        viewModel.isFollowersListEmpty.observe(this) {empty ->
            if (empty) {
                followersEmptyView.visibility = View.VISIBLE
                followersRv.visibility = View.GONE
                followersEmptyView.text = if (viewModel.activityMode == Const.EXTRA_MODE_FOLLOWERS) "You have no followers yet" else "You have no followings yet"
            } else {
                followersEmptyView.visibility = View.GONE
                followersRv.visibility = View.VISIBLE
            }
        }

        makeInternetSafeRequest {
            viewModel.getNewFollowers()?.observe(this) {
                adapter.submitList(it)
                followersRv.adapter = adapter
            }
        }
    }

    private fun followOrUnfollowUser(follower: FollowerResult, position: Int) {
        viewModel.followOrUnfollowUser(follower.username).observe(this) {
            it?.let {
                if (it) {
                    EventBus.getDefault().post(FollowUnfollowEvent())
                    follower.isFollowedByUser = !follower.isFollowedByUser
                    adapter.notifyItemChanged(position)
                } else {
                    Toast.makeText(this, "Could not perform this action", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startOtherUserActivity(followerResult: FollowerResult) {
        registerActivityForEvents()
        startActivity(Intent(this, OtherUserProfileActivity::class.java).apply {
            putExtra(Const.EXTRA_USERNAME, followerResult.username)
        })
    }

    @Subscribe
    fun onFollowUnfollowEvent(event: FollowUnfollowEvent) {
        viewModel.getNewFollowers()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActivityFromEvents()
    }
}