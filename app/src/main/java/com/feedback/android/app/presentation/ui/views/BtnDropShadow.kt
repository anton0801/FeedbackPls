package com.feedback.android.app.presentation.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.feedback.android.app.R
import com.google.android.material.button.MaterialButton
import android.graphics.Bitmap

import android.graphics.BlurMaskFilter
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import android.graphics.drawable.BitmapDrawable

import android.graphics.drawable.Drawable
import android.os.Build


class BtnDropShadow : AppCompatTextView {

    private var dropShadow: Int = 0
    private var dropWhenBtnActive: Boolean = false

    var isActive = false
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    @SuppressLint("ResourceType")
    private fun init(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BtnDropShadow, 0, 0)
        dropShadow = a.getInt(R.styleable.BtnDropShadow_dropShadow, 0)
        dropWhenBtnActive = a.getBoolean(R.styleable.BtnDropShadow_dropWhenIsActive, false)
        a.recycle()
        if (!dropWhenBtnActive) {
            setState(true)
        } else {
            setState(false)
        }
    }

    /**
     * Method set the button state active or inactive
     * */
    fun setState(active: Boolean) {
        if (active) {
//            val originalBitmap =
//                drawableToBitmap(context.resources.getDrawable(R.drawable.active_btn_state))
//            if (originalBitmap != null) {
//                this.setBackgroundDrawable(dropShadow(originalBitmap)?.toDrawable(context.resources))
//            } else {
//                this.setBackgroundResource(R.drawable.active_btn_state)
//            }
            this.setBackgroundResource(R.drawable.active_btn_state)
        } else {
            this.setBackgroundResource(R.drawable.in_active_btn_state)
        }
        isActive = active
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun dropShadow(originalBitmap: Bitmap): Bitmap? {
        val blurFilter = BlurMaskFilter(dropShadow.toFloat(), BlurMaskFilter.Blur.OUTER)
        val shadowPaint = Paint()
        shadowPaint.maskFilter = blurFilter
        val offsetXY = IntArray(2)
        val shadowImage = originalBitmap.extractAlpha(shadowPaint, offsetXY)

        /* Need to convert shadowImage from 8-bit to ARGB here. */
        val shadowImage32 = shadowImage.copy(Bitmap.Config.ARGB_8888, true)
        val c = Canvas(shadowImage32)
        c.drawBitmap(originalBitmap, -offsetXY[0].toFloat(), -offsetXY[1].toFloat(), null)
        return shadowImage32
    }

}