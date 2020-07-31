package com.york.exordi.models

import com.google.gson.annotations.SerializedName

open class BaseResponse(
    val status: String,
    val code: Int,
    @SerializedName("code_text")
    val codeText: String,
    val message: String
)