package com.york.exordi.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.york.exordi.base.BaseActivity
import com.york.exordi.MainActivity
import com.york.exordi.R
import com.york.exordi.models.AuthToken
import com.york.exordi.models.Login
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private val TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        usernameEt.doOnTextChanged { _, _, _, _ ->
            hideError()
        }
        passwordTil.doOnTextChanged { _, _, _, _ ->
            hideError()
        }
        signInBtn.setOnClickListener {
            if (checkInputs()) {
                login()
            } else {
                Toast.makeText(this, "One or both fields are empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login() {
        val api = RetrofitInstance.getInstance().create(WebService::class.java)
        api.login(Login(usernameEt.text.toString(), passwordTil.text.toString())).enqueue(object :
            Callback<AuthToken> {
            override fun onFailure(call: Call<AuthToken>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(call: Call<AuthToken>, response: Response<AuthToken>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        PrefManager.getMyPrefs(this@LoginActivity).edit().putString(Const.PREF_AUTH_TOKEN, "Jwt " + it.token).apply()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                } else {
                    showError()
                }
            }

        })
    }

    private fun checkInputs() = !usernameEt.text.isNullOrEmpty() && !passwordTil.text.isNullOrEmpty()

    private fun showError() {
        passwordErrorTv.visibility = View.VISIBLE
        val drawable = passwordTil.background as GradientDrawable
        drawable.setStroke(5, Color.RED)
    }

    private fun hideError() {
        passwordErrorTv.visibility = View.INVISIBLE
        val drawable = passwordTil.background as GradientDrawable
        drawable.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
    }
}