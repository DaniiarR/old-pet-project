package com.york.exordi.feed

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
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.models.Profile
import com.york.exordi.settings.SettingsActivity
import com.york.exordi.shared.Const
import kotlinx.android.synthetic.main.activity_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ProfileActivity : AppCompatActivity() {

    private val viewModel by viewModels<ProfileViewModel>()

    private var progressBar: CircularProgressDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(profileToolbar)
        profileToolbar.setNavigationOnClickListener { finish() }

        if (viewModel.profile.value == null) {
            viewModel.profile.value = intent.getSerializableExtra(Const.EXTRA_PROFILE) as Profile
        }

        progressBar = CircularProgressDrawable(this).apply {
            strokeWidth = 5F
            centerRadius = 30F
            setColorSchemeColors(ContextCompat.getColor(this@ProfileActivity, R.color.textColorPrimary))
            start()
        }
        
        viewModel.profile.observe(this) {
            setupViews(it)
        }
        editProfileButton.setOnClickListener { startEditProfileActivity() }
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        startActivity(Intent(this, SettingsActivity::class.java).apply {
            putExtra(Const.EXTRA_PROFILE, viewModel.profile.value)
        })
    }

    private fun startEditProfileActivity() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        startActivity(Intent(this, EditProfileActivity::class.java).apply {
            putExtra(Const.EXTRA_PROFILE, viewModel.profile.value)
        })
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        val profile = Profile(event.email, event.username, event.birthday, event.bio, event.profilePic)
        viewModel.profile.value = profile
        setupViews(profile)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun setupViews(profile: Profile) {
        usernameTv.text = profile.username
        profileDescriptionTv.text = profile.bio
        if (!TextUtils.isEmpty(profile.profilePic)) {
            Glide.with(this@ProfileActivity).load(profile.profilePic).placeholder(progressBar).into(profilePictureIv)
        }
    }
}