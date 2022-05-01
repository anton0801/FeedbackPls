package com.feedback.android.app.common

import android.content.Context
import javax.inject.Inject

class SharedManager @Inject constructor(
    private val context: Context
) {

    private val sharedPreferences = context.getSharedPreferences("feedback_pls_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun putString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String) = sharedPreferences.getString(key, "") ?: ""

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String) = sharedPreferences.getBoolean(key, false)


}