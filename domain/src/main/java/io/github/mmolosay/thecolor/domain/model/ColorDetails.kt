package io.github.mmolosay.thecolor.domain.model

/**
 * A [color] coupled with various data regarding it.
 */
data class ColorDetails(
    val color: Color,
    val colorHexString: ColorHexString,
    val colorTranslations: ColorTranslations,
    val colorName: String,
    val exact: Exact,
    val matchesExact: Boolean,
    val distanceFromExact: Int,
) {

    data class ColorHexString(
        val withNumberSign: String,
        val withoutNumberSign: String,
    )

    data class ColorTranslations(
        val rgb: ColorTranslation.Rgb,
        val hsl: ColorTranslation.Hsl,
        val hsv: ColorTranslation.Hsv,
        val xyz: ColorTranslation.Xyz,
        val cmyk: ColorTranslation.Cmyk,
    )

    data class Exact(
        val color: Color,
        val hexStringWithNumberSign: String,
    )

    object ColorTranslation {

        data class Rgb(
            val standard: StandardValues,
            val normalized: NormalizedValues,
        ) {

            data class StandardValues(
                val r: Int,
                val g: Int,
                val b: Int,
            )

            data class NormalizedValues(
                val r: Float,
                val g: Float,
                val b: Float,
            )
        }

        data class Hsl(
            val standard: StandardValues,
            val normalized: NormalizedValues,
        ) {

            data class StandardValues(
                val h: Int,
                val s: Int,
                val l: Int,
            )

            data class NormalizedValues(
                val h: Float,
                val s: Float,
                val l: Float,
            )
        }

        data class Hsv(
            val standard: StandardValues,
            val normalized: NormalizedValues,
        ) {

            data class StandardValues(
                val h: Int,
                val s: Int,
                val v: Int,
            )

            data class NormalizedValues(
                val h: Float,
                val s: Float,
                val v: Float,
            )
        }

        data class Xyz(
            val standard: StandardValues,
            val normalized: NormalizedValues,
        ) {

            data class StandardValues(
                val x: Int,
                val y: Int,
                val z: Int,
            )

            data class NormalizedValues(
                val x: Float,
                val y: Float,
                val z: Float,
            )
        }

        data class Cmyk(
            val standard: StandardValues,
            val normalized: NormalizedValues,
        ) {
            data class StandardValues(
                val c: Int,
                val m: Int,
                val y: Int,
                val k: Int,
            )

            data class NormalizedValues(
                val c: Float,
                val m: Float,
                val y: Float,
                val k: Float,
            )

        }
    }
}