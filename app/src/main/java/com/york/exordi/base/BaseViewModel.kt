package com.york.exordi.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.york.exordi.repository.AppRepository

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val repository = AppRepository.getInstance(application)
}