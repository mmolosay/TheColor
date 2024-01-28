package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.ColorLightnessRepository
import javax.inject.Inject

class IsColorLightUseCase @Inject constructor(
    private val colorLightnessRepository: ColorLightnessRepository,
) {

    fun Color.isLight(threshold: Float = 0.60f): Boolean {
        val lightness = colorLightnessRepository.hslLightness(color = this)
        return (lightness >= threshold)
    }
}