package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

class IsColorLightUseCase @Inject constructor(
    private val getColorLightness: GetColorLightnessUseCase,
) {

    fun Color.isLight(threshold: Float = 0.60f): Boolean {
        val lightness = with(getColorLightness) { hslLightness() }
        return (lightness >= threshold)
    }
}