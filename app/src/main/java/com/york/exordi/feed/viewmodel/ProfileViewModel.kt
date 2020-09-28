package com.york.exordi.feed.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.feed.datasource.UserPostDataSourceFactory
import com.york.exordi.models.Profile
import com.york.exordi.models.Result

class ProfileViewModel(application: Application) : BaseViewModel(application) {

    var profile = MutableLiveData<Profile>()

    var results: LiveData<PagedList<Result>>? = null
    val isResultListEmpty = MutableLiveData<Boolean>(false)
    var dataSourceLiveData: LiveData<PageKeyedDataSource<String, Result>>? = null
    lateinit var dataSourceFactory: UserPostDataSourceFactory


    fun getNewProfile(): MutableLiveData<Profile> {
        repository.getProfileInfo(profile)
        return profile
    }

    fun getPosts(): LiveData<PagedList<Result>>? {
        profile.value?.let {
            dataSourceFactory = UserPostDataSourceFactory(getApplication(), it.data.id)
            dataSourceLiveData = dataSourceFactory.postLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()
            results = LivePagedListBuilder(dataSourceFactory, config)
                .setBoundaryCallback(object: PagedList.BoundaryCallback<Result>() {
                    override fun onZeroItemsLoaded() {
                        super.onZeroItemsLoaded()
                        isResultListEmpty.value = true
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: Result) {
                        super.onItemAtEndLoaded(itemAtEnd)
                        isResultListEmpty.value = false
                    }

                    override fun onItemAtFrontLoaded(itemAtFront: Result) {
                        super.onItemAtFrontLoaded(itemAtFront)
                        isResultListEmpty.value = false
                    }
                }).build()
        }
        return results
    }

    fun upvotePostLocally(postId: String) {
        results?.value?.forEach {
            if (it.id == postId) {
                it.upvotedByUser = !it.upvotedByUser
            }
        }
    }

}