package com.york.exordi.events

import com.google.gson.annotations.SerializedName

class EditProfileEvent(
    val id: Int,
    val email: String?,
    val username: String,
    val birthday: String?,
    val bio: String?,
    @SerializedName("photo")
    val profilePic: String?,
    val rating: Double?,
    @SerializedName("rating_change")
    val ratingChange: Int,
    @SerializedName("number_of_posts")
    val numberOfPosts: Int,
    @SerializedName("number_of_followers")
    val numberOfFollowers: Int,
    @SerializedName("followers_change")
    val followersChange: Int,
    @SerializedName("upvotes_change")
    val upvotesChange: Int,
    @SerializedName("number_of_following")
    val numberOfFollowings: Int
)