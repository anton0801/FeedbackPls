package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthBodyDto(
    val phone: String,
    @SerializedName("pin_code")
    val pinCode: String,
    @SerializedName("is_user_recognized_account")
    val isUserRecognizedAccount: Int = 1
)