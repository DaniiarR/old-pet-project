package com.york.exordi.feed.datasource

import android.app.Application
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Post
import com.york.exordi.models.Result
import com.york.exordi.network.*
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDataSource(val application: Application, var categoryId: Int, var order: String) : PageKeyedDataSource<String, Result>() {

    companion object {
        private const val TAG = "PostDataSource"
//        private const val BASE_URL = "https://softloft.xyz/api/"

    }

    private val webServiceHolder = WebServiceInstance.get()
//    private val okHttpClient = OkHttpClientInstance.Builder(application.applicationContext, webServiceHolder).build()
//    private var okHttpClient = OkHttpClient().newBuilder().authenticator(TokenAuthenticator(application.applicationContext, webServiceHolder)).build()
//    private val webService = retrofit2.Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
//        .addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(
//            GsonConverterFactory.create()).build().create(WebService::class.java)
    private val webService = RetrofitInstance.getInstance(application.applicationContext, webServiceHolder).create(WebService::class.java)

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
        this.webService.getAllPosts(getAuthToken(), categoryId, order).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " +  t.message!!)
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(setCurrentUserPosts(it.data.results), null, it.data.next)
                        }
                    }
                }
            }
        })
    }

    private fun setCurrentUserPosts(posts: List<Result>): List<Result> {
        val username = PrefManager.getMyPrefs(application.applicationContext).getString(Const.PREF_USERNAME, null)
        username?.let { name ->
            for (post in posts) {
                posts.forEach {
                    if (it.author.username == name) {
                        it.isCurrentUserPost = true
                    }
                }
            }
        }
        return posts
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Result>) {
        this.webService.getAdjacentPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(setCurrentUserPosts(it.data.results), it.data.previous)
                        }
                    }
                }
            }

        })
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Result>) {
        this.webService.getAdjacentPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(setCurrentUserPosts(it.data.results), it.data.next)
                        }
                    }
                }
            }
        })
    }
}