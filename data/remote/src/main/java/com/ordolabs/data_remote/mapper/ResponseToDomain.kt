package com.ordolabs.data_remote.mapper

import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.data_remote.model.ColorDetailsResponse
import com.ordolabs.data_remote.model.ColorSchemeResponse
import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.model.ColorScheme

fun ColorDetailsResponse.toDomain() = ColorDetails(
    hexValue = this.hex?.value,
    hexClean = this.hex?.clean,

    rgbFractionR = this.rgb?.fraction?.r,
    rgbFractionG = this.rgb?.fraction?.g,
    rgbFractionB = this.rgb?.fraction?.b,
    rgbR = this.rgb?.r,
    rgbG = this.rgb?.g,
    rgbB = this.rgb?.b,
    rgbValue = this.rgb?.value,

    hslFractionH = this.hsl?.fraction?.h,
    hslFractionS = this.hsl?.fraction?.s,
    hslFractionL = this.hsl?.fraction?.l,
    hslH = this.hsl?.h,
    hslS = this.hsl?.s,
    hslL = this.hsl?.l,
    hslValue = this.hsl?.value,

    hsvFractionH = this.hsv?.fraction?.h,
    hsvFractionS = this.hsv?.fraction?.s,
    hsvFractionV = this.hsv?.fraction?.v,
    hsvH = this.hsv?.h,
    hsvS = this.hsv?.s,
    hsvV = this.hsv?.v,
    hsvValue = this.hsv?.value,

    xyzFractionX = this.XYZ?.fraction?.X,
    xyzFractionY = this.XYZ?.fraction?.Y,
    xyzFractionZ = this.XYZ?.fraction?.Z,
    xyzX = this.XYZ?.X,
    xyzY = this.XYZ?.Y,
    xyzZ = this.XYZ?.Z,
    xyzValue = this.XYZ?.value,

    cmykFractionC = this.cmyk?.fraction?.c,
    cmykFractionM = this.cmyk?.fraction?.m,
    cmykFractionY = this.cmyk?.fraction?.y,
    cmykFractionK = this.cmyk?.fraction?.k,
    cmykC = this.cmyk?.c,
    cmykM = this.cmyk?.m,
    cmykY = this.cmyk?.y,
    cmykK = this.cmyk?.k,
    cmykValue = this.cmyk?.value,

    name = this.name?.value,
    exactNameHex = this.name?.closest_named_hex,
    isNameMatchExact = this.name?.exact_match_name,
    exactNameHexDistance = this.name?.distance,

    imageBareUrl = this.image?.bare,
    imageNamedUrl = this.image?.named,

    contrastHex = this.contrast?.value
)

fun ColorSchemeResponse.toDomain() = ColorScheme(
    mode = this.mode?.toDomain(),
    sampleCount = this.sampleCount,
    colors = this.colors?.map { it.toDomain() },
    seed = this.seed?.toDomain()
)

fun TheColorApiService.SchemeMode.toDomain() =
    ColorScheme.SchemeMode.values()[this.ordinal]