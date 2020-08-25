package com.york.exordi.models

class SearchUser(
    val code: Int,
    val data: SearchUserData
)

class SearchUserData(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<SearchUserResult>
)

class SearchUserResult(
    val id: Int,
    val username: String,
    val photo: String?,
    val rating: Double?
)