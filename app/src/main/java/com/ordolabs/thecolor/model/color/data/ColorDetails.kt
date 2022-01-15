package com.ordolabs.thecolor.model.color.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorDetails(
    val hexValue: String?,
    val hexClean: String?,

    val rgbFractionR: Float?,
    val rgbFractionG: Float?,
    val rgbFractionB: Float?,
    val rgbR: Int?,
    val rgbG: Int?,
    val rgbB: Int?,
    val rgbValue: String?,

    val hslFractionH: Float?,
    val hslFractionS: Float?,
    val hslFractionL: Float?,
    val hslH: Int?,
    val hslS: Int?,
    val hslL: Int?,
    val hslValue: String?,

    val hsvFractionH: Float?,
    val hsvFractionS: Float?,
    val hsvFractionV: Float?,
    val hsvH: Int?,
    val hsvS: Int?,
    val hsvV: Int?,
    val hsvValue: String?,

    val xyzFractionX: Float?,
    val xyzFractionY: Float?,
    val xyzFractionZ: Float?,
    val xyzX: Int?,
    val xyzY: Int?,
    val xyzZ: Int?,
    val xyzValue: String?,

    val cmykFractionC: Float?,
    val cmykFractionM: Float?,
    val cmykFractionY: Float?,
    val cmykFractionK: Float?,
    val cmykC: Int?,
    val cmykM: Int?,
    val cmykY: Int?,
    val cmykK: Int?,
    val cmykValue: String?,

    val name: String?,
    val exactNameHex: String?,
    val exactNameHexSigned: String?,
    val isNameMatchExact: Boolean?,
    val exactNameHexDistance: Int?,

    val imageBareUrl: String?,
    val imageNamedUrl: String?,

    val contrastHex: String?
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (other !is ColorDetails) return false
        other.hexValue?.let {
            if (this.hexValue == other.hexValue) return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = hexValue?.hashCode() ?: 0
        result = 31 * result + (hexClean?.hashCode() ?: 0)
        result = 31 * result + (rgbFractionR?.hashCode() ?: 0)
        result = 31 * result + (rgbFractionG?.hashCode() ?: 0)
        result = 31 * result + (rgbFractionB?.hashCode() ?: 0)
        result = 31 * result + (rgbR ?: 0)
        result = 31 * result + (rgbG ?: 0)
        result = 31 * result + (rgbB ?: 0)
        result = 31 * result + (rgbValue?.hashCode() ?: 0)
        result = 31 * result + (hslFractionH?.hashCode() ?: 0)
        result = 31 * result + (hslFractionS?.hashCode() ?: 0)
        result = 31 * result + (hslFractionL?.hashCode() ?: 0)
        result = 31 * result + (hslH ?: 0)
        result = 31 * result + (hslS ?: 0)
        result = 31 * result + (hslL ?: 0)
        result = 31 * result + (hslValue?.hashCode() ?: 0)
        result = 31 * result + (hsvFractionH?.hashCode() ?: 0)
        result = 31 * result + (hsvFractionS?.hashCode() ?: 0)
        result = 31 * result + (hsvFractionV?.hashCode() ?: 0)
        result = 31 * result + (hsvH ?: 0)
        result = 31 * result + (hsvS ?: 0)
        result = 31 * result + (hsvV ?: 0)
        result = 31 * result + (hsvValue?.hashCode() ?: 0)
        result = 31 * result + (xyzFractionX?.hashCode() ?: 0)
        result = 31 * result + (xyzFractionY?.hashCode() ?: 0)
        result = 31 * result + (xyzFractionZ?.hashCode() ?: 0)
        result = 31 * result + (xyzX ?: 0)
        result = 31 * result + (xyzY ?: 0)
        result = 31 * result + (xyzZ ?: 0)
        result = 31 * result + (xyzValue?.hashCode() ?: 0)
        result = 31 * result + (cmykFractionC?.hashCode() ?: 0)
        result = 31 * result + (cmykFractionM?.hashCode() ?: 0)
        result = 31 * result + (cmykFractionY?.hashCode() ?: 0)
        result = 31 * result + (cmykFractionK?.hashCode() ?: 0)
        result = 31 * result + (cmykC ?: 0)
        result = 31 * result + (cmykM ?: 0)
        result = 31 * result + (cmykY ?: 0)
        result = 31 * result + (cmykK ?: 0)
        result = 31 * result + (cmykValue?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (exactNameHex?.hashCode() ?: 0)
        result = 31 * result + (exactNameHexSigned?.hashCode() ?: 0)
        result = 31 * result + (isNameMatchExact?.hashCode() ?: 0)
        result = 31 * result + (exactNameHexDistance ?: 0)
        result = 31 * result + (imageBareUrl?.hashCode() ?: 0)
        result = 31 * result + (imageNamedUrl?.hashCode() ?: 0)
        result = 31 * result + (contrastHex?.hashCode() ?: 0)
        return result
    }
}