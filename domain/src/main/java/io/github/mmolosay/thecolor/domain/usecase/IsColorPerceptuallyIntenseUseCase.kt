package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

class IsColorPerceptuallyIntenseUseCase @Inject constructor(
    private val colorMath: ColorMath,
    private val colorConverter: ColorConverter,
) {

    fun Color.isPerceptuallyIntense(): Boolean {
        val intensity = this.perceptualIntensity()
        val threshold = 0.50f
        return (intensity > threshold)
    }

    private fun Color.perceptualIntensity(): Float {
        // https://stackoverflow.com/a/596243/8862499
        val rgb = with(colorConverter) { toRgb() }
        val intensity = kotlin.run {
            val r = rgb.r.toFloat().pow(2) * 0.299f
            val g = rgb.g.toFloat().pow(2) * 0.587f
            val b = rgb.b.toFloat().pow(2) * 0.114f
            sqrt(r + g + b)
        }
        val normalizedIntensity = intensity / 255f
        return normalizedIntensity
    }
}