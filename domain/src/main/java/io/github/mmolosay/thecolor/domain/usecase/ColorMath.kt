package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.utils.maxAbs

/**
 * Performs mathematical operations with colors.
 *
 * It is an interface, because the actual implementation is powered by an external library,
 * thus is implemented in Data architectural layer.
 */
interface ColorMath {

    fun Color.toLab(): Lab

    /** CIE LAB */
    data class Lab(
        val l: Float,
        val a: Float,
        val b: Float,
    ) {
        companion object {
            val LGamut = 0f..100f
            val AGamut = -128f..+127f
            val BGamut = -128f..+127f
        }
    }
}