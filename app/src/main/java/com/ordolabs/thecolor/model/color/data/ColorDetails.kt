package com.ordolabs.thecolor.model.color.data

import android.os.Parcelable
import com.ordolabs.thecolor.model.color.Color
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorDetails(
    val color: Color?,
    val spaces: Spaces,
    val exact: Exact
) : Parcelable {

    // TODO: implement hasAllData(): Boolean method for all spaces data classes

    @Parcelize
    data class Spaces(
        val hex: Hex,
        val rgb: Rgb,
        val hsl: Hsl,
        val hsv: Hsv,
        val cmyk: Cmyk
    ) : Parcelable {

        @Parcelize
        data class Hex(
            val signed: String?,
            val signless: String?
        ) : Parcelable

        @Parcelize
        data class Rgb(
            val r: Int?,
            val g: Int?,
            val b: Int?
        ): Parcelable

        @Parcelize
        data class Hsl(
            val h: Int?,
            val s: Int?,
            val l: Int?
        ): Parcelable

        @Parcelize
        data class Hsv(
            val h: Int?,
            val s: Int?,
            val v: Int?
        ): Parcelable

        @Parcelize
        data class Cmyk(
            val c: Int?,
            val m: Int?,
            val y: Int?,
            val k: Int?
        ): Parcelable
    }

    @Parcelize
    data class Exact(
        val name: String?,
        val color: Color?,
        val distance: Int?,
        val isMatch: Boolean?
    ) : Parcelable
}