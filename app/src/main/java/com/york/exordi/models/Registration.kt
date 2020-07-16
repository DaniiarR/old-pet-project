package com.york.exordi.models

import com.google.gson.annotations.SerializedName

class UserRegistration(
    val username: String,
    val email: String,
    val password: String,
    val location: String,
    val birthday: String
)

class EmailCheck(
    val email: String
)

class UsernameCheck(
    val username: String
)

class ActivationCode(
    @SerializedName("activation_code")
    val activationCode: Int
)

class AuthToken(
    val token: String
)