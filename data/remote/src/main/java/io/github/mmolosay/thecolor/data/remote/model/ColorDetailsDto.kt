@file:Suppress("unused")

package io.github.mmolosay.thecolor.data.remote.model

import com.squareup.moshi.Json

data class ColorDetailsDto(
    @Json(name = "hex") val hex: Hex,
    @Json(name = "rgb") val rgb: Rgb,
    @Json(name = "hsl") val hsl: Hsl,
    @Json(name = "hsv") val hsv: Hsv,
    @Json(name = "XYZ") val xyz: Xyz,
    @Json(name = "cmyk") val cmyk: Cmyk,
    @Json(name = "name") val name: Name,
    @Json(name = "image") val image: Image,
    @Json(name = "contrast") val contrast: Contrast,
) {

    data class Hex(
        @Json(name = "value") val valueWithNumberSign: String,
        @Json(name = "clean") val valueWithoutNumberSign: String,
    )

    data class Rgb(
        @Json(name = "fraction") val normalized: NormalizedValues,
        @Json(name = "r") val r: Int,
        @Json(name = "g") val g: Int,
        @Json(name = "b") val b: Int,
        @Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @Json(name = "r") val r: Float,
            @Json(name = "g") val g: Float,
            @Json(name = "b") val b: Float,
        )
    }

    data class Hsl(
        @Json(name = "fraction") val normalized: NormalizedValues,
        @Json(name = "h") val h: Int,
        @Json(name = "s") val s: Int,
        @Json(name = "l") val l: Int,
        @Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @Json(name = "h") val h: Float,
            @Json(name = "s") val s: Float,
            @Json(name = "l") val l: Float,
        )
    }

    data class Hsv(
        @Json(name = "fraction") val normalized: NormalizedValues,
        @Json(name = "h") val h: Int,
        @Json(name = "s") val s: Int,
        @Json(name = "v") val v: Int,
        @Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @Json(name = "h") val h: Float,
            @Json(name = "s") val s: Float,
            @Json(name = "v") val v: Float,
        )
    }

    data class Xyz(
        @Json(name = "fraction") val normalized: NormalizedValues,
        @Json(name = "X") val x: Int,
        @Json(name = "Y") val y: Int,
        @Json(name = "Z") val z: Int,
        @Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @Json(name = "X") val x: Float,
            @Json(name = "Y") val y: Float,
            @Json(name = "Z") val z: Float,
        )
    }

    data class Cmyk(
        @Json(name = "fraction") val normalized: NormalizedValues,
        @Json(name = "c") val c: Int?,
        @Json(name = "m") val m: Int?,
        @Json(name = "y") val y: Int?,
        @Json(name = "k") val k: Int?,
        @Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @Json(name = "c") val c: Float?,
            @Json(name = "m") val m: Float?,
            @Json(name = "y") val y: Float?,
            @Json(name = "k") val k: Float?,
        )
    }

    data class Name(
        @Json(name = "value") val colorName: String,
        @Json(name = "closest_named_hex") val hexValueWithNumberSignOfExactColor: String,
        @Json(name = "exact_match_name") val exactMatch: Boolean,
        @Json(name = "distance") val distanceFromExact: Int,
    )

    data class Image(
        @Json(name = "bare") val bareUrl: String,
        @Json(name = "named") val namedUrl: String,
    )

    data class Contrast(
        @Json(name = "value") val hexValueWithNumberSignOfContrastColor: String,
    )
}