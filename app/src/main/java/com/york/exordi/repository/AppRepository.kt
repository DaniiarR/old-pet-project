package com.york.exordi.repository

import android.app.Application

class AppRepository(application: Application) {

    private val TAG = "AppRepository"
    companion object {
        private var repository: AppRepository? = null

        fun getInstance(application: Application): AppRepository = repository ?: AppRepository(application)
    }


}