package io.github.mmolosay.thecolor.domain.model

// TODO: refactor and use nested data classes for data grouping
data class ColorDetails(
    val color: Color,

    val hexValue: String,
    val hexClean: String,

    val rgbFractionR: Float,
    val rgbFractionG: Float,
    val rgbFractionB: Float,
    val rgbR: Int,
    val rgbG: Int,
    val rgbB: Int,
    val rgbValue: String,

    val hslFractionH: Float,
    val hslFractionS: Float,
    val hslFractionL: Float,
    val hslH: Int,
    val hslS: Int,
    val hslL: Int,
    val hslValue: String,

    val hsvFractionH: Float,
    val hsvFractionS: Float,
    val hsvFractionV: Float,
    val hsvH: Int,
    val hsvS: Int,
    val hsvV: Int,
    val hsvValue: String,

    val xyzFractionX: Float,
    val xyzFractionY: Float,
    val xyzFractionZ: Float,
    val xyzX: Int,
    val xyzY: Int,
    val xyzZ: Int,
    val xyzValue: String,

    val cmykFractionC: Float,
    val cmykFractionM: Float,
    val cmykFractionY: Float,
    val cmykFractionK: Float,
    val cmykC: Int,
    val cmykM: Int,
    val cmykY: Int,
    val cmykK: Int,
    val cmykValue: String,

    val name: String,
    val exact: Color,
    val exactNameHex: String,
    val isNameMatchExact: Boolean,
    val exactNameHexDistance: Int,

    val imageBareUrl: String,
    val imageNamedUrl: String,

    val contrastHex: String,
)