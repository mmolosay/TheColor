package io.github.mmolosay.thecolor.data.remote.mapper

import io.github.mmolosay.thecolor.data.remote.model.ColorDetailsDto
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import javax.inject.Inject

/**
 * Maps [ColorDetailsDto] model of Data layer to [ColorDetails] model of Domain layer.
 */
class ColorDetailsMapper @Inject constructor() {

    fun ColorDetailsDto.toDomain(): ColorDetails =
        ColorDetails(
            color = Color.Hex(this.hex.valueWithoutNumberSign.toInt(radix = 16)), // TODO: use ColorFactory
            colorHexString = this.toDomainHexString(),
            colorTranslations = this.toDomainColorTranslations(),
            colorName = this.name.colorName,
            exact = this.name.toDomainExact(),
            matchesExact = this.name.exactMatch,
            distanceFromExact = this.name.distanceFromExact,
        )

    private fun ColorDetailsDto.toDomainHexString() =
        ColorDetails.ColorHexString(
            withNumberSign = this.hex.valueWithNumberSign,
            withoutNumberSign = this.hex.valueWithoutNumberSign,
        )

    private fun ColorDetailsDto.toDomainColorTranslations() =
        ColorDetails.ColorTranslations(
            rgb = this.rgb.toDomainRgbColorTranslation(),
            hsl = this.hsl.toDomainHslColorTranslation(),
            hsv = this.hsv.toDomainHsvColorTranslation(),
            xyz = this.xyz.toDomainXyzColorTranslation(),
            cmyk = this.cmyk.toDomainCmykColorTranslation(),
        )

    private fun ColorDetailsDto.Rgb.toDomainRgbColorTranslation() =
        ColorDetails.ColorTranslation.Rgb(
            standard = ColorDetails.ColorTranslation.Rgb.StandardValues(
                r = this.r,
                g = this.g,
                b = this.b,
            ),
            normalized = ColorDetails.ColorTranslation.Rgb.NormalizedValues(
                r = this.normalized.r,
                g = this.normalized.g,
                b = this.normalized.b,
            ),
        )

    private fun ColorDetailsDto.Hsl.toDomainHslColorTranslation() =
        ColorDetails.ColorTranslation.Hsl(
            standard = ColorDetails.ColorTranslation.Hsl.StandardValues(
                h = this.h,
                s = this.s,
                l = this.l,
            ),
            normalized = ColorDetails.ColorTranslation.Hsl.NormalizedValues(
                h = this.normalized.h,
                s = this.normalized.s,
                l = this.normalized.l,
            ),
        )

    private fun ColorDetailsDto.Hsv.toDomainHsvColorTranslation() =
        ColorDetails.ColorTranslation.Hsv(
            standard = ColorDetails.ColorTranslation.Hsv.StandardValues(
                h = this.h,
                s = this.s,
                v = this.v,
            ),
            normalized = ColorDetails.ColorTranslation.Hsv.NormalizedValues(
                h = this.normalized.h,
                s = this.normalized.s,
                v = this.normalized.v,
            ),
        )

    private fun ColorDetailsDto.Xyz.toDomainXyzColorTranslation() =
        ColorDetails.ColorTranslation.Xyz(
            standard = ColorDetails.ColorTranslation.Xyz.StandardValues(
                x = this.x,
                y = this.y,
                z = this.x,
            ),
            normalized = ColorDetails.ColorTranslation.Xyz.NormalizedValues(
                x = this.normalized.x,
                y = this.normalized.y,
                z = this.normalized.z,
            ),
        )

    private fun ColorDetailsDto.Cmyk.toDomainCmykColorTranslation() =
        ColorDetails.ColorTranslation.Cmyk(
            // for some colors backend returns 'null', but means zero
            standard = ColorDetails.ColorTranslation.Cmyk.StandardValues(
                c = this.c ?: 0,
                m = this.m ?: 0,
                y = this.y ?: 0,
                k = this.k ?: 0,
            ),
            normalized = ColorDetails.ColorTranslation.Cmyk.NormalizedValues(
                c = this.normalized.c ?: 0f,
                m = this.normalized.m ?: 0f,
                y = this.normalized.y ?: 0f,
                k = this.normalized.k ?: 0f,
            ),
        )

    private fun ColorDetailsDto.Name.toDomainExact() =
        ColorDetails.Exact(
            color = Color.Hex(
                this.hexValueWithNumberSignOfExactColor.trimStart('#').toInt(radix = 16)
            ), // TODO: use ColorFactory,
            hexStringWithNumberSign = this.hexValueWithNumberSignOfExactColor,
        )
}