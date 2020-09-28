package com.york.exordi.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.york.exordi.R
import com.york.exordi.adapters.SettingsAdapter
import com.york.exordi.events.EditProfileEvent
import com.york.exordi.feed.ui.EditProfileActivity
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
        val adapter = SettingsAdapter(arrayListOf("Account", "Help", "About", "Privacy"))
        adapter.setOnItemClickListener { adapter, view, position ->
            selectActivity(position)
        }
        settingsRv.adapter = adapter
        logOutBtn.setOnClickListener {
            showConfirmActionDialog()
        }

    }

    private fun showConfirmActionDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Confirm action")
            setMessage("Are you sure you want to log out from the app?")
            setPositiveButton("Log out") { p0, p1 -> logOut() }
            setNegativeButton("Cancel", null)
            setCancelable(true)
        }.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }
        dialog.show()
    }

    private fun logOut() {
        PrefManager.getMyPrefs(applicationContext).edit()
            .remove(Const.PREF_AUTH_TOKEN)
            .remove(Const.PREF_REFRESH_TOKEN)
            .remove(Const.PREF_USERNAME)
            .apply()
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.EMAIL))
            .requestIdToken("214536121200-0eelqcmuaqbe0shjpk9pdhvcp4hc5adk.apps.googleusercontent.com")
            .requestServerAuthCode("214536121200-0eelqcmuaqbe0shjpk9pdhvcp4hc5adk.apps.googleusercontent.com")
//                .requestServerAuthCode("663429823844-a2sjdhehrmeu7rij1h77ok9ij0trfeof.apps.googleusercontent.com")
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso).signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
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