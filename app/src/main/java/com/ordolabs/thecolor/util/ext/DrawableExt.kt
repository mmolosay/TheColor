package com.ordolabs.thecolor.util.ext

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.annotation.ColorInt

fun Drawable.setColor(@ColorInt color: Int) {
    when (this) {
        is ColorDrawable -> this.color = color
        is ShapeDrawable -> this.setColor(color)
        is LayerDrawable -> this.setColor(color)
        is GradientDrawable -> this.setColor(color)
        else -> error("this drawable type is not supported")
    }
}

fun LayerDrawable.setColor(@ColorInt color: Int) {
    repeat(this.numberOfLayers) { index ->
        this.getDrawable(index).setColor(color)
    }
}

fun ShapeDrawable.setColor(@ColorInt color: Int) {
    this.paint.color = color
}