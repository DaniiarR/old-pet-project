package com.york.exordi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.york.exordi.base.BaseActivity
import com.york.exordi.login.LoginActivity
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (PrefManager.getMyPrefs(this).getString(Const.PREF_AUTH_TOKEN, null) == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}