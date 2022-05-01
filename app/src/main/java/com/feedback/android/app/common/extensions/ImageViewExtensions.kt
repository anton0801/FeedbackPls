package com.feedback.android.app.common.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.feedback.android.app.R

fun ImageView.downloadAndSetImageViewWithShimmer(url: String) {
    val shimmer =
        Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
            .setDuration(800) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.6f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }

    Glide.with(context)
        .load(url)
        .placeholder(shimmerDrawable)
        .error(R.drawable.ic_moderator_user_no_avatar)
        .into(this)
}

fun downloadImageAsBitmap(context: Context, url: String, onLoaded: (Bitmap) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .error(R.drawable.ic_moderator_user_no_avatar)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onLoaded(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}