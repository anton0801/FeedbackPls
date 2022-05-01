package com.feedback.android.app.common.extensions

import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.widget.AppCompatTextView

fun AppCompatTextView.setUnderlineEffect() {
    val content = SpannableString(this.text)
    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    this.text = content
}