package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import com.york.exordi.R
import com.york.exordi.models.Result
import com.york.exordi.shared.OnPostClickListener
import com.york.exordi.shared.OnViewDetachedFromWindowListener
import kotlinx.android.synthetic.main.feed_list_item.view.*

class PostAdapter(val requestManager: RequestManager, val lifecycleOwner: LifecycleOwner) : PagedListAdapter<Result, PostViewHolder>(diffCallback) {

    var clickListener: OnPostClickListener? = null
    var onViewDetachedListener: OnViewDetachedFromWindowListener? = null

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


    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
        onViewDetachedListener?.onViewDetached()
        holder.itemView.feedCommentsRv.adapter = null
        holder.itemView.feedBlankSpace.visibility = View.GONE
        holder.itemView.feedCommentsRv.visibility = View.GONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feed_list_item, parent, false), clickListener!!, lifecycleOwner)
    }

//    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
//        var adapter = holder.commentsRv.adapter as PagedListAdapter<CommentResult, CommentAdapter.CommentViewHolder>
//        adapter.submitList(null)
//    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        holder.bind(position, getItem(position), requestManager)
        holder.bind(position, getItem(holder.bindingAdapterPosition), requestManager)

    }

}