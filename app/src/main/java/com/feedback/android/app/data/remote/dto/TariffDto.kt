package com.feedback.android.app.data.remote.dto

import com.feedback.android.app.domain.model.TariffModel
import com.google.gson.annotations.SerializedName

data class TariffDto(
    val id: Int,
    @SerializedName("payment_type")
    val paymentType: Int,
    @SerializedName("tariff_desc")
    val tariffDesc: String,
    @SerializedName("tariff_name")
    val tariffName: String,
    @SerializedName("tariff_price")
    val tariffPrice: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

fun TariffDto.toTariffModel() = TariffModel(
    id = this.id,
    paymentType = this.paymentType,
    tariffDesc = this.tariffDesc,
    tariffName = this.tariffName,
    tariffPrice = this.tariffPrice
)