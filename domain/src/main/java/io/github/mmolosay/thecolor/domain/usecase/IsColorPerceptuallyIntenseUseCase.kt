package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

class IsColorPerceptuallyIntenseUseCase @Inject constructor(
    private val colorMath: ColorMath,
) {

    fun Color.isPerceptuallyIntense(): Boolean {
        val intensity = this.perceptualIntensity()
        val threshold = 90f
        return (intensity > threshold)
    }

    /**
     * Returns human-perceptual intensity of a [Color] in a range from 0 to ≈180+.
     * The upper bound is not fixed (but could be theoretically) because of input [Color]'s
     * color space and gamut details. For example, sRGB and CIE LAB gamuts will produce maximum
     * perceptual intensity value of ≈180.28. Extended color spaces (like ProPhoto RGB) can produce
     * values up to ≈300.
     */
    private fun Color.perceptualIntensity(): Float {
        val lab = with(colorMath) { toLab() }
        val chroma = hypot(lab.a, lab.b) // √(A² + B²)
        val k = 1 // chroma scaling factor
        val perceptualIntensity = sqrt(lab.l.pow(2) + (k * chroma.pow(2))) // √(L² + (k × Chroma²))
        return perceptualIntensity
    }
}