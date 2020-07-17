package com.york.exordi.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Profile(
    val email: String? = null,
    val username: String? = null,
    val birthday: String? = null,
    val bio: String? = null,
    @SerializedName("profile_pic")
    val profilePic: String? = null
) : Serializable