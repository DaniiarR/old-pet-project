package com.york.exordi.feed.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.feed.datasource.CommentDataSourceFactory
import com.york.exordi.feed.datasource.PostDataSourceFactory
import com.york.exordi.models.*

class FeedViewModel(application: Application) : BaseViewModel(application) {

    private val TAG = "FeedViewModel"

    val profile = MutableLiveData<Profile>()
    val categories = MutableLiveData<List<CategoryData>>()

    val selectedCategory = MutableLiveData<Int?>(null)
    var selectedOrder: String = "popular"

    var results: LiveData<PagedList<Result>>? = null
    val isFollowersListEmpty = MutableLiveData<Boolean>(false)
    var dataSourceLiveData: LiveData<PageKeyedDataSource<String, Result>>? = null
    lateinit var dataSourceFactory: PostDataSourceFactory

    var commentDataSourceLiveData: LiveData<PageKeyedDataSource<String, CommentResult>>? = null
    var commentDataSourceFactory: CommentDataSourceFactory? = null
    var comments: LiveData<PagedList<CommentResult>>? = null
    val isCommentListEmpty = MutableLiveData<Boolean>(false)
    var commentsAmount = MutableLiveData<Int?>(null)

    val isUpvoteSuccessful = MutableLiveData<Boolean?>(null)
    val isCommentSuccessful = MutableLiveData<Boolean?>(null)
    val isDeleteCommentSuccessful = MutableLiveData<Boolean?>(null)

    init {
        getProfileInfo()
        getCategories()
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
        if (results == null) {
            dataSourceFactory =
                PostDataSourceFactory(
                    getApplication(),
                    selectedCategory.value!!,
                    selectedOrder
                )

            dataSourceLiveData = dataSourceFactory.postLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()
            results = LivePagedListBuilder(dataSourceFactory, config)
                .setBoundaryCallback(object : PagedList.BoundaryCallback<Result>() {
                override fun onZeroItemsLoaded() {
                    super.onZeroItemsLoaded()
                    isFollowersListEmpty.value = true
                }

                override fun onItemAtEndLoaded(itemAtEnd: Result) {
                    super.onItemAtEndLoaded(itemAtEnd)
                    isFollowersListEmpty.value = false
                }

                override fun onItemAtFrontLoaded(itemAtFront: Result) {
                    super.onItemAtFrontLoaded(itemAtFront)
                    isFollowersListEmpty.value = false
                }

            }).build()
        }
    }

    fun getNewComments(postId: String): LiveData<PagedList<CommentResult>>? {
        if (comments == null) {
            commentDataSourceFactory = CommentDataSourceFactory(getApplication(),postId)
            commentDataSourceLiveData = commentDataSourceFactory!!.commentLiveDataSource
            val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .build()
            comments = LivePagedListBuilder(commentDataSourceFactory!!, config)
                .setBoundaryCallback(object: PagedList.BoundaryCallback<CommentResult>() {
                    override fun onZeroItemsLoaded() {
                        super.onZeroItemsLoaded()
                        isCommentListEmpty.value = true
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: CommentResult) {
                        super.onItemAtEndLoaded(itemAtEnd)
                        isCommentListEmpty.value = false
                    }

                    override fun onItemAtFrontLoaded(itemAtFront: CommentResult) {
                        super.onItemAtFrontLoaded(itemAtFront)
                        isCommentListEmpty.value = false
                    }
                }).build()
        } else {
            commentDataSourceFactory?.postId = postId
            commentDataSourceLiveData?.value?.invalidate()
        }
        return comments
    }

    fun refreshResults() {
//        val dataSource = dataSourceLiveData?.value as PostDataSource
//        dataSource.categoryId = selectedCategory.value!!
        dataSourceFactory.categoryId = selectedCategory.value!!
        dataSourceFactory.order = selectedOrder
        dataSourceLiveData?.value?.invalidate()
//        dataSource.invalidate()
    }

    fun toggleUpvote(postId: String) {
        repository.toggleUpvote(PostId(postId)) {
            isUpvoteSuccessful.value = it
            isUpvoteSuccessful.value = null
        }
    }

    fun postComment(comment: String, postId: String): MutableLiveData<Boolean?> {
        repository.commentPost(CommentText(comment), postId) {
            isCommentSuccessful.value = it
            isCommentSuccessful.value = null
        }
        return isCommentSuccessful
    }

    fun deleteComment(comment: CommentResult) {
        repository.deleteComment(comment.id) {
            isDeleteCommentSuccessful.value = it
            isDeleteCommentSuccessful.value = null
        }
    }
}