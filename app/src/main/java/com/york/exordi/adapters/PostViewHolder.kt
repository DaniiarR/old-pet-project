package com.york.exordi.adapters

import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.york.exordi.R
import com.york.exordi.models.Result
import com.york.exordi.shared.Const

import com.york.exordi.shared.OnItemClickListener
import com.york.exordi.shared.OnPostClickListener
import kotlinx.android.synthetic.main.feed_list_item.view.*

class PostViewHolder : RecyclerView.ViewHolder {

    lateinit var parent: View
    lateinit var frameLayout: FrameLayout
    lateinit var profilePicture: ImageView
    lateinit var username: TextView
    lateinit var publicationDate: TextView
    lateinit var postImageView: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var upvoteButton: ImageButton
    lateinit var commentsButton: ImageButton
    lateinit var commentsAmount: TextView
    lateinit var description: TextView
    lateinit var requestManager: RequestManager
    lateinit var photoProgressBar: CircularProgressDrawable

    private var clickListener: OnPostClickListener?

    constructor(itemView: View, clickListener: OnPostClickListener) : super(itemView) {
        parent = itemView
        frameLayout = itemView.findViewById(R.id.feedListItemFrameLayout)
        profilePicture = itemView.findViewById(R.id.feedProfilePictureIv)
        profilePicture.tag = Const.TAG_PROFILE
        username = itemView.findViewById(R.id.feedUsernameTv)
        username.tag = Const.TAG_PROFILE
        publicationDate = itemView.findViewById(R.id.feedPublicationDateTv)
        postImageView = itemView.findViewById(R.id.feedPhotoIv)
        progressBar = itemView.findViewById(R.id.feedListItemPb)
        upvoteButton = itemView.findViewById(R.id.feedUpvoteBtn)
        upvoteButton.tag = Const.TAG_UPVOTE
        commentsButton = itemView.findViewById(R.id.feedCommentsBtn)
        commentsButton.tag = Const.TAG_COMMENTS
        commentsAmount = itemView.findViewById(R.id.feedCommentsTv)
        description = itemView.findViewById(R.id.feedDescriptionTv)
        photoProgressBar = CircularProgressDrawable(itemView.context).apply {
            strokeWidth = 5F
            centerRadius = 70F
            setColorSchemeColors(ContextCompat.getColor(itemView.context, R.color.textColorPrimary))
            start()
        }
        this.clickListener = clickListener
    }
        fun bind(position: Int, post: Result?, requestManager: RequestManager) {
            parent.tag = this
            this.requestManager = requestManager

            post?.let {
                if (it.author.photo != null) {
                    Glide.with(itemView.context).load(it.author.photo).into(profilePicture)
                } else {
                    profilePicture.setImageResource(R.drawable.ic_profile)
                }
                profilePicture.setOnClickListener { clickListener?.onItemClick(position, post, it.tag.toString(), null) }
                val upvoteDrawable = DrawableCompat.wrap(upvoteButton.drawable)
                profilePicture.setOnClickListener { clickListener?.onItemClick(position, post, it.tag.toString(), null) }
                username.setOnClickListener { clickListener?.onItemClick(position, post, it.tag.toString(), null) }
                if (!it.upvotedByUser) {
//                    DrawableCompat.setTint(
//                        upvoteDrawable,
//                        ContextCompat.getColor(itemView.context, R.color.textColorPrimary)
//                    )
//                    upvoteButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.textColorPrimary))
                    upvoteButton.setImageResource(R.drawable.ic_upvote)
                } else {
//                    upvoteDrawable.setTintList(null)
                    upvoteButton.setImageResource(R.drawable.ic_upvote_filled)

                }
                upvoteButton.setOnClickListener { clickListener?.onItemClick(position, post, it.tag.toString(), it) }
                username.text = it.author.username
                if (it.files[0].type == "image") {
                    Glide.with(itemView.context).load(it.files[0].file).placeholder(photoProgressBar).into(postImageView)
                }
                commentsAmount.text = "${it.commentsAmount} comments"
                description.text = it.text ?: ""
                commentsButton.setOnClickListener { clickListener?.onItemClick(position, post, it.tag.toString(), null) }
            }
        }
}
