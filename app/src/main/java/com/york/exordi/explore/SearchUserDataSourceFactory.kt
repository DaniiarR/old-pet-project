package com.york.exordi.explore

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.SearchUserResult

class SearchUserDataSourceFactory(val context: Context, var username: String) :
    DataSource.Factory<String, SearchUserResult>() {

    val userLiveDataSource = MutableLiveData<PageKeyedDataSource<String, SearchUserResult>>()

    override fun create(): DataSource<String, SearchUserResult> {
        val userDataSource = SearchUserDataSource(context, username)
        userLiveDataSource.postValue(userDataSource)
        return userDataSource
    }

}