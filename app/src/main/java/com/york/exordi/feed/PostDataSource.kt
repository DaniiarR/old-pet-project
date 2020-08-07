package com.york.exordi.feed

import android.app.Application
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Post
import com.york.exordi.models.Result
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDataSource(val application: Application) : PageKeyedDataSource<String, Result>() {

    companion object {
        private const val TAG = "PostDataSource"
    }

    private val api = RetrofitInstance.getInstance().create(WebService::class.java)
    private val prefs = PrefManager.getMyPrefs(application.applicationContext)

    private fun getAuthToken(): String = prefs.getString(Const.PREF_AUTH_TOKEN, null) ?: ""


    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Result>
    ) {
        api.getAllPosts(getAuthToken()).enqueue(object : Callback<Post> {
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
        api.getPreviousPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
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
        api.getNextPosts(getAuthToken(), params.key).enqueue(object : Callback<Post> {
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