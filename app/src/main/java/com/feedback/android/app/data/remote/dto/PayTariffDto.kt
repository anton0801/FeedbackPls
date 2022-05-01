package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PayTariffDto(
    @SerializedName("confirmation_url")
    val confirmationUrl: String
)