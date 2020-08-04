package com.york.exordi.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.models.*
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppRepository(application: Application) {

    private val TAG = "AppRepository"
    private val BASE_URL = "https://softloft.xyz/api/"

    companion object {
        private var repository: AppRepository? = null

        fun getInstance(application: Application): AppRepository = repository ?: AppRepository(application)
    }

    private val prefs = PrefManager.getMyPrefs(application.applicationContext)
//    private val webServiceHolder = WebServiceInstance()
//    private val okHttpClient = OkHttpClientInstance.Builder(application.applicationContext, webServiceHolder).build()
//    private val webService = retrofit2.Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
//        .addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(
//            GsonConverterFactory.create()).build().create(WebService::class.java)
    val webService = RetrofitInstance.getInstance().create(WebService::class.java)
//    init {
//        webServiceHolder.webService = webService
//    }

    private fun getAuthToken(): String = prefs.getString(Const.PREF_AUTH_TOKEN, null) ?: ""

    fun checkUsername(usernameCheck: UsernameCheck, callback: (String) -> Unit) {
        webService.checkUsername(usernameCheck).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { callback(it.message!!) }
                }
            }
        })
    }

    fun getProfileInfo(profile: MutableLiveData<Profile>) {
        Log.i(TAG, "getProfileInfo: " + getAuthToken())
        webService.getProfileInfo(getAuthToken()).enqueue(object : Callback<Profile> {
            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        profile.value = it
                    }
                }
            }

        })
    }

    fun editProfile(
        username: String,
        description: String,
        photo: MultipartBody.Part?,
        callback: (Profile?) -> Unit
    ) {
        webService.editProfile(getAuthToken(), username, description, photo).enqueue(object : Callback<Profile> {
            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it)
                    }
                } else {
                    callback(null)
                }
            }

        })
    }

    fun editDescription(
        description: String,
        photo: MultipartBody.Part?,
        callback: (Profile?) -> Unit
    ) {
        Log.e(TAG, "editDescription: " + getAuthToken() )
        webService.editDescription(getAuthToken(), description, photo).enqueue(object : Callback<Profile> {
            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it)
                    }
                } else {
                    callback(null)
                }
            }

        })
    }

    fun createPost(category: Int, description: String?, file: MultipartBody.Part, callback: (AddPostResponse) -> Unit) {
        webService.createPost(getAuthToken(), category, file, description).enqueue(object :
            Callback<AddPostResponse> {
            override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!!)
            }

            override fun onResponse(
                call: Call<AddPostResponse>,
                response: Response<AddPostResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it)
                    }
                }
            }
        })
    }
}