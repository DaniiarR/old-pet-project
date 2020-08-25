package com.york.exordi.network

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class RetrofitInstance {

    companion object {
        private const val BASE_URL = "https://exordi.dns-cloud.net/api/"

        @Volatile
        private var okHttpClient: OkHttpClient? = null
        @Volatile
        private var retrofit: Retrofit? = null
        fun getInstance(context: Context, holder: WebServiceInstance): Retrofit = retrofit
            ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient(context, holder))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        private fun getOkHttpClient(context: Context, holder: WebServiceInstance) =
            okHttpClient ?: OkHttpClient()
                .newBuilder()
                .hostnameVerifier(HostnameVerifier { _, _ -> true })
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .authenticator(TokenAuthenticator(context, holder))
//                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()

        //        private fun attachInterceptor(): OkHttpClient {
//            val interceptor = HttpLoggingInterceptor()
//            interceptor.level = HttpLoggingInterceptor.Level.BODY
//            return OkHttpClient.Builder().addInterceptor(interceptor).build()
//
//        }

    }

    class SimpleInstance {

        companion object {
            private const val BASE_URL = "https://exordi.dns-cloud.net/api/"
            @Volatile
            private var retrofit: Retrofit? = null

            fun get(): Retrofit = retrofit
                ?: Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(OkHttpClient()
                            .newBuilder()
                            .hostnameVerifier(HostnameVerifier { _, _ -> true }).build())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

        }
    }
}