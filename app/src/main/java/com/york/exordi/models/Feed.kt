package com.york.exordi.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Profile(
    val data: ProfileData
) : Serializable

class ProfileData(
    val email: String? = null,
    val username: String? = null,
    val birthday: String? = null,
    val bio: String? = null,
    @SerializedName("photo")
    val profilePic: String? = null,
    val token: String? = null
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
    val author: PostAuthor
)

class PostComment(

)

class PostFile(
    val file: String,
    val type: String
)

class PostCategory(
    val id: Int,
    val name: String
)

class PostAuthor(
    val id: Int,
    val username: String?,
    val photo: String?
)

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
)