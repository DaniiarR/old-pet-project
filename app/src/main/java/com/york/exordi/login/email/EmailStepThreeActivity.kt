package com.york.exordi.login.email

import android.app.Instrumentation
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.york.exordi.R
import com.york.exordi.models.UserRegistration
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_email_step_three.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailStepThreeActivity : AppCompatActivity() {

    private val TAG = "EmailStepThreeActivity"

    private var birthDay: String? = null
    private var country: String? = null
    private var email: String? = null
    private var username: String? = null
    private var password: String? = null
    private var arePasswordsEqual = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_step_three)

        birthDay = intent.getStringExtra(Const.EXTRA_BIRTHDATE)
        country = intent.getStringExtra(Const.EXTRA_COUNTRY)
        backBtn.setOnClickListener { finish() }
        registerBtn.setOnClickListener {
            if (areInputsValid()) {
                makeInternetSafeRequest { registerUser() }
            } else {
                Toast.makeText(this, "One or more fields are empty!", Toast.LENGTH_SHORT).show()
            }
        }
        val emailDrawable = emailEt.background as GradientDrawable
        val usernameDrawable = usernameEt.background as GradientDrawable
        val passwordDrawable = passwordEt.background as GradientDrawable
        val confirmPasswordDrawable = confirmPasswordEt.background as GradientDrawable

        passwordEt.doOnTextChanged { text, start, before, count ->
            if (text.toString().length < 8) {
                showError(passwordDrawable, passwordErrorTv)
            } else {
                hideError(passwordDrawable, passwordErrorTv)
            }
        }

        confirmPasswordEt.doOnTextChanged { text, start, before, count ->
            if (text.toString() != passwordEt.text.toString()) {
                showError(confirmPasswordDrawable, confirmPasswordErrorTv)
            } else {
                hideError(confirmPasswordDrawable, confirmPasswordErrorTv)
            }
        }
    }

    private fun showError(drawable: GradientDrawable, textView: TextView) {
        drawable.setStroke(5, Color.RED)
        textView.visibility = View.VISIBLE
        if (textView == passwordErrorTv) {
            textView.text = "Your password is too short"
            textView.setTextColor(Color.RED)
        }
    }

    private fun hideError(drawable: GradientDrawable, textView: TextView) {
        drawable.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
        textView.visibility = View.INVISIBLE
        if (textView == passwordErrorTv) {
            textView.text = "Your password must be at least 8 characters long"
            textView.setTextColor(ContextCompat.getColor(this, R.color.textColorSecondary))

        }
    }

    private fun areInputsValid(): Boolean {
        return (!TextUtils.isEmpty(emailEt.text)
                && !TextUtils.isEmpty(usernameEt.text)
                && !TextUtils.isEmpty(passwordEt.text)
                && !TextUtils.isEmpty(confirmPasswordEt.text)
                && passwordEt.text.toString() == confirmPasswordEt.text.toString())
    }

    private fun registerUser() {
        val api = RetrofitInstance.getInstance().create(WebService::class.java)
        val userData = UserRegistration(
            usernameEt.text.toString(),
            emailEt.text.toString(),
            passwordEt.text.toString(),
            country ?: "United Kingdom",
            birthDay ?: "19 August 1994")
        api.registerUser(userData).enqueue(object: Callback<UserRegistration> {
            override fun onFailure(call: Call<UserRegistration>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message)
            }

            override fun onResponse(
                call: Call<UserRegistration>,
                response: Response<UserRegistration>
            ) {

            }

        })

    }
}