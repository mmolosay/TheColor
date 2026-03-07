package io.github.mmolosay.thecolor.domain.color

import javax.inject.Inject

class IsColorLightUseCase @Inject constructor(
    private val getColorLightness: GetColorLightnessUseCase,
) {

    fun Color.isLight(threshold: Float = 0.60f): Boolean {
        val lightness = with(getColorLightness) { labLightness() }
        return (lightness >= threshold)
    }
}