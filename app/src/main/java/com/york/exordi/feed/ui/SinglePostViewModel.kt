package com.york.exordi.feed.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.york.exordi.base.BaseViewModel
import com.york.exordi.feed.datasource.CommentDataSourceFactory
import com.york.exordi.models.*

class SinglePostViewModel(application: Application) : BaseViewModel(application) {

    lateinit var post: Result

    var isVideoViewAdded: Boolean = false

    val isUpvoteSuccessful = MutableLiveData<Boolean?>(null)
    val isCommentSuccessful = MutableLiveData<Boolean?>(null)
    val isDeleteCommentSuccessful = MutableLiveData<Boolean?>(null)
    val isDeletePostSuccessful = MutableLiveData<Boolean?>(null)


    var commentDataSourceLiveData: LiveData<PageKeyedDataSource<String, CommentResult>>? = null
    var commentDataSourceFactory: CommentDataSourceFactory? = null
    var comments: LiveData<PagedList<CommentResult>>? = null

    fun toggleUpvote(postId: String) {
        repository.toggleUpvote(PostId(postId)) {
            isUpvoteSuccessful.value = it
            isUpvoteSuccessful.value = null
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
            comments = LivePagedListBuilder(commentDataSourceFactory!!, config).build()
        } else {
            commentDataSourceFactory?.postId = postId
            commentDataSourceLiveData?.value?.invalidate()
        }
        return comments
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

    fun deletePost(postId: String) {
        repository.deletePost(postId) {
            isDeletePostSuccessful.value = it
            isDeletePostSuccessful.value = null
        }
    }
}