package com.ordolabs.thecolor.util

import android.graphics.Point
import android.view.View
import androidx.annotation.IdRes
import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.SpringAnimation
import com.ordolabs.thecolor.R
import kotlin.math.hypot

object AnimationUtils {

    @IdRes
    fun getSpringPropertyKey(property: ViewProperty): Int = when (property) {
        SpringAnimation.TRANSLATION_X -> R.id.springPropertyKeyTranslationX
        SpringAnimation.TRANSLATION_Y -> R.id.springPropertyKeyTranslationY
        SpringAnimation.TRANSLATION_Z -> R.id.springPropertyKeyTranslationZ
        SpringAnimation.SCALE_X -> R.id.springPropertyKeyScaleX
        SpringAnimation.SCALE_Y -> R.id.springPropertyKeyScaleY
        SpringAnimation.ROTATION -> R.id.springPropertyKeyRotation
        SpringAnimation.ROTATION_X -> R.id.springPropertyKeyRotationX
        SpringAnimation.ROTATION_Y -> R.id.springPropertyKeyRotationY
        SpringAnimation.X -> R.id.springPropertyKeyX
        SpringAnimation.Y -> R.id.springPropertyKeyY
        SpringAnimation.Z -> R.id.springPropertyKeyZ
        SpringAnimation.ALPHA -> R.id.springPropertyKeyAlpha
        SpringAnimation.SCROLL_X -> R.id.springPropertyKeyScrollX
        SpringAnimation.SCROLL_Y -> R.id.springPropertyKeyScrollY
        else -> error("this animation property is not supported")
    }

    @IdRes
    fun getCircularRevealKey(): Int = R.id.animCircularRevealKey

    fun getCircularRevealMaxRadius(
        view: View,
        cx: Int = view.width / 2,
        cy: Int = view.height / 2
    ): Float {
        val x = (if (cx >= view.width / 2) 0 else view.width).toFloat()
        val y = (if (cy >= view.height / 2) 0 else view.height).toFloat()
        return hypot(x - cx, y - cy)
    }

    fun getCircularRevealMaxRadius(view: View, center: Point): Float {
        return getCircularRevealMaxRadius(view, center.x, center.y)
    }
}