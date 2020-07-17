package com.york.exordi.network

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitInstance {

    companion object {
        private val BASE_URL = "http://46.101.142.120:8000/api/"

        @Volatile
        private var retrofit: Retrofit? = null
        fun getInstance(): Retrofit = retrofit
            ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(attachInterceptor())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        private fun attachInterceptor(): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            return OkHttpClient.Builder().addInterceptor(interceptor).build()

        }
    }
}