package com.york.exordi.network

import com.york.exordi.models.*
import com.york.exordi.shared.Const
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
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
    fun activateUser(@Body activationCode: ActivationCode): Call<AuthToken>

    @GET("user/user-activation-repeat/{username}/")
    fun resendCode(@Path("username") username: String): Call<ResponseMessage>

    @POST("user/login/")
    fun login(@Body login: Login): Call<AuthToken>

    @POST("user/token-refresh/")
    fun refreshToken(@Body authToken: AuthToken): Call<AuthToken>

    /** Feed Section */
    @GET("user/me/")
    fun getProfileInfo(@Header(Const.AUTH) authToken: String): Call<Profile>

    @PATCH("user/me/")
    fun editProfile(@Header(Const.AUTH) authToken: String, @Body profile: EditProfile): Call<Profile>

    @Multipart
    @POST("user/avatar/upload/")
    fun editProfilePhoto(@Header(Const.AUTH) authToken: String, @Part profilePhoto: MultipartBody.Part): Call<ResponseMessage>

}

class WebServiceInstance {

    var webService: WebService? = null
}