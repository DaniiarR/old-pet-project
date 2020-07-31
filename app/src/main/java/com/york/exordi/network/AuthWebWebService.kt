package com.york.exordi.network

import com.york.exordi.models.LoginCredentials
import com.york.exordi.models.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthWebWebService {

    @POST("user/oauth/login/")
    fun login(@Body credentials: LoginCredentials): Call<LoginResponse>
}