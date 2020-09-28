package com.york.exordi.login.email

import android.content.Intent
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.york.exordi.R
import com.york.exordi.base.BaseActivity
import com.york.exordi.models.*
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_email_step_three.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailStepThreeActivity : BaseActivity() {

    private val TAG = "EmailStepThreeActivity"

    private var birthDay: String? = null
    private var country: String? = null

    private var emailDrawable: GradientDrawable? = null
    private var usernameDrawable: GradientDrawable? = null
    private var passwordDrawable: GradientDrawable? = null
    private var confirmPasswordDrawable: GradientDrawable? = null

    private val api = RetrofitInstance.SimpleInstance.get().create(WebService::class.java)

    private var isEmailValid = MutableLiveData<Boolean>(false)
    private var isUsernameValid = MutableLiveData(false)
    private var isPasswordValid = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_step_three)

        birthDay = intent.getStringExtra(Const.EXTRA_BIRTHDATE)
        country = intent.getStringExtra(Const.EXTRA_COUNTRY)

        setupObservers()
        backBtn.setOnClickListener { finish() }
        registerBtn.setOnClickListener {
            if (areInputsValid()) {
                it.isEnabled = false
                makeInternetSafeRequest { registerUser() }
            } else {
                Toast.makeText(this, "One or more fields are empty!", Toast.LENGTH_SHORT).show()
            }
        }
        emailDrawable = emailEt.background as GradientDrawable
        usernameDrawable = usernameEt.background as GradientDrawable
        passwordDrawable = passwordEt.background as GradientDrawable
        confirmPasswordDrawable = confirmPasswordEt.background as GradientDrawable

        hideUsernameError()
        hideEmailError()
        hideError(passwordDrawable, passwordErrorTv)
        hideError(confirmPasswordDrawable, confirmPasswordErrorTv)

        emailEt.doOnTextChanged {text, _, _, _ ->
            if (!TextUtils.isEmpty(text)) {
                if (checkEmailLocally(text)) {
                    hideEmailError()
                    makeInternetSafeRequest { checkEmail(text!!) }
                } else {
                    isEmailValid.value = false
                    showEmailError("Email is not valid")
                }
            } else {
                isEmailValid.value = false
            }
        }
        usernameEt.doOnTextChanged { text, _, _, _ ->
            if (!TextUtils.isEmpty(text)) {
                when {
                    text!!.length < 4 -> {
                        isUsernameValid.value = false
                        showUsernameError("Username is too short")
                    }
                    text.length > 15 -> {
                        isUsernameValid.value = false
                        showUsernameError("Username is too long")
                    }
                    else -> {
                        if (text.toString().matches("^[a-zA-Z0-9_-]{4,15}\$".toRegex())) {
                            makeInternetSafeRequest { checkUsername(text.toString()) }
                        } else {
                            isUsernameValid.value = false
                            showUsernameError("Username must only contain letters a-z, numbers 0-9, and underscores")
                        }
                    }
                }
            } else {
                isUsernameValid.value = false
            }
        }
        passwordEt.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length < 8) {
                isPasswordValid.value = false
                showPasswordError(passwordErrorTv)
            } else {
                isPasswordValid.value = true
                hideError(passwordDrawable, passwordErrorTv)
            }
        }

        confirmPasswordEt.doOnTextChanged { text, start, before, count ->
            if (text.toString() != passwordEt.text.toString()) {
                isPasswordValid.value = false
                showPasswordError(confirmPasswordErrorTv)
            } else {
                isPasswordValid.value = true
                hideError(confirmPasswordDrawable, confirmPasswordErrorTv)
                hideError(passwordDrawable, passwordErrorTv)
            }
        }
    }

    private fun setupObservers() {
        isEmailValid.observe(this) {
            checkAllInputs()
        }
        isUsernameValid.observe(this) {
            checkAllInputs()
        }
        isPasswordValid.observe(this) {
            checkAllInputs()
        }
    }

    private fun checkAllInputs() {
        if (isUsernameValid.value!! && isPasswordValid.value!! && isEmailValid.value!!) {
            if (arePasswordsEqual()) {
                registerBtn.visibility = View.VISIBLE
            }
        } else {
            registerBtn.visibility = View.GONE
        }
    }

    private fun checkUsername(username: String) {
        usernamePb.visibility = View.VISIBLE
        api.checkUsername(UsernameCheck(username)).enqueue(object: Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                usernamePb.visibility = View.INVISIBLE
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.message == "OK") {
                            isUsernameValid.value = true
                            usernameTick.visibility = View.VISIBLE
                            hideUsernameError()
                        }
                    }
                } else {
                    isUsernameValid.value = false
                    showUsernameError("User with the given username already exists")
                }
            }

        })
    }

    private fun checkEmail(email: CharSequence) {
        emailPb.visibility = View.VISIBLE
        api.checkEmail(EmailCheck(email.toString())).enqueue(object: Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                emailPb.visibility = View.INVISIBLE
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.message == "OK") {
                            isEmailValid.value = true
                            emailTick.visibility = View.VISIBLE
                            hideEmailError()
                        }
                    }
                } else {
                    isEmailValid.value = false
                    showEmailError("User with the given email already exists")
                }
            }

        })
    }

    private fun checkEmailLocally(email: CharSequence?): Boolean = email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun showEmailError(error: String) {
        emailTick.visibility = View.INVISIBLE
        emailErrorTv.text = error
        emailErrorTv.visibility = View.VISIBLE
        emailDrawable?.setStroke(5, Color.RED)
    }

    private fun hideEmailError() {
        emailErrorTv.visibility = View.INVISIBLE
        emailDrawable?.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
    }

    private fun showUsernameError(error: String) {
        usernameTick.visibility = View.INVISIBLE
        usernameErrorTv.text = error
        usernameErrorTv.visibility = View.VISIBLE
        usernameDrawable?.setStroke(5, Color.RED)
    }

    private fun hideUsernameError() {
        usernameErrorTv.visibility = View.INVISIBLE
        usernameDrawable?.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
    }

    private fun showPasswordError(textView: TextView) {
        passwordDrawable?.setStroke(5, Color.RED)
        textView.visibility = View.VISIBLE
        if (textView == passwordErrorTv) {
            textView.text = "Your password is too short"
            textView.setTextColor(Color.RED)
        }
    }

    private fun hideError(drawable: GradientDrawable?, textView: TextView) {
        drawable?.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
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
                && arePasswordsEqual())
    }

    private fun arePasswordsEqual(): Boolean = passwordEt.text.toString() == confirmPasswordEt.text.toString()

    private fun registerUser() {
        val userData = UserRegistration(
            usernameEt.text.toString(),
            emailEt.text.toString(),
            passwordEt.text.toString(),
            country ?: "United Kingdom",
            birthDay ?: "19 August 1994")
        api.registerUser(userData).enqueue(object: Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message)
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                registerBtn.isEnabled = true
                if (response.isSuccessful) {
                    response.body()?.let {
                        Toast.makeText(this@EmailStepThreeActivity, it.message!!, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@EmailStepThreeActivity, CodeInputActivity::class.java).apply {
                            putExtra(Const.EXTRA_USERNAME, usernameEt.text.toString())
                        })
                    }
                }
            }

        })

    }
}