package com.york.exordi.events

import com.google.gson.annotations.SerializedName

class EditProfileEvent(
    val email: String? = null,
    val username: String? = null,
    val birthday: String? = null,
    val bio: String? = null,
    @SerializedName("photo")
    val profilePic: String? = null
)