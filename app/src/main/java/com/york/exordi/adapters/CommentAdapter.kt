package com.york.exordi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.york.exordi.R
import com.york.exordi.models.CommentResult
import com.york.exordi.models.SearchUserResult
import com.york.exordi.shared.Const
import com.york.exordi.shared.OnCommentClickListener
import com.york.exordi.shared.toHoursAgo
import kotlinx.android.synthetic.main.comment_list_item.view.*

class CommentAdapter(
    private val isCurrentUserPost: Boolean,
    private val username: String,
    private val clickListener: OnCommentClickListener
) : PagedListAdapter<CommentResult, CommentAdapter.CommentViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<CommentResult>() {
            override fun areItemsTheSame(oldItem: CommentResult, newItem: CommentResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CommentResult,
                newItem: CommentResult
            ): Boolean {
                return oldItem.text == newItem.text && oldItem.postedOn == newItem.postedOn
            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(comment: CommentResult?, clickListener: OnCommentClickListener) {
            comment?.let {
                if (it.author.photo != null) {
                    Glide.with(itemView.context).load(it.author.photo).into(itemView.commentsProfilePicIv)
                } else {
                    itemView.commentsProfilePicIv.setImageResource(R.drawable.ic_profile)
                }
                itemView.apply {
                    commentsProfilePicIv.tag = Const.TAG_PROFILE
                    commentsProfilePicIv.setOnClickListener { v -> clickListener.onItemClick(it, commentsProfilePicIv.tag.toString()) }
                    commentsUsernameTv.text = it.author.username
                    commentsUsernameTv.tag = Const.TAG_PROFILE
                    commentsUsernameTv.setOnClickListener { v -> clickListener.onItemClick(it, commentsUsernameTv.tag.toString()) }
                    commentsPublicationDateTv.text = it.postedOn.toHoursAgo()
                    commentsCommentTv.text = it.text
                }
                setupDetailButton(comment)

            }
        }

        fun setupDetailButton(comment: CommentResult) {
            if (isCurrentUserPost || comment.author.username == username) {
                itemView.commentsDetailsBtn.visibility = View.VISIBLE
                itemView.commentsDetailsBtn.tag = Const.TAG_COMMENT_DETAILS
                itemView.commentsDetailsBtn.setOnClickListener { clickListener.onItemClick(comment, itemView.commentsDetailsBtn.tag.toString()) }
            } else {

            }
        }

    }
}