package com.york.exordi.network

import android.content.Context
import com.york.exordi.models.AuthToken
import com.york.exordi.models.LoginToken
import com.york.exordi.models.RefreshToken
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
        val refreshToken = prefs.getString(Const.PREF_REFRESH_TOKEN, null)

        val retrofitResponse: retrofit2.Response<LoginToken>? = webService!!.webService!!.refreshToken(
            RefreshToken(refreshToken!!)
        ).execute()
        retrofitResponse?.let {
            it.body()?.let { body ->
                prefs.edit()
                    .putString(Const.PREF_AUTH_TOKEN, "JWT " + body.data.access)
                    .putString(Const.PREF_REFRESH_TOKEN, body.data.refresh).apply()

                return response.request.newBuilder().header("Authorization", "JWT " + body.data.access).build()
            }

        }

        return null
    }


}