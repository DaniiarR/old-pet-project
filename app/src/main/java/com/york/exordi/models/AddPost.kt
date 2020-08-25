package com.york.exordi.models

import java.io.Serializable

class AddPostResponse(
    val code: Int
)

class AddPostCategory(
    val id: Int,
    val name: String,
    var isSelected: Boolean
) : Serializable