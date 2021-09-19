package com.ordolabs.thecolor.util

import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.SpringAnimation

object AnimationUtils {

    private const val SPRING_PROPERTY_KEY_TRANSLATION_X = 77000
    private const val SPRING_PROPERTY_KEY_TRANSLATION_Y = 77001
    private const val SPRING_PROPERTY_KEY_TRANSLATION_Z = 77002
    private const val SPRING_PROPERTY_KEY_SCALE_X = 77003
    private const val SPRING_PROPERTY_KEY_SCALE_Y = 77004
    private const val SPRING_PROPERTY_KEY_ROTATION = 77005
    private const val SPRING_PROPERTY_KEY_ROTATION_X = 77006
    private const val SPRING_PROPERTY_KEY_ROTATION_Y = 77007
    private const val SPRING_PROPERTY_KEY_X = 77008
    private const val SPRING_PROPERTY_KEY_Y = 77009
    private const val SPRING_PROPERTY_KEY_Z = 77010
    private const val SPRING_PROPERTY_KEY_ALPHA = 77011
    private const val SPRING_PROPERTY_KEY_SCROLL_X = 77012
    private const val SPRING_PROPERTY_KEY_SCROLL_Y = 77013

    private const val CIRCULAR_REVEAL_KEY = 88000

    fun getSpringPropertyKey(property: ViewProperty): Int = when (property) {
        SpringAnimation.TRANSLATION_X -> SPRING_PROPERTY_KEY_TRANSLATION_X
        SpringAnimation.TRANSLATION_Y -> SPRING_PROPERTY_KEY_TRANSLATION_Y
        SpringAnimation.TRANSLATION_Z -> SPRING_PROPERTY_KEY_TRANSLATION_Z
        SpringAnimation.SCALE_X -> SPRING_PROPERTY_KEY_SCALE_X
        SpringAnimation.SCALE_Y -> SPRING_PROPERTY_KEY_SCALE_Y
        SpringAnimation.ROTATION -> SPRING_PROPERTY_KEY_ROTATION
        SpringAnimation.ROTATION_X -> SPRING_PROPERTY_KEY_ROTATION_X
        SpringAnimation.ROTATION_Y -> SPRING_PROPERTY_KEY_ROTATION_Y
        SpringAnimation.X -> SPRING_PROPERTY_KEY_X
        SpringAnimation.Y -> SPRING_PROPERTY_KEY_Y
        SpringAnimation.Z -> SPRING_PROPERTY_KEY_Z
        SpringAnimation.ALPHA -> SPRING_PROPERTY_KEY_ALPHA
        SpringAnimation.SCROLL_X -> SPRING_PROPERTY_KEY_SCROLL_X
        SpringAnimation.SCROLL_Y -> SPRING_PROPERTY_KEY_SCROLL_Y
        else -> error("this animation property is not supported")
    }

    fun getCircularRevealKey(): Int = CIRCULAR_REVEAL_KEY
}