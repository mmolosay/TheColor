package com.ordolabs.data.remote.mapper

import com.ordolabs.data.remote.model.GetColorInformationResponse
import com.ordolabs.domain.model.ColorInformation

internal fun GetColorInformationResponse.toDomain() = ColorInformation(
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

    xyzFractionX = this.xyz.fraction.x,
    xyzFractionY = this.xyz.fraction.y,
    xyzFractionZ = this.xyz.fraction.z,
    xyzX = this.xyz.x,
    xyzY = this.xyz.y,
    xyzZ = this.xyz.z,
    xyzValue = this.xyz.value,

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
    exactNameHex = this.name.closest_named_hex,
    isNameMatchExact = this.name.exact_match_name,
    exactNameHexDistance = this.name.distance,

    imageBareUrl = this.image.bare,
    imageNamedUrl = this.image.named,

    contrastHex = this.contrast.value
)