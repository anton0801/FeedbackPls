package com.feedback.android.app.domain.model

data class UserModel(
    val id: Int,
    val name: String?,
    val about: String?,
    val profession: String?,
    val avatar: String?,
    val email: String?,
    val birthday: String?,
    val phone: String?,
    val isAccountPayed: Int?,
    val rating: String?,
    val tariffId: Int?,
    val whatsappAvailable: Int?,
    val payedToDate: String?,
    val tariffName: String?,
    val tariffDescription: String?,
    val tariffPrice: String?,
    val pinCode: String?,
    val isPublished: Int,
    val userType: String,
    val createdUt: String,
    val website: String?,
    val isAccountArchived: Boolean
)