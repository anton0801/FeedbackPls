package com.feedback.android.app.common

data class ResourceState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)