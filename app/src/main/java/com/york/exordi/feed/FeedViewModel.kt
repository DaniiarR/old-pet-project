package com.york.exordi.feed

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.models.*

class FeedViewModel(application: Application) : BaseViewModel(application) {

    private val TAG = "FeedViewModel"

    val profile = MutableLiveData<Profile>()
    val categories = MutableLiveData<List<CategoryData>>()
    val selectedCategory = MutableLiveData(1)
    var results: LiveData<PagedList<Result>>? = null
    var dataSourceLiveData: LiveData<PageKeyedDataSource<String, Result>>? = null
    lateinit var dataSourceFactory: PostDataSourceFactory

    val isUpvoteSuccessful = MutableLiveData<Boolean?>(null)

    init {
        getProfileInfo()
        getCategories()
        getNewResults()
    }

    private fun getProfileInfo() {
        repository.getProfileInfo(profile)
    }

    private fun getCategories() {
        repository.getCategories {
            // make the first item in the list selected
            it[0].isSelected = true
            categories.value = it
            selectedCategory.value = it[0].id
        }
    }

    fun getNewResults() {
        dataSourceFactory = PostDataSourceFactory(getApplication(), selectedCategory.value!!)
        dataSourceLiveData = dataSourceFactory.postLiveDataSource
        val config = PagedList.Config.Builder()
            .setInitialLoadSizeHint(1)
            .setPageSize(1)
            .build()
        results = LivePagedListBuilder(dataSourceFactory, config).build()
    }

    fun refreshResults() {
//        val dataSource = dataSourceLiveData?.value as PostDataSource
//        dataSource.categoryId = selectedCategory.value!!
        dataSourceFactory.categoryId = selectedCategory.value!!
        dataSourceLiveData?.value?.invalidate()
//        dataSource.invalidate()
    }

    fun toggleUpvote(postId: String) {
        repository.toggleUpvote(PostId(postId)) {
            isUpvoteSuccessful.value = it
            isUpvoteSuccessful.value = null
        }
    }
}