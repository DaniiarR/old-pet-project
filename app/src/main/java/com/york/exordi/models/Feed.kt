package com.york.exordi.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.math.BigDecimal

class Profile(
    val data: ProfileData
) : Serializable

class ProfileData(
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
) : Serializable

class Post(
    val code: Int,
    val data: PostData
)

class PostData(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Result>
)

class Result(
    val id: String,
    val text: String?,
    val location: String?,
    @SerializedName("posted_on")
    val postedOn: String,
    @SerializedName("post_comments")
    val postComments: List<PostComment>,
    val files: List<PostFile>,
    @SerializedName("number_of_comments")
    val commentsAmount: Int,
    val category: PostCategory,
    @SerializedName("upvoted_by_req_user")
    var upvotedByUser: Boolean,
    val author: PostAuthor,
    var isCurrentUserPost: Boolean = false,
    @SerializedName("number_of_watches")
    val numberOfViews: BigDecimal
) : Serializable

class PostComment(

) : Serializable

class PostFile(
    val file: String,
    val type: String,
    val thumb: String?
) : Serializable

class PostCategory(
    val id: Int,
    val name: String
) : Serializable

class PostAuthor(
    val id: Int,
    val username: String,
    val photo: String?
) : Serializable

class PostId(
    @SerializedName("post_uuid")
    val postId: String
)

class Category(
    val code: Int,
    val data: List<CategoryData>
)

class CategoryData(
    val id: Int,
    val name: String,
    var isSelected: Boolean = false
) : Serializable

class Comment(
    val code: Int,
    val data: CommentData
)

class CommentData(
    val next: String?,
    val previous: String?,
    val results: List<CommentResult>
)

class CommentResult(
    val id: Int,
    val author: PostAuthor,
    val text: String,
    @SerializedName("posted_on")
    val postedOn: String
)

class OtherProfile(
    val code: Int,
    val data: OtherProfileData
)

class OtherProfileData(
    val id: Int,
    val username: String,
    val fullname: String?,
    val bio: String?,
    val photo: String?,
    @SerializedName("number_of_posts")
    val numberOfPosts: Int,
    val rating: Double,
    @SerializedName("followed_by_req_user")
    var followedByMe: Boolean,
    @SerializedName("number_of_following")
    val numberOfFollowings: Int
)

class Username(
    val username: String
)

class CommentText(
    val text: String
)

class Follower(
    val code: Int,
    val data: FollowerData
)

class FollowerData(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<FollowerResult>
)

class FollowerResult(
    val id: Int,
    val email: String?,
    val username: String,
    val rating: Double,
    val photo: String?,
    @SerializedName("followed_by_req_user")
    var isFollowedByUser: Boolean
)

class ReportPost(
    val post: String?,
    val description: String,
    @SerializedName("accused_user")
    val accusedUser: Int?
)