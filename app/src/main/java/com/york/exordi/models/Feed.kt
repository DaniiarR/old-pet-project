package com.york.exordi.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Profile(
    val email: String? = null,
    val username: String? = null,
    val birthday: String? = null,
    val bio: String? = null,
    @SerializedName("profile_pic")
    val profilePic: String? = null,
    val token: String? = null
) : Serializable

class EditProfile(
    val username: String,
    val bio: String
)

class EditProfileDescription(
    val bio: String
)

class Post()

