package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.models.FollowerResult
import com.york.exordi.shared.Const
import com.york.exordi.shared.OnItemWithTagClickListener
import com.york.exordi.shared.getCircularProgressDrawable
import kotlinx.android.synthetic.main.follower_list_item.view.*

class FollowerAdapter() : PagedListAdapter<FollowerResult, FollowerAdapter.FollowerViewHolder>(
    diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<FollowerResult>() {
            override fun areItemsTheSame(
                oldItem: FollowerResult,
                newItem: FollowerResult
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: FollowerResult,
                newItem: FollowerResult
            ): Boolean {
                return oldItem.username == newItem.username
            }

        }
    }
    var clickListener: OnItemWithTagClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        return FollowerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.follower_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, position)
    }

    inner class FollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(follower: FollowerResult?, listener: OnItemWithTagClickListener?, position: Int) {
            follower?.let {
                Glide.with(itemView.context).load(follower.photo).placeholder(itemView.context.getCircularProgressDrawable()).into(itemView.followerProfilePicIv)
                itemView.followerUsernameTv.text = follower.username
                itemView.followerRatingTv.text = follower.rating.toString()
                if (follower.isFollowedByUser) {
                    itemView.otherProfileFollowBtn.visibility = View.INVISIBLE
                    itemView.otherProfileUnfollowBtn.apply {
                        visibility = View.VISIBLE
                        tag = Const.TAG_UNFOLLOW
                        setOnClickListener { listener?.onItemClick(follower, tag.toString(), position) }
                    }
                } else {
                    itemView.otherProfileUnfollowBtn.visibility = View.INVISIBLE
                    itemView.otherProfileFollowBtn.apply {
                        visibility = View.VISIBLE
                        tag = Const.TAG_FOLLOW
                        setOnClickListener { listener?.onItemClick(follower, tag.toString(), position) }
                    }
                }
                itemView.followerWholeView.tag = Const.TAG_WHOLE_VIEW
                itemView.followerWholeView.setOnClickListener { clickListener?.onItemClick(follower, Const.TAG_WHOLE_VIEW, position)}

            }
        }
    }
}