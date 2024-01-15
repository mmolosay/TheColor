package io.github.mmolosay.thecolor.presentation.util.ext

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.annotation.ColorInt

@ColorInt
fun Drawable.getColor(): Int {
    return when (this) {
        is ColorDrawable -> this.color
        else -> error("this drawable type is not supported")
    }
}

/**
 * Changes color of `this` drawable.
 * Don't forget to call `View.invalidate()`, if the drawable is a view's background.
 */
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