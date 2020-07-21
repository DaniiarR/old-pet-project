package com.york.exordi.feed

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.york.exordi.base.BaseViewModel
import com.york.exordi.models.EditProfile
import com.york.exordi.models.Profile

class FeedViewModel(application: Application) : BaseViewModel(application) {

    private val TAG = "FeedViewModel"

    val profile = MutableLiveData<Profile>()

    init {
        getProfileInfo()
    }

    private fun getProfileInfo() {
        repository.getProfileInfo(profile)
    }
}