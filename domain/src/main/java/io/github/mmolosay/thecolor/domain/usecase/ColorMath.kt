package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color

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
    )
}