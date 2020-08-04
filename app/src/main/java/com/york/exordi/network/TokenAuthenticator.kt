package com.york.exordi.network

import android.content.Context
import com.york.exordi.models.AuthToken
import com.york.exordi.shared.Const
import com.york.exordi.shared.PrefManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Call

class TokenAuthenticator(val context: Context, var webService: WebServiceInstance?) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (webService == null) {
            return null
        }
        val prefs = PrefManager.getMyPrefs(context)
        val token = prefs.getString(Const.PREF_AUTH_TOKEN, null)?.substring(4)

//        val retrofitResponse: retrofit2.Response<AuthToken>? = webService!!.webService!!.refreshToken(AuthToken(token!!)).execute()
//        retrofitResponse?.let {
//            prefs.edit()
//                .putString(Const.PREF_AUTH_TOKEN, "Jwt " + it.body()!!.access)
//                .putString(Const.PREF_REFRESH_TOKEN, it.body()!!.refresh).apply()
//
//            return response.request.newBuilder().header("Authorization", "Jwt " + it.body()!!.access).build()
//        }
        return null
    }


}