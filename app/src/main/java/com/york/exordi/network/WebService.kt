package com.york.exordi.network

import com.york.exordi.models.*
import com.york.exordi.shared.Const
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface WebService {

    /** Authentication section */
    @POST("user/validate/email/")
    fun checkEmail(@Body email: EmailCheck): Call<ResponseMessage>

    @POST("user/validate/username/")
    fun checkUsername(@Body username: UsernameCheck): Call<ResponseMessage>

    @POST("user/register/")
    fun registerUser(@Body user: UserRegistration): Call<ResponseMessage>

    @POST("user/activate/")
    fun activateUser(@Body activationCode: ActivationCode): Call<LoginToken>

    @GET("user/activate/repeat/")
    fun resendCode(@Body username: String): Call<ResponseMessage>

    @POST("auth/token/")
    fun login(@Body login: Login): Call<LoginToken>

    @POST("auth/token/refresh/")
    fun refreshToken(@Body authToken: RefreshToken): Call<LoginToken>

    /** Feed Section */
    @GET("user/me/")
    fun getProfileInfo(@Header(Const.AUTH) authToken: String): Call<Profile>

    @Multipart
    @PATCH("user/me/")
    fun editProfile(@Header(Const.AUTH) authToken: String, @Part("username") username: String, @Part("bio") bio: String, @Part photo: MultipartBody.Part?): Call<Profile>

    @Multipart
    @PATCH("user/me/")
    fun editDescription(@Header(Const.AUTH) authToken: String, @Part("bio") bio: String, @Part photo: MultipartBody.Part?): Call<Profile>

    @Multipart
    @POST("post/")
    fun createPost(@Header(Const.AUTH) authToken: String, @Part("category") category: Int, @Part("file_type") fileType: String, @Part file: MultipartBody.Part, @Part("text") description: String?): Call<AddPostResponse>

    @GET("post")
    fun getAllPosts(@Header(Const.AUTH) authToken: String, @Query("category") category: Int, @Query("order") order: String): Call<Post>

    @GET
    fun getAdjacentPosts(@Header(Const.AUTH) authToken: String, @Url url: String): Call<Post>

    @GET("category")
    fun getAllCategories(@Header(Const.AUTH) authToken: String): Call<Category>

    @POST("post/upvote/")
    fun toggleUpvote(@Header(Const.AUTH) authToken: String, @Body postId: PostId): Call<ResponseMessage>


    @GET("post/comment/{id}/")
    fun getComments(@Header(Const.AUTH) authToken: String, @Path("id") postId: String): Call<Comment>

    @POST("post/comment/{id}/")
    fun postComment(@Header(Const.AUTH) authToken: String, @Path("id") postId: String, @Body comment: CommentText): Call<ResponseMessage>

    @HTTP(method = "DELETE", path = "post/comment/edit/{id}/", hasBody = true)
    fun deleteComment(@Header(Const.AUTH) authToken: String, @Path("id") commentId: Int): Call<ResponseMessage>

    @GET
    fun getAdjacentComments(@Header(Const.AUTH) authToken: String, @Url url: String): Call<Comment>

    @GET("user/{username}/")
    fun getOtherProfile(@Header(Const.AUTH) authToken: String, @Path("username") username: String): Call<OtherProfile>

    @GET("post")
    fun getUserPosts(@Header(Const.AUTH) authToken: String, @Query("author") author: Int): Call<Post>

    @POST("user/follow/")
    fun followUser(@Header(Const.AUTH) authToken: String, @Body user: Username): Call<ResponseMessage>

    @GET("user/{user}/{mode}/")
    fun getFollowers(@Header(Const.AUTH) authToken: String, @Path("mode") mode: String, @Path("user") user: String = "me"): Call<Follower>

    @GET
    fun getAdjacentFollowers(@Header(Const.AUTH) authToken: String, @Url url: String): Call<Follower>
    
    @GET("user")
    fun searchUser(@Header(Const.AUTH) authToken: String, @Query("q") username: String): Call<SearchUser>

    @GET
    fun searchAdjacentUser(@Header(Const.AUTH) authToken: String, @Url url: String): Call<SearchUser>

    @DELETE("post/{id}/")
    fun deletePost(@Header(Const.AUTH) authToken: String, @Path("id") postId: String): Call<ResponseMessage>

}

class WebServiceInstance {

    companion object {

        private var instance: WebServiceInstance? = null
        fun get() = instance ?: WebServiceInstance()

    }
    var webService: WebService? = null

}