package com.york.exordi.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.york.exordi.events.CreatePostEvent
import com.york.exordi.models.*
import com.york.exordi.network.OkHttpClientInstance
import com.york.exordi.network.RetrofitInstance
import com.york.exordi.network.WebService
import com.york.exordi.network.WebServiceInstance
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class AppRepository(application: Application) {

    private val TAG = "AppRepository"
    private val BASE_URL = "https://softloft.xyz/api/"

    companion object {
        private var repository: AppRepository? = null

        fun getInstance(application: Application): AppRepository = repository ?: AppRepository(application)
    }

    private val prefs = PrefManager.getMyPrefs(application.applicationContext)
    private val webServiceHolder = WebServiceInstance.get()
//    private val okHttpClient = OkHttpClientInstance.Builder(application.applicationContext, webServiceHolder).build()
//    private val webService = retrofit2.Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
//        .addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(
//            GsonConverterFactory.create()).build().create(WebService::class.java)
//    val webService = RetrofitInstance.getInstance().create(WebService::class.java)
//    init {
//        webServiceHolder.webService = webService
//    }

    private val webService = RetrofitInstance.getInstance(application.applicationContext, webServiceHolder).create(WebService::class.java)

    init {
        webServiceHolder.webService = this.webService
    }

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

    fun createPost(fileType: String, category: Int, description: String?, file: MultipartBody.Part, callback: (AddPostResponse) -> Unit) {
        webService.createPost(getAuthToken(), category, fileType, file, description).enqueue(object :
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

    fun getCategories(callback: (List<CategoryData>) -> Unit) {
        webService.getAllCategories(getAuthToken()).enqueue(object : Callback<Category> {
            override fun onFailure(call: Call<Category>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(call: Call<Category>, response: Response<Category>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            callback(it.data)

                        }
                    }
                }
            }
        })
    }

    fun toggleUpvote(postId: PostId, callback: (Boolean) -> Unit) {
        webService.toggleUpvote(getAuthToken(), postId).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it.message == "OK")
                    }
                }
            }
        })
    }

    fun getOtherUserProfile(username: String, profile: MutableLiveData<OtherProfileData>) {
        webService.getOtherProfile(getAuthToken(), username).enqueue(object :
            Callback<OtherProfile> {
            override fun onFailure(call: Call<OtherProfile>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(call: Call<OtherProfile>, response: Response<OtherProfile>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.code == 200) {
                            profile.value = it.data
                        }
                    }
                }
            }
        })
    }

    fun followUser(username: Username, callback: (Boolean) -> Unit) {
        webService.followUser(getAuthToken(), username).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it.code == 200)
                    }
                }
            }
        })
    }

    fun commentPost(comment: CommentText, postId: String, callback: (Boolean) -> Unit) {
        webService.postComment(getAuthToken(), postId, comment).enqueue(object :
            Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it.code == 200)
                    }
                }
            }
        })
    }

    fun deleteComment(commentId: Int, callback: (Boolean) -> Unit) {
        webService.deleteComment(getAuthToken(), commentId).enqueue(object :
            Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it.code == 200)
                    }
                }
            }
        })
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        webService.deletePost(getAuthToken(), postId).enqueue(object: Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(it.code == 200)
                    }
                }
            }

        })
    }

    fun watchVideo(postId: PostId) {
        webService.watchVideo(getAuthToken(), postId).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message!! )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    Log.e(TAG, "onResponse: " + "video watched successfully")
                }
            }

        })
    }

    fun reportPost(post: ReportPost, callback: (Boolean) -> Unit) {
        webService.reportPost(getAuthToken(), post).enqueue(object : Callback<ResponseMessage> {
            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message )
            }

            override fun onResponse(
                call: Call<ResponseMessage>,
                response: Response<ResponseMessage>
            ) {
                callback(response.isSuccessful)
            }

        })
    }

}