package com.york.exordi.feed

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import com.york.exordi.R
import com.york.exordi.adapters.ProfilePhotosAdapter
import com.york.exordi.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(profileToolbar)

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
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}