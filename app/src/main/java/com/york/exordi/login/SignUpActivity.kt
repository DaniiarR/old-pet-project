package com.york.exordi.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.york.exordi.BaseActivity
import com.york.exordi.R
import com.york.exordi.login.email.EmailStepOneActivity
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    private var googleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGN_IN_RC = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signUpWithGoogleBtn.setOnClickListener {
            startActivityForResult(googleSignInClient?.signInIntent, GOOGLE_SIGN_IN_RC)
        }
        signInBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        signUpWithEmailBtn.setOnClickListener {
            startActivity(Intent(this, EmailStepOneActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            
        }
    }
}