package com.ordolabs.thecolor.util.wrapper

import android.animation.TimeInterpolator

class ReverseInterpolator(
    private val interpolator: TimeInterpolator,
    private var origin: Float
) : TimeInterpolator {

    init {
        require(interpolator !is ReverseInterpolator)
        require(origin in 0f..1f)
    }

    override fun getInterpolation(input: Float): Float {
        val reversed = 2 * origin - input
        val coerced = reversed.coerceAtLeast(0f)
        return interpolator.getInterpolation(coerced)
    }
}