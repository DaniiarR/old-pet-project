package com.york.exordi.feed

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.adapters.ProfilePhotosAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.Profile
import com.york.exordi.settings.SettingsActivity
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ProfileActivity : AppCompatActivity() {

    private val viewModel by viewModels<ProfileViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(profileToolbar)
        profileToolbar.setNavigationOnClickListener { finish() }

        viewModel.profile.value = intent.getSerializableExtra(Const.EXTRA_PROFILE) as Profile

        viewModel.profile.observe(this) {
            setupViews(it)
        }
        editProfileButton.setOnClickListener { startSettingsActivity() }
        ratingLl.setOnClickListener { showBottomSheetDialog() }
        profilePostsRv.layoutManager = GridLayoutManager(this, 3)
        profilePostsRv.adapter = ProfilePhotosAdapter()

    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = ProfileBottomSheetDialog.newInstance()
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
        EventBus.getDefault().register(this)
        startActivity(Intent(this, SettingsActivity::class.java).apply {
            putExtra(Const.EXTRA_PROFILE, viewModel.profile.value)
        })
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        viewModel.getNewProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun setupViews(profile: Profile) {
        usernameTv.text = profile.username
        profileDescriptionTv.text = profile.bio
        Glide.with(this@ProfileActivity).load(profile.profilePic).into(profilePictureIv)
    }
}