package com.york.exordi.login.email

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.york.exordi.MainActivity
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.york.exordi.R
import com.york.exordi.models.ActivationCode
import com.york.exordi.models.AuthToken
import com.york.exordi.models.ResponseMessage
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.makeInternetSafeRequest
import kotlinx.android.synthetic.main.activity_code_input.*

class CodeInputActivity : AppCompatActivity() {

    private val TAG = "CodeInputActivity"

    val api = RetrofitInstance.getInstance().create(WebService::class.java)

    private var backgroundDrawable: GradientDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_input)

        backgroundDrawable = codeTiL.background as GradientDrawable
        backBtn.setOnClickListener { finish() }

        codeTiL.doOnTextChanged { text, _, _, count ->
            if (text?.length == 4) {
                codePb.visibility = View.VISIBLE
                makeInternetSafeRequest { activateUser() }
            } else {
                hideCodeError()
            }
        }
        sendAgainBtn.setOnClickListener {
            makeInternetSafeRequest { sendCodeAgain() }
        }
    }

    private fun sendCodeAgain() {
        api.resendCode(intent.getStringExtra(Const.EXTRA_USERNAME)!!).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CodeInputActivity, "The code was sent to your email address", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun activateUser() {
        api.activateUser(ActivationCode(codeTiL.text.toString().toInt())).enqueue(object :
            Callback<AuthToken> {
            override fun onFailure(call: Call<AuthToken>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(call: Call<AuthToken>, response: Response<AuthToken>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        PrefManager.getMyPrefs(this@CodeInputActivity).edit()
                            .putString(Const.PREF_AUTH_TOKEN, "Jwt " + it.token).apply()
                        startActivity(Intent(this@CodeInputActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    showCodeError()
                }
            }

        })
    }

    private fun showCodeError() {
        codePb.visibility = View.INVISIBLE
        codeErrorTv.visibility = View.VISIBLE
        backgroundDrawable?.setStroke(5, Color.RED)
    }

    private fun hideCodeError() {
        codePb.visibility = View.INVISIBLE
        codeErrorTv.visibility = View.GONE
        backgroundDrawable?.setStroke(2, ContextCompat.getColor(this, R.color.buttonBackgroundColor))
    }
}