package com.york.exordi.network

import com.york.exordi.models.UserRegistration
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface WebService {

    @POST("auth/registration")
    fun registerUser(@Body user: UserRegistration): Call<UserRegistration>

}