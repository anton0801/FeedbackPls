package com.feedback.android.app.data.remote.dto

import com.google.gson.annotations.SerializedName

open class BaseResponse<T> {
    var status: String? = null

    @SerializedName("data")
    var data: T? = null
    var message: String? = null
    val error: String? = null

    override fun toString(): String {
        return "BaseResponse(status=$status, data=$data, message=$message)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseResponse<*>

        if (status != other.status) return false
        if (data != other.data) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status?.hashCode() ?: 0
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }

}