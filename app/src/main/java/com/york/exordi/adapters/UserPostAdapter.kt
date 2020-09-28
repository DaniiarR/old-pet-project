package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.models.Result
import com.york.exordi.shared.OnItemClickListener
import com.york.exordi.shared.getCircularProgressDrawable
import kotlinx.android.synthetic.main.profile_post_list_item.view.*

class UserPostAdapter(val clickListener: OnItemClickListener) :
    PagedListAdapter<Result, UserPostAdapter.PostViewHolder>(diffCallback) {

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
        return PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.profile_post_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(result: Result?, clickListener: OnItemClickListener) {
            result?.let {
                when (it.files[0].type) {
                    "video" -> {
                        itemView.profilePlayIv.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(it.files[0].thumb)
                            .placeholder(itemView.context.getCircularProgressDrawable())
                            .into(itemView.profilePostIv)
                    }
                    "image" -> {
                        itemView.profilePlayIv.visibility = View.GONE
                        Glide.with(itemView.context)
                            .load(it.files[0].file)
                            .placeholder(itemView.context.getCircularProgressDrawable())
                            .into(itemView.profilePostIv)
                    }
                    else -> {}
                }
                itemView.setOnClickListener { clickListener.onItemClick(result) }

            }
        }
    }
}