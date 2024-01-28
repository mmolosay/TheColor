package io.github.mmolosay.thecolor.data

import com.github.ajalt.colormath.model.RGB
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.ColorLightnessRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import javax.inject.Inject

class ColorLightnessRepositoryImpl @Inject constructor(
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
) : ColorLightnessRepository {

    override fun hslLightness(color: Color): Float {
        val hex = with(colorConverter) { color.toAbstract().toHex() }
        val hexString = with(colorMapper) { hex.toHexString() }
        val hsl = RGB(hex = hexString).toHSL()
        return hsl.l
    }
}