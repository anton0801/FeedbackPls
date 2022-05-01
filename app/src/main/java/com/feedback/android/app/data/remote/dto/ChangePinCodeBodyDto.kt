package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.lang.Exception

data class ChangePinCodeBodyDto(
    val phone: String,
    @SerializedName("pin_code")
    val pinCode: String
)

class ChangePinCodeException(m: String): Exception(m)