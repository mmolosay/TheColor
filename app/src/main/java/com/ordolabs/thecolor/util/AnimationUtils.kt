package com.ordolabs.thecolor.util

import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.SpringAnimation

object AnimationUtils {

    fun getSpringPropertyKey(property: ViewProperty): Int = when (property) {
        SpringAnimation.TRANSLATION_X -> 770
        SpringAnimation.TRANSLATION_Y -> 771
        SpringAnimation.TRANSLATION_Z -> 772
        SpringAnimation.SCALE_X -> 773
        SpringAnimation.SCALE_Y -> 774
        SpringAnimation.ROTATION -> 775
        SpringAnimation.ROTATION_X -> 776
        SpringAnimation.ROTATION_Y -> 777
        SpringAnimation.X -> 778
        SpringAnimation.Y -> 779
        SpringAnimation.Z -> 780
        SpringAnimation.ALPHA -> 781
        SpringAnimation.SCROLL_X -> 782
        SpringAnimation.SCROLL_Y -> 783
        else -> error("this animation property is not supported")
    }
}