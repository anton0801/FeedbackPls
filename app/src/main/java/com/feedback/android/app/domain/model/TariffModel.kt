package com.feedback.android.app.domain.model

import com.google.gson.annotations.SerializedName

data class TariffModel(
    val id: Int,
    val paymentType: Int,
    val tariffDesc: String,
    val tariffName: String,
    val tariffPrice: Int
)