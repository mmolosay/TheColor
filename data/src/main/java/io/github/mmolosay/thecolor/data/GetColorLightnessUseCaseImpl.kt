package io.github.mmolosay.thecolor.data

import com.github.ajalt.colormath.model.SRGB
import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorLightnessUseCase
import javax.inject.Inject

class GetColorLightnessUseCaseImpl @Inject constructor(
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
) : GetColorLightnessUseCase {

    override fun Color.labLightness(): Float {
        val hex = with(colorConverter) { toHex() }
        val hexString = with(colorMapper) { hex.toHexString() }
        val lab = SRGB(hex = hexString).toLAB()
        return (lab.l / 100) // LAB's L is in range 0..100, but we want in range 0..1
    }
}