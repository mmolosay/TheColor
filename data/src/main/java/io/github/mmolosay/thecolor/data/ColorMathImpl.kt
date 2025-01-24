package io.github.mmolosay.thecolor.data

import com.github.ajalt.colormath.model.SRGB
import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorMath
import javax.inject.Inject

/**
 * An implementation of [ColorMath] powered by [Colormath](https://github.com/ajalt/colormath)
 * library.
 */
class ColorMathImpl @Inject constructor(
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
) : ColorMath {

    override fun Color.toLab(): ColorMath.Lab {
        val hex = with(colorConverter) { toHex() }
        val hexString = with(colorMapper) { hex.toHexString() }
        val lab = SRGB(hex = hexString).toLAB()
        return ColorMath.Lab(
            l = lab.l,
            a = lab.a,
            b = lab.b,
        )
    }
}