package com.york.exordi.network

import com.york.exordi.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WebService {

    @POST("user/validator-email")
    fun checkEmail(@Body email: EmailCheck): Call<ResponseMessage>

    @POST("user/validator-username")
    fun checkUsername(@Body username: UsernameCheck): Call<ResponseMessage>

    @POST("user/register/")
    fun registerUser(@Body user: UserRegistration): Call<ResponseMessage>

    @POST("user/user-activation/")
    fun activateUser(@Body activationCode: ActivationCode): Call<AuthToken>

}