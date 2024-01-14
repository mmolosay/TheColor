package io.github.mmolosay.presentation.util

import android.graphics.Point
import android.util.Property
import android.view.View
import androidx.annotation.IdRes
import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.SpringAnimation
import io.github.mmolosay.presentation.common.R
import kotlin.math.hypot

object AnimationUtils {

    @IdRes
    fun getSpringPropertyKey(property: ViewProperty): Int =
        when (property) {
            SpringAnimation.TRANSLATION_X -> R.id.animPropertyKeyTranslationX
            SpringAnimation.TRANSLATION_Y -> R.id.animPropertyKeyTranslationY
            SpringAnimation.TRANSLATION_Z -> R.id.animPropertyKeyTranslationZ
            SpringAnimation.SCALE_X -> R.id.animPropertyKeyScaleX
            SpringAnimation.SCALE_Y -> R.id.animPropertyKeyScaleY
            SpringAnimation.ROTATION -> R.id.animPropertyKeyRotation
            SpringAnimation.ROTATION_X -> R.id.animPropertyKeyRotationX
            SpringAnimation.ROTATION_Y -> R.id.animPropertyKeyRotationY
            SpringAnimation.X -> R.id.animPropertyKeyX
            SpringAnimation.Y -> R.id.animPropertyKeyY
            SpringAnimation.Z -> R.id.animPropertyKeyZ
            SpringAnimation.ALPHA -> R.id.animPropertyKeyAlpha
            SpringAnimation.SCROLL_X -> R.id.animPropertyKeyScrollX
            SpringAnimation.SCROLL_Y -> R.id.animPropertyKeyScrollY
            else -> error("this animation property is not supported")
        }

    @IdRes
    fun getViewPropertyKey(property: Property<*, *>): Int =
        when (property) {
            View.TRANSLATION_X -> R.id.animPropertyKeyTranslationX
            View.TRANSLATION_Y -> R.id.animPropertyKeyTranslationY
            View.TRANSLATION_Z -> R.id.animPropertyKeyTranslationZ
            View.SCALE_X -> R.id.animPropertyKeyScaleX
            View.SCALE_Y -> R.id.animPropertyKeyScaleY
            View.ROTATION -> R.id.animPropertyKeyRotation
            View.ROTATION_X -> R.id.animPropertyKeyRotationX
            View.ROTATION_Y -> R.id.animPropertyKeyRotationY
            View.X -> R.id.animPropertyKeyX
            View.Y -> R.id.animPropertyKeyY
            View.Z -> R.id.animPropertyKeyZ
            View.ALPHA -> R.id.animPropertyKeyAlpha
            else -> error("this animation property is not supported")
        }

    fun getCircularRevealMaxRadius(
        view: View,
        cx: Int = view.width / 2,
        cy: Int = view.height / 2
    ): Float {
        val x = (if (cx >= view.width / 2) 0 else view.width).toFloat()
        val y = (if (cy >= view.height / 2) 0 else view.height).toFloat()
        return hypot(x - cx, y - cy)
    }

    fun getCircularRevealMaxRadius(
        view: View,
        center: Point
    ): Float {
        return getCircularRevealMaxRadius(view, center.x, center.y)
    }

    enum class CustomViewProperty(@IdRes val key: Int) {
        CIRCULAR_REVEAL(R.id.animPropertyKeyCircularReveal)
    }
}