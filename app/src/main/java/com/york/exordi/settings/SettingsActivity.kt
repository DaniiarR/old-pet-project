package com.york.exordi.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.york.exordi.R
import com.york.exordi.adapters.SettingsAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.feed.EditProfileActivity
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

    }

    private fun selectActivity(position: Int) {
        when (position) {
            0 -> {
                EventBus.getDefault().register(this)
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        }
    }

    @Subscribe
    fun onEditProfile(event: EditProfileEvent) {
        EventBus.getDefault().unregister(this)
        finish()
    }
}