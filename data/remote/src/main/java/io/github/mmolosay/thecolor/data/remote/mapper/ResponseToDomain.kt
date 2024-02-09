package io.github.mmolosay.thecolor.data.remote.mapper

import io.github.mmolosay.thecolor.data.remote.model.ColorDetailsResponse
import io.github.mmolosay.thecolor.data.remote.model.ColorSchemeResponse
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.OldColorScheme

fun ColorDetailsResponse.toDomain() = ColorDetails(
    color = Color.Hex(this.hex.clean.toInt(radix = 16)), // TODO: use ColorFactory

    hexValue = this.hex.value,
    hexClean = this.hex.clean,

    rgbFractionR = this.rgb.fraction.r,
    rgbFractionG = this.rgb.fraction.g,
    rgbFractionB = this.rgb.fraction.b,
    rgbR = this.rgb.r,
    rgbG = this.rgb.g,
    rgbB = this.rgb.b,
    rgbValue = this.rgb.value,

    hslFractionH = this.hsl.fraction.h,
    hslFractionS = this.hsl.fraction.s,
    hslFractionL = this.hsl.fraction.l,
    hslH = this.hsl.h,
    hslS = this.hsl.s,
    hslL = this.hsl.l,
    hslValue = this.hsl.value,

    hsvFractionH = this.hsv.fraction.h,
    hsvFractionS = this.hsv.fraction.s,
    hsvFractionV = this.hsv.fraction.v,
    hsvH = this.hsv.h,
    hsvS = this.hsv.s,
    hsvV = this.hsv.v,
    hsvValue = this.hsv.value,

    xyzFractionX = this.XYZ.fraction.X,
    xyzFractionY = this.XYZ.fraction.Y,
    xyzFractionZ = this.XYZ.fraction.Z,
    xyzX = this.XYZ.X,
    xyzY = this.XYZ.Y,
    xyzZ = this.XYZ.Z,
    xyzValue = this.XYZ.value,

    cmykFractionC = this.cmyk.fraction.c,
    cmykFractionM = this.cmyk.fraction.m,
    cmykFractionY = this.cmyk.fraction.y,
    cmykFractionK = this.cmyk.fraction.k,
    cmykC = this.cmyk.c,
    cmykM = this.cmyk.m,
    cmykY = this.cmyk.y,
    cmykK = this.cmyk.k,
    cmykValue = this.cmyk.value,

    name = this.name.value,
    exact = Color.Hex(this.name.closest_named_hex.trimStart('#').toInt(radix = 16)), // TODO: use ColorFactory
    exactNameHex = this.name.closest_named_hex,
    isNameMatchExact = this.name.exact_match_name,
    exactNameHexDistance = this.name.distance,

    imageBareUrl = this.image.bare,
    imageNamedUrl = this.image.named,

    contrastHex = this.contrast.value,
)

fun ColorSchemeResponse.toDomain() = OldColorScheme(
    modeOrdinal = this.mode.ordinal,
    sampleCount = this.sampleCount,
    colors = this.colors.map { it.toDomain() },
    seed = this.seed.toDomain()
)