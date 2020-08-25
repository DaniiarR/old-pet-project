package com.york.exordi.login

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.york.exordi.MainActivity
import com.york.exordi.base.BaseActivity
import com.york.exordi.R
import com.york.exordi.login.email.EmailStepOneActivity
import com.york.exordi.models.LoginCredentials
import com.york.exordi.models.LoginResponse
import com.york.exordi.network.AuthWebWebService
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : BaseActivity() {

    companion object {
        private const val TAG = "SignUpActivity"
    }

    private var googleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGN_IN_RC = 100

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.EMAIL))
            .requestServerAuthCode("214536121200-0eelqcmuaqbe0shjpk9pdhvcp4hc5adk.apps.googleusercontent.com")
//                .requestServerAuthCode("663429823844-a2sjdhehrmeu7rij1h77ok9ij0trfeof.apps.googleusercontent.com")
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
        actualFbLoginBtn.setOnClickListener {
            fbLoginBtn.performClick()
        }
        callbackManager = CallbackManager.Factory.create()
        fbLoginBtn.setPermissions("email")
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
        val api = RetrofitInstance.SimpleInstance.get().create(AuthWebWebService::class.java)
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
                                PrefManager.getMyPrefs(this@SignUpActivity).edit()
                                    .putString(Const.PREF_AUTH_TOKEN, "JWT " + token)
                                    .putString(Const.PREF_REFRESH_TOKEN, body.data.refreshToken)
                                    .commit()
                                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
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
}