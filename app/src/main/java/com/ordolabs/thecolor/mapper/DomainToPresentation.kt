package com.ordolabs.thecolor.mapper

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.thecolor.model.ColorDetailsPresentation

fun ColorDetails.toPresentation() = ColorDetailsPresentation(
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
    exactNameHex = this.exactNameHex,
    isNameMatchExact = this.isNameMatchExact,
    exactNameHexDistance = this.exactNameHexDistance,

    imageBareUrl = this.imageBareUrl,
    imageNamedUrl = this.imageNamedUrl,

    contrastHex = this.contrastHex
)