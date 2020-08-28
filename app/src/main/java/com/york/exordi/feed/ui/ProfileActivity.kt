package com.york.exordi.feed.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.adapters.ProfilePhotosAdapter
import com.york.exordi.adapters.UserPostAdapter
import com.york.exordi.events.DeletePostEvent
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.events.FollowUnfollowEvent
import com.york.exordi.feed.viewmodel.ProfileViewModel
import com.york.exordi.models.Profile
import com.york.exordi.models.ProfileData
import com.york.exordi.models.Result
import com.york.exordi.settings.SettingsActivity
import com.york.exordi.shared.*
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val RC_SINGLE_POST = 100
    }

    private val viewModel by viewModels<ProfileViewModel>()

    private var progressBar: CircularProgressDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(profileToolbar)
        profileToolbar.setNavigationOnClickListener { finish() }

//        if (viewModel.profile.value == null) {
//            if (intent.getSerializableExtra(Const.EXTRA_PROFILE) == null) {
//                viewModel.getNewProfile()
//            } else {
//                viewModel.profile.value = intent.getSerializableExtra(Const.EXTRA_PROFILE) as Profile
//            }
//        }
        viewModel.getNewProfile()

        progressBar = getCircularProgressDrawable()
        
        viewModel.profile.observe(this) {
            setupViews(it)
            populatePosts()
        }
//        populatePosts()
        editProfileButton.setOnClickListener { startEditProfileActivity() }
        ratingLl.setOnClickListener { showBottomSheetDialog() }
        profileFollowersLayout.setOnClickListener { startFollowersOrFollowingsActivity(Const.EXTRA_MODE_FOLLOWERS) }
        profileFollowingsLayout.setOnClickListener { startFollowersOrFollowingsActivity(Const.EXTRA_MODE_FOLLOWINGS) }
    }

    private fun startFollowersOrFollowingsActivity(mode: String) {
        startActivity(Intent(this, FollowersOrFollowingsActivity::class.java).apply {
            putExtra(Const.EXTRA_ACTIVITY_MODE, mode)
        })
        registerActivityForEvents()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog =
            ProfileBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    viewModel.profile.value?.data?.numberOfFollowers?.let {
                        putInt(Const.EXTRA_NUMBER_OF_FOLLOWERS, it)
                        putInt(Const.EXTRA_FOLLOWERS_CHANGE, viewModel.profile.value!!.data.followersChange)
                        putInt(Const.EXTRA_RATING_CHANGE, viewModel.profile.value!!.data.ratingChange)
                        putInt(Const.EXTRA_UPVOTES_CHANGE, viewModel.profile.value!!.data.upvotesChange)
                        putDouble(Const.EXTRA_RATING, viewModel.profile.value?.data?.rating!!)
                    }
                }
            }
        bottomSheetDialog.show(supportFragmentManager, "bottom_sheet_dialog")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profileSettings -> startSettingsActivity()
        }
        return true
    }

    private fun startSettingsActivity() {
        registerActivityForEvents()
        startActivity(Intent(this, SettingsActivity::class.java).apply {
            putExtra(Const.EXTRA_PROFILE, viewModel.profile.value)
        })
    }

    private fun startEditProfileActivity() {
        registerActivityForEvents()
        startActivity(Intent(this, EditProfileActivity::class.java).apply {
            putExtra(Const.EXTRA_PROFILE, viewModel.profile.value)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OtherUserProfileActivity.RC_SINGLE_POST && resultCode == Activity.RESULT_OK) {
            viewModel.upvotePostLocally(data!!.getStringExtra(Const.EXTRA_POST_ID)!!)
        }
    }

    @Subscribe
    fun onDeletePost(event: DeletePostEvent) {
        viewModel.getNewProfile()
        populatePosts()
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        viewModel.getNewProfile()
        populatePosts()
    }

    @Subscribe
    fun onFollowUnfollowEvent(event: FollowUnfollowEvent) {
        viewModel.getNewProfile()
        populatePosts()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterActivityFromEvents()
    }

    private fun setupViews(profile: Profile) {
        profile.data.also {
            usernameTv.text = it.username
            profileDescriptionTv.text = it.bio
            if (!TextUtils.isEmpty(it.profilePic)) {
                Glide.with(this@ProfileActivity).load(it.profilePic).placeholder(progressBar).into(profilePictureIv)
            }
            ratingTv.text = it.rating.toString() ?: "0"
            profileFollowersTv.text = it.numberOfFollowers.toString()
            profileFollowingsTv.text = it.numberOfFollowings.toString()
            profileNumberOfPostsTv.text = it.numberOfPosts.toString()
        }
    }

    private fun populatePosts() {
            val adapter = UserPostAdapter(object : OnItemClickListener {
                override fun <T> onItemClick(listItem: T) {
                    registerActivityForEvents()
                    launchSinglePostActivity(listItem as Result)
                }
            })
            profilePostsRv.layoutManager = GridLayoutManager(this, 3)
            profilePostsRv.adapter = adapter
            viewModel.getPosts()?.observe(this) {
                adapter.submitList(it)
            }
    }

    private fun launchSinglePostActivity(result: Result) {
        registerActivityForEvents()
        startActivityForResult(Intent(this, SinglePostActivity::class.java).apply {
            putExtra(Const.EXTRA_POST, result)
        }, RC_SINGLE_POST)
    }
}