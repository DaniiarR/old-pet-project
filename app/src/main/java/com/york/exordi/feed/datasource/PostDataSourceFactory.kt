package com.york.exordi.feed.datasource

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.feed.datasource.PostDataSource
import com.york.exordi.models.Result

class PostDataSourceFactory(val application: Application, var categoryId: Int, var order: String) : DataSource.Factory<String, Result>() {

    val postLiveDataSource = MutableLiveData<PageKeyedDataSource<String, Result>>()

    override fun create(): DataSource<String, Result> {
        val postDataSource = PostDataSource(
            application,
            categoryId,
            order
        )
        postLiveDataSource.postValue(postDataSource)
        return postDataSource
    }


}