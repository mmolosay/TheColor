package io.github.mmolosay.thecolor.data

import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.colormath.model.RGB.Companion.invoke
import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorConstants
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.DimColorUseCase
import javax.inject.Inject

class DimColorUseCaseImpl @Inject constructor(
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
) : DimColorUseCase {

    override fun Color.dim(desiredLightness: Float): Color {
        require(desiredLightness >= 0f || desiredLightness < 1f)
        val hex = with(colorConverter) { toHex() }
        val hexString = with(colorMapper) { hex.toHexString() }
        val lab = RGB(hex = hexString).toLAB()
        val lightness = (lab.l / 100) // LAB's L is in range 0..100, but we want in range 0..1
        if (lightness <= desiredLightness) return this
        @Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
        val dimmedRgb = lab
            .copy(l = desiredLightness * 100) // convert back into range 0..100
            .toSRGB()
        /*
         * There's a bug in Colormath that produces negative 0-255 green component when
         * input color of this function is #F9031B
         */
        fun Int.coerceInRgbComponentRange() =
            this.coerceIn(ColorConstants.RgbColorComponentIntRange)
        return Color.Rgb(
            r = dimmedRgb.redInt.coerceInRgbComponentRange(),
            g = dimmedRgb.greenInt.coerceInRgbComponentRange(),
            b = dimmedRgb.blueInt.coerceInRgbComponentRange(),
        )
    }
}