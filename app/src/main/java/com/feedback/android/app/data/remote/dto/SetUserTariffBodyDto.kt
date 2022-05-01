package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SetUserTariffBodyDto(
    @SerializedName("tariff_name")
    val tariffName: String
)
