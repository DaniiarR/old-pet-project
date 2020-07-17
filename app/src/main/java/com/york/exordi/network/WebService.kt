package com.york.exordi.network

import com.york.exordi.models.*
import com.york.exordi.shared.Const
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

    @POST("user/login/")
    fun login(@Body login: Login): Call<AuthToken>

    @POST("user/token-refresh/")
    fun refreshToken(@Body authToken: AuthToken): Call<AuthToken>

    /** Feed Section */
    @GET("user/me/")
    fun getProfileInfo(@Header(Const.AUTH) authToken: String): Call<Profile>

}

class WebServiceInstance {

    var webService: WebService? = null
}