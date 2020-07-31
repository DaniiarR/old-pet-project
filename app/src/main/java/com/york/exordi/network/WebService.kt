package com.york.exordi.network

import com.york.exordi.models.*
import com.york.exordi.shared.Const
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface WebService {

    /** Authentication section */
    @POST("user/validator-email")
    fun checkEmail(@Body email: EmailCheck): Call<ResponseMessage>

    @POST("user/validator-username")
    fun checkUsername(@Body username: UsernameCheck): Call<ResponseMessage>

    @POST("user/register/")
    fun registerUser(@Body user: UserRegistration): Call<ResponseMessage>

    @POST("user/user-activation/")
    fun activateUser(@Body activationCode: ActivationCode): Call<LoginToken>

    @GET("user/user-activation-repeat/{username}/")
    fun resendCode(@Path("username") username: String): Call<ResponseMessage>

    @POST("user/login/")
    fun login(@Body login: Login): Call<LoginToken>

    @POST("user/token-refresh/")
    fun refreshToken(@Body authToken: AuthToken): Call<AuthToken>

    /** Feed Section */
    @GET("user/me/")
    fun getProfileInfo(@Header(Const.AUTH) authToken: String): Call<Profile>

    @Multipart
    @PATCH("user/me/")
    fun editProfile(@Header(Const.AUTH) authToken: String, @Part("username") username: RequestBody, @Part("bio") bio: RequestBody, @Part photo: MultipartBody.Part?): Call<Profile>

    @Multipart
    @PATCH("user/me/")
    fun editDescription(@Header(Const.AUTH) authToken: String, @Part("bio") bio: RequestBody, @Part photo: MultipartBody.Part?): Call<Profile>

    @Multipart
    @POST("post/")
    fun createPost(@Header(Const.AUTH) authToken: String, @Part("category") category: Int, @Part file: MultipartBody.Part, @Part("text") description: String?): Call<AddPostResponse>

}

class WebServiceInstance {

    var webService: WebService? = null
}