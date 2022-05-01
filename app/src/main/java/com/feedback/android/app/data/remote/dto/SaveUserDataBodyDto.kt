package com.feedback.android.app.data.remote.dto

import okhttp3.MultipartBody

data class SaveUserDataBodyDto(
    val phone: String,
    val whatsapp: Int,
    val fullName: String,
    val email: String,
    val birthday: String,
    val aboutTitle: String,
    val aboutText: String,
    val profession: String,
    val website: String,
    val avatar: MultipartBody.Part
)

class SaveUserDataException(fields: List<Int>) : Exception(fields.joinToString(", "))