package com.york.exordi.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.york.exordi.feed.EditProfileActivity
import com.york.exordi.models.EditProfile
import com.york.exordi.models.Profile
import com.york.exordi.models.ResponseMessage
import com.york.exordi.models.UsernameCheck
import com.york.exordi.network.OkHttpClientInstance
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.network.WebServiceInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class AppRepository(application: Application) {

    private val TAG = "AppRepository"
    private val BASE_URL = "http://46.101.142.120:8000/api/"

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
        profile: EditProfile,
        callback: (Boolean) -> Unit
    ) {
        webService.editProfile(getAuthToken(), profile).enqueue(object : Callback<Profile> {
            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }

        })
    }
}