package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.models.SearchUserResult
import com.york.exordi.shared.OnItemClickListener
import kotlinx.android.synthetic.main.explore_user_list_item.view.*

class SearchUserAdapter(val clickListener: OnItemClickListener):
    PagedListAdapter<SearchUserResult, SearchUserAdapter.SearchUserViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<SearchUserResult>() {
            override fun areItemsTheSame(
                oldItem: SearchUserResult,
                newItem: SearchUserResult
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SearchUserResult,
                newItem: SearchUserResult
            ): Boolean {
                return oldItem.username == newItem.username
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        return SearchUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explore_user_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    inner class SearchUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(user: SearchUserResult?, clickListener: OnItemClickListener) {
            user?.let {
                if (it.photo != null) {
                    Glide.with(itemView.context).load(it.photo).into(itemView.exploreUserIv)
                } else {

                }
                itemView.exploreUsernameTv.text = it.username
                itemView.exploreUserRatingTv.text = it.rating.toString()
                itemView.exploreUserWholeView.setOnClickListener { v -> clickListener.onItemClick(it)}
            }
        }

    }
}