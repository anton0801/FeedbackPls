package com.feedback.android.app.common

class Event<out T>(private val content: T) {

    var isHandled = false
        private set

    fun getContentIfNotHandled(): T? = if (!isHandled) {
        isHandled = true
        content
    } else null

    fun peekContent(): T = content

    override fun toString(): String {
        return "$isHandled, $content"
    }

}