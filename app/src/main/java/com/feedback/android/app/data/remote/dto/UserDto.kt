package com.feedback.android.app.data.remote.dto

import com.feedback.android.app.domain.model.UserModel
import com.google.gson.annotations.SerializedName

data class UserDto(
    val about: String?,
    val age: Int?,
    val avatar: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    val email: String?,
    val birthday: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,
    val id: Int,
    @SerializedName("is_account_payed")
    val isAccountPayed: Int?,
    val name: String?,
    @SerializedName("payed_to_date")
    val payedToDate: String?,
    val phone: String,
    val profession: String?,
    val rating: String?,
    @SerializedName("tariff_id")
    val tariffId: Int?,
    val updated_at: String?,
    @SerializedName("whatsapp_available")
    val whatsappAvailable: Int?,
    @SerializedName("tariff_name")
    val tariffName: String?,
    @SerializedName("tariff_desc")
    val tariffDesc: String?,
    @SerializedName("tariff_price")
    val tariffPrice: String?,
    @SerializedName("pin_code")
    val pinCode: String?,
    @SerializedName("is_account_published")
    val isAccountPublished: Int,
    @SerializedName("user_type")
    val userType: String,
    @SerializedName("is_account_archived")
    val isAccountArchived: Int,
    val website: String,
)

fun UserDto.toUserModel() = UserModel(
    id = this.id,
    email = this.email,
    phone = this.phone,
    avatar = this.avatar,
    rating = this.rating,
    whatsappAvailable = this.whatsappAvailable,
    payedToDate = this.payedToDate,
    tariffId = this.tariffId,
    profession = this.profession,
    name = this.name,
    isAccountPayed = this.isAccountPayed,
    tariffName = this.tariffName,
    tariffPrice = this.tariffPrice,
    about = this.about,
    birthday = this.birthday,
    pinCode = this.pinCode,
    tariffDescription = this.tariffDesc,
    isPublished = this.isAccountPublished,
    userType = this.userType,
    createdUt = this.createdAt.toString(),
    website = this.website,
    isAccountArchived = this.isAccountArchived == 1
)