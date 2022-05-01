package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchUsersDto(
    val data: List<UserDto>,
    val total: Int,
    @SerializedName("last_page")
    val lastPage: Int
)