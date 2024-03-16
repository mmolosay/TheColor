@file:Suppress("unused")

package io.github.mmolosay.thecolor.data.remote.model

import com.squareup.moshi.Json

data class ColorDetailsDto(
    @field:Json(name = "hex") val hex: Hex,
    @field:Json(name = "rgb") val rgb: Rgb,
    @field:Json(name = "hsl") val hsl: Hsl,
    @field:Json(name = "hsv") val hsv: Hsv,
    @field:Json(name = "XYZ") val xyz: Xyz,
    @field:Json(name = "cmyk") val cmyk: Cmyk,
    @field:Json(name = "name") val name: Name,
    @field:Json(name = "image") val image: Image,
    @field:Json(name = "contrast") val contrast: Contrast,
) {

    data class Hex(
        @field:Json(name = "value") val valueWithNumberSign: String,
        @field:Json(name = "clean") val valueWithoutNumberSign: String,
    )

    data class Rgb(
        @field:Json(name = "fraction") val normalized: NormalizedValues,
        @field:Json(name = "r") val r: Int,
        @field:Json(name = "g") val g: Int,
        @field:Json(name = "b") val b: Int,
        @field:Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @field:Json(name = "r") val r: Float,
            @field:Json(name = "g") val g: Float,
            @field:Json(name = "b") val b: Float,
        )
    }

    data class Hsl(
        @field:Json(name = "fraction") val normalized: NormalizedValues,
        @field:Json(name = "h") val h: Int,
        @field:Json(name = "s") val s: Int,
        @field:Json(name = "l") val l: Int,
        @field:Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @field:Json(name = "h") val h: Float,
            @field:Json(name = "s") val s: Float,
            @field:Json(name = "l") val l: Float,
        )
    }

    data class Hsv(
        @field:Json(name = "fraction") val normalized: NormalizedValues,
        @field:Json(name = "h") val h: Int,
        @field:Json(name = "s") val s: Int,
        @field:Json(name = "v") val v: Int,
        @field:Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @field:Json(name = "h") val h: Float,
            @field:Json(name = "s") val s: Float,
            @field:Json(name = "v") val v: Float,
        )
    }

    data class Xyz(
        @field:Json(name = "fraction") val normalized: NormalizedValues,
        @field:Json(name = "X") val x: Int,
        @field:Json(name = "Y") val y: Int,
        @field:Json(name = "Z") val z: Int,
        @field:Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @field:Json(name = "X") val x: Float,
            @field:Json(name = "Y") val y: Float,
            @field:Json(name = "Z") val z: Float,
        )
    }

    data class Cmyk(
        @field:Json(name = "fraction") val normalized: NormalizedValues,
        @field:Json(name = "c") val c: Int?,
        @field:Json(name = "m") val m: Int?,
        @field:Json(name = "y") val y: Int?,
        @field:Json(name = "k") val k: Int?,
        @field:Json(name = "value") val cssFormula: String,
    ) {

        data class NormalizedValues(
            @field:Json(name = "c") val c: Float?,
            @field:Json(name = "m") val m: Float?,
            @field:Json(name = "y") val y: Float?,
            @field:Json(name = "k") val k: Float?,
        )
    }

    data class Name(
        @field:Json(name = "value") val colorName: String,
        @field:Json(name = "closest_named_hex") val hexValueWithNumberSignOfExactColor: String,
        @field:Json(name = "exact_match_name") val exactMatch: Boolean,
        @field:Json(name = "distance") val distanceFromExact: Int,
    )

    data class Image(
        @field:Json(name = "bare") val bareUrl: String,
        @field:Json(name = "named") val namedUrl: String,
    )

    data class Contrast(
        @field:Json(name = "") val hexValueWithNumberSignOfContrastColor: String,
    )
}