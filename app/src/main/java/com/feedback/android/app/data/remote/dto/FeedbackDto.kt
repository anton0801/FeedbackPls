package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedbackDto(
    val comment: String,
    @SerializedName("created_at")
    val createdAt: String,
    val id: Int,
    @SerializedName("img_src")
    val imgSrc: String,
    val rating: Int,
    @SerializedName("specialist_id")
    val specialistId: Int,
    val src: String,
    val timestamp: Int,
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String
)