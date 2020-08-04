package com.york.exordi.models

import com.google.android.gms.auth.api.Auth
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
    @SerializedName("code")
    val activationCode: Int
)

class LoginToken(
    val data: AuthToken
)

class AuthToken(
    val access: String,
    val refresh: String
)

class Login(
    val username: String,
    val password: String
)

class LoginCredentials(
    val provider: String,
    @SerializedName("access_token")
    val token: String
)

class LoginResponse(
    val status: String,
    val code: String,
    val data: LoginData
)

class LoginData(
    val email: String?,
    val username: String?,
    @SerializedName("access")
    val accessToken: String?,
    @SerializedName("refresh")
    val refreshToken: String?
)