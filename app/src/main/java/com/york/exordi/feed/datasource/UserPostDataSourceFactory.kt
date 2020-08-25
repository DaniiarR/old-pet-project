package com.york.exordi.feed.datasource

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.Result

class UserPostDataSourceFactory(val application: Application, var authorId: Int) : DataSource.Factory<String, Result>() {

    val postLiveDataSource = MutableLiveData<PageKeyedDataSource<String, Result>>()

    override fun create(): DataSource<String, Result> {
        val postDataSource = UserPostDataSource(
            application,
            authorId
        )
        postLiveDataSource.postValue(postDataSource)
        return postDataSource
    }


}