package com.ordolabs.thecolor.mapper

import com.ordolabs.thecolor.model.color.data.ColorDetails
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import com.ordolabs.domain.model.ColorDetails as ColorDetailsDomain
import com.ordolabs.domain.model.ColorScheme as ColorSchemeDomain


fun ColorDetailsDomain.toPresentation() = ColorDetails(
    hexValue = this.hexValue,
    hexClean = this.hexClean,

    rgbFractionR = this.rgbFractionR,
    rgbFractionG = this.rgbFractionG,
    rgbFractionB = this.rgbFractionB,
    rgbR = this.rgbR,
    rgbG = this.rgbG,
    rgbB = this.rgbB,
    rgbValue = this.rgbValue,

    hslFractionH = this.hslFractionH,
    hslFractionS = this.hslFractionS,
    hslFractionL = this.hslFractionL,
    hslH = this.hslH,
    hslS = this.hslS,
    hslL = this.hslL,
    hslValue = this.hslValue,

    hsvFractionH = this.hsvFractionH,
    hsvFractionS = this.hsvFractionS,
    hsvFractionV = this.hsvFractionV,
    hsvH = this.hsvH,
    hsvS = this.hsvS,
    hsvV = this.hsvV,
    hsvValue = this.hsvValue,

    xyzFractionX = this.xyzFractionX,
    xyzFractionY = this.xyzFractionY,
    xyzFractionZ = this.xyzFractionZ,
    xyzX = this.xyzX,
    xyzY = this.xyzY,
    xyzZ = this.xyzZ,
    xyzValue = this.xyzValue,

    cmykFractionC = this.cmykFractionC,
    cmykFractionM = this.cmykFractionM,
    cmykFractionY = this.cmykFractionY,
    cmykFractionK = this.cmykFractionK,
    cmykC = this.cmykC,
    cmykM = this.cmykM,
    cmykY = this.cmykY,
    cmykK = this.cmykK,
    cmykValue = this.cmykValue,

    name = this.name,
    exactNameHex = this.exactNameHex?.substring(1),
    exactNameHexSigned = this.exactNameHex,
    isNameMatchExact = this.isNameMatchExact,
    exactNameHexDistance = this.exactNameHexDistance,

    imageBareUrl = this.imageBareUrl,
    imageNamedUrl = this.imageNamedUrl,

    contrastHex = this.contrastHex
)

fun ColorSchemeDomain.toPresentation() = ColorScheme(
    mode = getFromEnumOrNull<ColorScheme.Mode>(this.modeOrdinal),
    samples = this.colors?.mapNotNull m@{
        val details = it.toPresentation()
        val hex = details.hexValue ?: return@m null
        ColorScheme.Sample(hex, details)
    },
    seed = this.seed?.toPresentation()
)
