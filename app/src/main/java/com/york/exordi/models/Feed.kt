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

class Post()
