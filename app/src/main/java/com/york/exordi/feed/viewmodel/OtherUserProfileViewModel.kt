package com.york.exordi.feed.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.feed.datasource.PostDataSourceFactory
import com.york.exordi.feed.datasource.UserPostDataSourceFactory
import com.york.exordi.models.*

class OtherUserProfileViewModel(application: Application) : BaseViewModel(application) {

    var username: String? = null

    var profile = MutableLiveData<OtherProfileData>()

    var results: LiveData<PagedList<Result>>? = null
    var dataSourceLiveData: LiveData<PageKeyedDataSource<String, Result>>? = null
    lateinit var dataSourceFactory: UserPostDataSourceFactory

    var followUserSuccessful = MutableLiveData<Boolean?>(null)

    fun getNewProfile() {
        username?.let {
            repository.getOtherUserProfile(it, profile)
        }
    }

    fun getPosts(): LiveData<PagedList<Result>>? {
        profile.value?.id?.let {
            dataSourceFactory = UserPostDataSourceFactory(getApplication(), it)
            dataSourceLiveData = dataSourceFactory.postLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()
            results = LivePagedListBuilder(dataSourceFactory, config).build()
        }
        return results
    }

    fun followUser(): MutableLiveData<Boolean?> {

        profile.value?.username?.let {
            repository.followUser(Username(it)) {success ->
                followUserSuccessful.value = success
                followUserSuccessful.value = null
                if (success) {
                    profile.value!!.followedByMe = !profile.value!!.followedByMe
                }
            }
        }
        return followUserSuccessful
    }

    fun upvotePostLocally(postId: String) {
        results?.value?.forEach {
            if (it.id == postId) {
                it.upvotedByUser = !it.upvotedByUser
            }
        }
    }
}