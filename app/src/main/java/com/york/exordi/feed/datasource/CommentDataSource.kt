package com.york.exordi.feed.datasource

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Comment
import com.york.exordi.models.CommentResult
import com.york.exordi.models.SearchUserResult
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.network.WebServiceInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentDataSource(val context: Context, var postId: String) :
    PageKeyedDataSource<String, CommentResult>() {

    companion object {
        private const val TAG = "CommentDataSource"
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
        callback: LoadInitialCallback<String, CommentResult>
    ) {
        webService.getComments(getAuthToken(), postId).enqueue(object : Callback<Comment> {
            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
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

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, CommentResult>
    ) {
        webService.getAdjacentComments(getAuthToken(), params.key).enqueue(object: Callback<Comment> {
            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
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

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, CommentResult>
    ) {
        webService.getAdjacentComments(getAuthToken(), params.key).enqueue(object :
            Callback<Comment> {
            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
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