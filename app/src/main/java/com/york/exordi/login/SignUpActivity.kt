package com.york.exordi.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.york.exordi.BaseActivity
import com.york.exordi.R
import com.york.exordi.login.email.EmailStepOneActivity
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        signUpWithEmailBtn.setOnClickListener {
            startActivity(Intent(this, EmailStepOneActivity::class.java))
        }
    }
}