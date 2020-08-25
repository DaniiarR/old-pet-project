package com.york.exordi.feed.datasource

import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.SearchUserResult
import com.york.exordi.models.Follower
import com.york.exordi.models.FollowerResult
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.network.WebServiceInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowersDataSource(val context: Context, var mode: String, var username: String?) :
    PageKeyedDataSource<String, FollowerResult>() {

    companion object {
        private const val TAG = "FollowersDataSource"
    }

    private val prefs = PrefManager.getMyPrefs(context)
    private val webServiceHolder = WebServiceInstance.get()
    private val webService = RetrofitInstance.getInstance(context, webServiceHolder).create(
        WebService::class.java)

    init {
        webServiceHolder.webService = this.webService
    }

    private fun getAuthToken(): String = prefs.getString(Const.PREF_AUTH_TOKEN, null) ?: ""
    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, FollowerResult>
    ) {
        if (username != null) {
            webService.getFollowers(getAuthToken(), mode, username!!).enqueue(object : Callback<Follower> {
                override fun onFailure(call: Call<Follower>, t: Throwable) {
                    Log.e(TAG, "onFailure: " + t.message )
                }

                override fun onResponse(call: Call<Follower>, response: Response<Follower>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.code == 200) {
                                callback.onResult(it.data.results, null, it.data.next)
                            }
                        }
                    }
                }
            })
        } else {
            webService.getFollowers(getAuthToken(), mode).enqueue(object : Callback<Follower> {
                override fun onFailure(call: Call<Follower>, t: Throwable) {
                    Log.e(TAG, "onFailure: " + t.message )
                }

                override fun onResponse(call: Call<Follower>, response: Response<Follower>) {
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
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, FollowerResult>
    ) {
        webService.getAdjacentFollowers(getAuthToken(), params.key).enqueue(object : Callback<Follower> {
            override fun onFailure(call: Call<Follower>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<Follower>, response: Response<Follower>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(it.data.results,  it.data.next)
                        }
                    }
                }
            }
        })
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, FollowerResult>
    ) {
        webService.getAdjacentFollowers(getAuthToken(), params.key).enqueue(object : Callback<Follower> {
            override fun onFailure(call: Call<Follower>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<Follower>, response: Response<Follower>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback.onResult(it.data.results,  it.data.previous)
                        }
                    }
                }
            }
        })
    }


}