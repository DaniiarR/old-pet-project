package com.york.exordi.feed

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.york.exordi.base.BaseViewModel
import com.york.exordi.models.Profile

class ProfileViewModel(application: Application) : BaseViewModel(application) {

    var profile = MutableLiveData<Profile>()

    fun getNewProfile() {
        repository.getProfileInfo(profile)
    }

}