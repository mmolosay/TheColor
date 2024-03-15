package io.github.mmolosay.thecolor.data.remote.mapper

import io.github.mmolosay.thecolor.data.remote.model.CmykModelResponse
import io.github.mmolosay.thecolor.data.remote.model.ColorDetailsResponse
import io.github.mmolosay.thecolor.data.remote.model.ColorSchemeResponse
import io.github.mmolosay.thecolor.data.remote.model.HslModelResponse
import io.github.mmolosay.thecolor.data.remote.model.HsvModelResponse
import io.github.mmolosay.thecolor.data.remote.model.NameResponse
import io.github.mmolosay.thecolor.data.remote.model.RgbModelResponse
import io.github.mmolosay.thecolor.data.remote.model.XyzModelResponse
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme

fun ColorDetailsResponse.toDomain() =
    ColorDetails(
        color = Color.Hex(this.hex.clean.toInt(radix = 16)), // TODO: use ColorFactory
        colorHexString = this.toDomainHexString(),
        colorTranslations = this.toDomainColorTranslations(),
        colorName = this.name.value,
        exact = this.name.toDomainExact(),
        matchesExact = this.name.exact_match_name,
        distanceFromExact = this.name.distance,
    )

private fun ColorDetailsResponse.toDomainHexString() =
    ColorDetails.ColorHexString(
        withNumberSign = this.hex.value,
        withoutNumberSign = this.hex.clean,
    )

private fun ColorDetailsResponse.toDomainColorTranslations() =
    ColorDetails.ColorTranslations(
        rgb = this.rgb.toDomainRgbColorTranslation(),
        hsl = this.hsl.toDomainHslColorTranslation(),
        hsv = this.hsv.toDomainHsvColorTranslation(),
        xyz = this.XYZ.toDomainXyzColorTranslation(),
        cmyk = this.cmyk.toDomainCmykColorTranslation(),
    )

private fun RgbModelResponse.toDomainRgbColorTranslation() =
    ColorDetails.ColorTranslation.Rgb(
        standard = ColorDetails.ColorTranslation.Rgb.StandardValues(
            r = this.r,
            g = this.g,
            b = this.b,
        ),
        normalized = ColorDetails.ColorTranslation.Rgb.NormalizedValues(
            r = this.fraction.r,
            g = this.fraction.g,
            b = this.fraction.b,
        ),
    )

private fun HslModelResponse.toDomainHslColorTranslation() =
    ColorDetails.ColorTranslation.Hsl(
        standard = ColorDetails.ColorTranslation.Hsl.StandardValues(
            h = this.h,
            s = this.s,
            l = this.l,
        ),
        normalized = ColorDetails.ColorTranslation.Hsl.NormalizedValues(
            h = this.fraction.h,
            s = this.fraction.s,
            l = this.fraction.l,
        ),
    )

private fun HsvModelResponse.toDomainHsvColorTranslation() =
    ColorDetails.ColorTranslation.Hsv(
        standard = ColorDetails.ColorTranslation.Hsv.StandardValues(
            h = this.h,
            s = this.s,
            v = this.v,
        ),
        normalized = ColorDetails.ColorTranslation.Hsv.NormalizedValues(
            h = this.fraction.h,
            s = this.fraction.s,
            v = this.fraction.v,
        ),
    )

private fun XyzModelResponse.toDomainXyzColorTranslation() =
    ColorDetails.ColorTranslation.Xyz(
        standard = ColorDetails.ColorTranslation.Xyz.StandardValues(
            x = this.X,
            y = this.Y,
            z = this.X,
        ),
        normalized = ColorDetails.ColorTranslation.Xyz.NormalizedValues(
            x = this.fraction.X,
            y = this.fraction.Y,
            z = this.fraction.Z,
        ),
    )

private fun CmykModelResponse.toDomainCmykColorTranslation() =
    ColorDetails.ColorTranslation.Cmyk(
        // for some colors backend returns 'null', but means zero
        standard = ColorDetails.ColorTranslation.Cmyk.StandardValues(
            c = this.c ?: 0,
            m = this.m ?: 0,
            y = this.y ?: 0,
            k = this.k ?: 0,
        ),
        normalized = ColorDetails.ColorTranslation.Cmyk.NormalizedValues(
            c = this.fraction.c ?: 0f,
            m = this.fraction.m ?: 0f,
            y = this.fraction.y ?: 0f,
            k = this.fraction.k ?: 0f,
        ),
    )

private fun NameResponse.toDomainExact() =
    ColorDetails.Exact(
        color = Color.Hex(
            this.closest_named_hex.trimStart('#').toInt(radix = 16)
        ), // TODO: use ColorFactory,
        hexStringWithNumberSign = this.closest_named_hex,
    )

fun ColorSchemeResponse.toDomain() =
    ColorScheme(
        swatchDetails = this.colors.map { it.toDomain() },
    )