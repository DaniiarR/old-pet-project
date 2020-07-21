package com.york.exordi.settings

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.york.exordi.R
import com.york.exordi.adapters.SettingsAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.feed.EditProfileActivity
import com.york.exordi.login.LoginActivity
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import kotlinx.android.synthetic.main.activity_settings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settingsToolbar.setNavigationOnClickListener { finish() }
        settingsRv.layoutManager = LinearLayoutManager(this)
        val adapter = SettingsAdapter(arrayListOf("Account", "Help", "About", "Privacy", "Log out"))
        adapter.setOnItemClickListener { adapter, view, position ->
            selectActivity(position)
        }
        settingsRv.adapter = adapter
        logOutBtn.setOnClickListener {
            showConfirmActionDialog()
        }

    }

    private fun showConfirmActionDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirm action")
            setMessage("Are you sure you want to log out from the app?")
            setPositiveButton("Log out") { p0, p1 -> logOut() }
            setNegativeButton("Cancel", null)
            setCancelable(true)
        }.show()
    }

    private fun logOut() {
        startActivity(Intent(this, LoginActivity::class.java))
        PrefManager.getMyPrefs(applicationContext).edit().remove(Const.PREF_AUTH_TOKEN).apply()
        finish()
    }

    private fun selectActivity(position: Int) {
        when (position) {
            0 -> {
                EventBus.getDefault().register(this)
                startActivity(Intent(this, EditProfileActivity::class.java).apply {
                    putExtra(Const.EXTRA_PROFILE, intent.getSerializableExtra(Const.EXTRA_PROFILE))
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        finish()
    }
}