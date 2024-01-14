package io.github.mmolosay.presentation.mapper

import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.data.ColorDetails
import io.github.mmolosay.presentation.model.color.data.ColorScheme
import io.github.mmolosay.presentation.util.ext.getFromEnumOrNull
import com.ordolabs.domain.model.ColorDetails as ColorDetailsDomain
import com.ordolabs.domain.model.ColorScheme as ColorSchemeDomain

// region ColorDetails

fun ColorDetailsDomain.toPresentation() = ColorDetails(
    color = this.toColorPresentation(),
    spaces = this.toSpacesPresentation(),
    exact = this.toExactPresentation()
)

// region Color

private fun ColorDetailsDomain.toColorPresentation() =
    this.hexValue?.let { Color(hex = it) }

// endregion

// region Space

private fun ColorDetailsDomain.toSpacesPresentation() =
    ColorDetails.Spaces(
        hex = this.toHexSpacePresentation(),
        rgb = this.toRgbSpacePresentation(),
        hsl = this.toHslSpacePresentation(),
        hsv = this.toHsvSpacePresentation(),
        cmyk = this.toCmykSpacePresentation()
    )

private fun ColorDetailsDomain.toHexSpacePresentation() =
    ColorDetails.Spaces.Hex(
        signed = this.hexValue,
        signless = this.hexClean
    )

private fun ColorDetailsDomain.toRgbSpacePresentation() =
    ColorDetails.Spaces.Rgb(
        r = this.rgbR,
        g = this.rgbG,
        b = this.rgbB
    )

private fun ColorDetailsDomain.toHslSpacePresentation() =
    ColorDetails.Spaces.Hsl(
        h = this.hslH,
        s = this.hslS,
        l = this.hslL
    )

private fun ColorDetailsDomain.toHsvSpacePresentation() =
    ColorDetails.Spaces.Hsv(
        h = this.hsvH,
        s = this.hsvS,
        v = this.hsvV
    )

private fun ColorDetailsDomain.toCmykSpacePresentation() =
    ColorDetails.Spaces.Cmyk(
        c = this.cmykC,
        m = this.cmykM,
        y = this.cmykY,
        k = this.cmykK
    )

// endregion

// region Exact

private fun ColorDetailsDomain.toExactPresentation() =
    ColorDetails.Exact(
        name = this.name,
        color = this.toExactColorPresentation(),
        distance = this.exactNameHexDistance,
        isMatch = this.isNameMatchExact
    )

private fun ColorDetailsDomain.toExactColorPresentation() =
    this.exactNameHex?.let { Color(it) }

// endregion

// endregion

// region ColorScheme

fun ColorSchemeDomain.toPresentation() = ColorScheme(
    mode = getFromEnumOrNull<ColorScheme.Mode>(this.modeOrdinal),
    samples = this.colors?.mapNotNull m@{
        val details = it.toPresentation()
        val hex = details.spaces.hex.signed ?: return@m null
        ColorScheme.Sample(hex, details)
    },
    seed = this.seed?.toPresentation()
)


// endregion