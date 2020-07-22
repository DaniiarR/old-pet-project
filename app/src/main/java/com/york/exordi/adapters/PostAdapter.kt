package com.york.exordi.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.york.exordi.R
import com.york.exordi.models.Post

class PostAdapter(): BaseQuickAdapter<Post, BaseViewHolder>(R.layout.feed_list_item,
    arrayListOf(Post(), Post(), Post(), Post())) {

    override fun convert(holder: BaseViewHolder, item: Post) {

    }
}