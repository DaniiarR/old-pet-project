package com.york.exordi.shared

import android.content.Context
import android.content.SharedPreferences

class PrefManager {

    companion object {
        @Volatile
        private var sharedPreferences: SharedPreferences? = null

        fun getMyPrefs(context: Context) = sharedPreferences
            ?: context.getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
    }

}