package com.york.exordi.feed

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.models.Post
import com.york.exordi.models.Profile
import com.york.exordi.models.Result

class FeedViewModel(application: Application) : BaseViewModel(application) {

    private val TAG = "FeedViewModel"

    val profile = MutableLiveData<Profile>()
    var results: LiveData<PagedList<Result>>? = null
    var dataSourceLiveData: LiveData<PageKeyedDataSource<String, Result>>? = null

    init {
        getProfileInfo()
    }

    private fun getProfileInfo() {
        repository.getProfileInfo(profile)
    }

    fun getNewResults() {
        val dataSourceFactory = PostDataSourceFactory(getApplication())
        dataSourceLiveData = dataSourceFactory.postLiveDataSource
        val config = PagedList.Config.Builder()
            .setInitialLoadSizeHint(1)
            .setPageSize(1)
            .build()

        results = LivePagedListBuilder(dataSourceFactory, config).build()
    }
}