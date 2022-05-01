package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.lang.Exception

data class SendPinCodeDto(
    val phone: String
)

class SendPinCodeException(m: String): Exception(m)
