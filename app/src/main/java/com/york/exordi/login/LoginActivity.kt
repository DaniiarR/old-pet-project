package com.york.exordi.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.york.exordi.base.BaseActivity
import com.york.exordi.MainActivity
import com.york.exordi.R
import com.york.exordi.models.*
import com.york.exordi.network.AuthWebWebService
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.fbLoginBtn
import kotlinx.android.synthetic.main.activity_login.signInBtn
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : BaseActivity() {

    private val TAG = "LoginActivity"

    private var googleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGN_IN_RC = 100

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.EMAIL))
            .requestServerAuthCode("214536121200-0eelqcmuaqbe0shjpk9pdhvcp4hc5adk.apps.googleusercontent.com")
//                .requestServerAuthCode("663429823844-a2sjdhehrmeu7rij1h77ok9ij0trfeof.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
        googleLoginBtn.setOnClickListener {
            startActivityForResult(googleSignInClient?.signInIntent, GOOGLE_SIGN_IN_RC)
        }
        facebookLoginBtn.setOnClickListener {
            fbLoginBtn.performClick()
        }
        callbackManager = CallbackManager.Factory.create()
        fbLoginBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                result?.let {
                    sendTokenToBackend("facebook", it.accessToken.token)
                }
            }

            override fun onCancel() {
                TODO("Not yet implemented")
            }

            override fun onError(error: FacebookException?) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_SIGN_IN_RC -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.serverAuthCode
            idToken?.let {
                sendCodeToGoogle(it)
            }
//            idToken?.let { sendTokenToBackend("google-oauth2", it) }
        } catch (e: ApiException) {

        }
    }

    private fun sendCodeToGoogle(authCode: String) {
        val policy = StrictMode.ThreadPolicy.Builder().permitNetwork().build()
        StrictMode.setThreadPolicy(policy)
        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            "https://oauth2.googleapis.com/token",
            "214536121200-0eelqcmuaqbe0shjpk9pdhvcp4hc5adk.apps.googleusercontent.com",
            "MY-XWazTLJ7A9vC1xsdsiUnQ",
            authCode,
            ""
        ).execute()
        val accessToken = tokenResponse.accessToken
        Log.e(TAG, "sendCodeToGoogle: " + "access token: " + accessToken )
        sendTokenToBackend("google-oauth2", accessToken)
    }

    private fun sendTokenToBackend(provider: String, token: String) {
        val api = getRetrofit().create(AuthWebWebService::class.java)
        Log.e(TAG, "sendTokenToBackend: " + token)
        api.login(LoginCredentials(provider, token)).enqueue(object :
            Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.code == "200") {
                            body.data.accessToken?.let { token ->
                                PrefManager.getMyPrefs(this@LoginActivity).edit()
                                    .putString(Const.PREF_AUTH_TOKEN, "JWT " + token)
                                    .putString(Const.PREF_REFRESH_TOKEN, body.data.refreshToken)
                                    .commit()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Could not authenticate with $provider",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        })

    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://softloft.xyz/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun login() {
        val api = RetrofitInstance.getInstance().create(WebService::class.java)
        api.login(Login(usernameEt.text.toString(), passwordTil.text.toString())).enqueue(object :
            Callback<LoginToken> {
            override fun onFailure(call: Call<LoginToken>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(call: Call<LoginToken>, response: Response<LoginToken>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.i(TAG, "onResponse: " + "Jwt " + it.data.access)
                        PrefManager.getMyPrefs(this@LoginActivity).edit()
                            .putString(Const.PREF_AUTH_TOKEN, "JWT " + it.data.access)
                            .putString(Const.PREF_REFRESH_TOKEN, it.data.refresh).apply()
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