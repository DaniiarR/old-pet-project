package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.york.exordi.R
import com.york.exordi.models.Post
import com.york.exordi.models.Result
import com.york.exordi.shared.OnItemClickListener
import kotlinx.android.synthetic.main.feed_list_item.view.*

class PostAdapter(private val clickListener: OnItemClickListener) : PagedListAdapter<Result, PostAdapter.PostViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Result>() {
            override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem.files[0].file == newItem.files[0].file
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feed_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(post: Result?, clickListener: OnItemClickListener) {
            itemView.apply {
                post?.let {
                    it.author.photo?.let { Glide.with(context).load(it).into(itemView.feedProfilePictureIv) }
                    feedUsernameTv.text = it.author.username
                    if (it.files[0].type == "image") {
                        Glide.with(context).load(it.files[0].file).into(feedPhotoIv)
                    }
                    feedCommentsTv.text = "${it.commentsAmount} comments"
                    feedDescriptionTv.text = it.text ?: ""
                }
            }

        }
    }
}