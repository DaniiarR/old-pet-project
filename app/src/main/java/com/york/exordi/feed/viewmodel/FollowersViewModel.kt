package com.york.exordi.feed.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.feed.datasource.CommentDataSourceFactory
import com.york.exordi.feed.datasource.FollowersDataSourceFactory
import com.york.exordi.models.*
import com.york.exordi.shared.Const

class FollowersViewModel(application: Application) : BaseViewModel(application) {

    var activityMode: String? = null
    var otherUserUsername: String? = null

    var followersDataSourceLiveData: LiveData<PageKeyedDataSource<String, FollowerResult>>? = null
    var followersDataSourceFactory: FollowersDataSourceFactory? = null
    var followers: LiveData<PagedList<FollowerResult>>? = null

    val isFollowersListEmpty = MutableLiveData<Boolean>(false)

    val isFollowUserSuccessful = MutableLiveData<Boolean?>(null)
    val isUnFollowUserSuccessful = MutableLiveData<Boolean?>(null)

    fun getNewFollowers(): LiveData<PagedList<FollowerResult>>? {
        val path = if (activityMode == Const.EXTRA_MODE_FOLLOWERS) "followers" else "following"
        if (followers == null) {
            followersDataSourceFactory = FollowersDataSourceFactory(getApplication(), path, otherUserUsername)
            followersDataSourceLiveData = followersDataSourceFactory!!.followersLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(2)
                .setPageSize(1)
                .build()
            followers = LivePagedListBuilder(followersDataSourceFactory!!, config)
                .setBoundaryCallback(object : PagedList.BoundaryCallback<FollowerResult>() {
                    override fun onZeroItemsLoaded() {
                        super.onZeroItemsLoaded()
                        isFollowersListEmpty.value = true
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: FollowerResult) {
                        super.onItemAtEndLoaded(itemAtEnd)
                        isFollowersListEmpty.value = false
                    }

                    override fun onItemAtFrontLoaded(itemAtFront: FollowerResult) {
                        super.onItemAtFrontLoaded(itemAtFront)
                        isFollowersListEmpty.value = false
                    }
                }).build()
        } else {
            followersDataSourceFactory?.mode = path
            followersDataSourceLiveData?.value?.invalidate()
        }
        return followers
    }

    fun followOrUnfollowUser(username: String): MutableLiveData<Boolean?> {
        val isSuccessful = MutableLiveData<Boolean?>(null)
        repository.followUser(Username(username)) {success ->
            isSuccessful.value = success
        }
        return isSuccessful
    }

}