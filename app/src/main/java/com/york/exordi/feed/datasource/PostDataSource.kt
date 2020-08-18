package com.york.exordi.feed

import android.app.Application
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Post
import com.york.exordi.models.Result
import com.york.exordi.network.*
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class PostDataSource(val application: Application, var categoryId: Int) : PageKeyedDataSource<String, Result>() {

    companion object {
        private const val TAG = "PostDataSource"
        private const val BASE_URL = "https://softloft.xyz/api/"

    }

    private val webServiceHolder = WebServiceInstance()
//    private val okHttpClient = OkHttpClientInstance.Builder(application.applicationContext, webServiceHolder).build()
    private var okHttpClient = OkHttpClient().newBuilder().authenticator(TokenAuthenticator(application.applicationContext, webServiceHolder)).build()
    private val webService = retrofit2.Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(
            GsonConverterFactory.create()).build().create(WebService::class.java)
        init {
        webServiceHolder.webService = this.webService
    }
//    private val webService = RetrofitInstance.getInstance().create(WebService::class.java)
    private val prefs = PrefManager.getMyPrefs(application.applicationContext)

    private fun getAuthToken(): String = prefs.getString(Const.PREF_AUTH_TOKEN, null) ?: ""


    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Result>
    ) {
        Log.e(TAG, "loadInitial: " + getAuthToken())
        this.webService.getAllPosts(getAuthToken(), categoryId).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " +  t.message!!)
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(it.data.results, null, it.data.next)
                        }
                    }
                }
            }
        })
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Result>) {
        this.webService.getPreviousPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(it.data.results, it.data.previous)
                        }
                    }
                }
            }

        })
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Result>) {
        this.webService.getNextPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(it.data.results, it.data.next)
                        }
                    }
                }
            }
        })
    }
}