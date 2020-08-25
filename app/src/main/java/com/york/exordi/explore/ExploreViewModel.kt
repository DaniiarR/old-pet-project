package com.york.exordi.explore

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.models.SearchUserResult

class ExploreViewModel(application: Application) : BaseViewModel(application) {

    var searchUserDataSourceLiveData: LiveData<PageKeyedDataSource<String, SearchUserResult>>? = null
    var searchUserDataSourceFactory: SearchUserDataSourceFactory? = null
    var users: LiveData<PagedList<SearchUserResult>>? = null
    val isUserListEmpty = MutableLiveData<Boolean>(false)


    fun searchUser(username: String): LiveData<PagedList<SearchUserResult>>? {
        if (users == null) {
            searchUserDataSourceFactory = SearchUserDataSourceFactory(getApplication(), username)
            searchUserDataSourceLiveData = searchUserDataSourceFactory!!.userLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()
            users = LivePagedListBuilder(searchUserDataSourceFactory!!, config)
                .setBoundaryCallback(object : PagedList.BoundaryCallback<SearchUserResult>() {
                    override fun onZeroItemsLoaded() {
                        super.onZeroItemsLoaded()
                        isUserListEmpty.value = true
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: SearchUserResult) {
                        super.onItemAtEndLoaded(itemAtEnd)
                        isUserListEmpty.value = false
                    }

                    override fun onItemAtFrontLoaded(itemAtFront: SearchUserResult) {
                        super.onItemAtFrontLoaded(itemAtFront)
                        isUserListEmpty.value = false
                    }
                }).build()
        } else {
            searchUserDataSourceFactory?.username = username
            searchUserDataSourceLiveData?.value?.invalidate()
        }
        return users
    }
}