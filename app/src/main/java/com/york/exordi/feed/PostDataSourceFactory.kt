package com.york.exordi.feed

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Result

class PostDataSourceFactory(val application: Application) : DataSource.Factory<String, Result>() {

    val postLiveDataSource = MutableLiveData<PageKeyedDataSource<String, Result>>()

    override fun create(): DataSource<String, Result> {
        val postDataSource = PostDataSource(application)
        postLiveDataSource.postValue(postDataSource)
        return postDataSource
    }


}