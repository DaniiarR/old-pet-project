package com.york.exordi.explore

import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.SearchUserResult
import com.york.exordi.models.SearchUser
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.network.WebServiceInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserDataSource(val context: Context, var username: String) :
    PageKeyedDataSource<String, SearchUserResult>() {

    companion object {
        private const val TAG = "SearchUserDataSource"
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
        callback: LoadInitialCallback<String, SearchUserResult>
    ) {
        webService.searchUser(getAuthToken(), username).enqueue(object: Callback<SearchUser> {
            override fun onFailure(call: Call<SearchUser>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<SearchUser>, response: Response<SearchUser>) {
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

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, SearchUserResult>
    ) {
        webService.searchAdjacentUser(getAuthToken(), params.key).enqueue(object: Callback<SearchUser> {
            override fun onFailure(call: Call<SearchUser>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<SearchUser>, response: Response<SearchUser>) {
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

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, SearchUserResult>
    ) {
        webService.searchAdjacentUser(getAuthToken(), params.key).enqueue(object: Callback<SearchUser> {
            override fun onFailure(call: Call<SearchUser>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<SearchUser>, response: Response<SearchUser>) {
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
}