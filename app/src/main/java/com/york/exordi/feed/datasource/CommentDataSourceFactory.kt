package com.york.exordi.feed.datasource

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.york.exordi.models.CommentResult
import com.york.exordi.models.SearchUserResult

class CommentDataSourceFactory(val context: Context, var postId: String) :
    DataSource.Factory<String, CommentResult>() {

    val commentLiveDataSource = MutableLiveData<PageKeyedDataSource<String, CommentResult>>()

    override fun create(): DataSource<String, CommentResult> {
        val commentDataSource = CommentDataSource(context, postId)
        commentLiveDataSource.postValue(commentDataSource)
        return commentDataSource
    }

}