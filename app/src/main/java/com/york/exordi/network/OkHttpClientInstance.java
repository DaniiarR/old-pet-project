package com.york.exordi.network;

import android.content.Context;

import com.york.exordi.shared.Const;
import com.york.exordi.shared.PrefManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientInstance {

    public static class Builder {

        private Context context;
        private WebServiceInstance webServiceInstance;

        public Builder(Context context, WebServiceInstance webServiceInstance) {
            this.context = context;
            this.webServiceInstance = webServiceInstance;
        }

        public OkHttpClient build() {
            TokenAuthenticator authenticator = new TokenAuthenticator(context, webServiceInstance);
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {

                        @NotNull
                        @Override
                        public Response intercept(@NotNull Chain chain) throws IOException {
                            // Add default headers
                            Request.Builder requestBuilder = chain.request().newBuilder()
                                    .addHeader("accept", "*/*")
                                    .addHeader("accept-encoding", "gzip, deflate, br")
//                                    .addHeader("accept-language", "en-US,en;q=0.9")
                                    .addHeader("connection", "keep-alive")
                                    .addHeader("Content-Type", "application/json");

                            if (context != null) {
                                String token = PrefManager.Companion.getMyPrefs(context).getString(Const.PREF_AUTH_TOKEN, null);
                                if (token != null) {
                                    requestBuilder.addHeader("Authorization", token);
                                }
                            }
                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .addInterceptor(interceptor)
                    .connectTimeout(0, TimeUnit.SECONDS)
                    .writeTimeout(0, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS);

            okHttpClientBuilder.authenticator(authenticator);
            return okHttpClientBuilder.build();
        }
    }
}
