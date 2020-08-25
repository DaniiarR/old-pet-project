package com.york.exordi.feed.datasource

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.FollowerResult

class FollowersDataSourceFactory(val context: Context, var mode: String, var username: String?) :
    DataSource.Factory<String, FollowerResult>() {

    val followersLiveDataSource = MutableLiveData<PageKeyedDataSource<String, FollowerResult>>()


    override fun create(): DataSource<String, FollowerResult> {
        val dataSource = FollowersDataSource(context, mode, username)
        followersLiveDataSource.postValue(dataSource)
        return dataSource
    }


}