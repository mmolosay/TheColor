package io.github.mmolosay.thecolor.domain.model

/**
 * Represents a valid, opaque color.
 */
sealed interface Color {

    /**
     * Solid color in RRGGBB format without alpha channel.
     * Example: `value = 0x1A803F`.
     */
    data class Hex(
        val value: Int,
    ) : Color {

        init {
            require(value in ColorConstants.HexColorIntRange)
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            value
                .toString(16)
                .padStart(6, '0')
                .uppercase()
                .let { "#$it" }
    }

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    ) : Color {

        init {
            require(r in ColorConstants.RgbColorComponentIntRange)
            require(g in ColorConstants.RgbColorComponentIntRange)
            require(b in ColorConstants.RgbColorComponentIntRange)
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            "r=$r,g=$g,b=$b"
    }

    data class Hsv(
        val hue: Float,
        val saturation: Float,
        val value: Float,
    ) : Color {

        init {
            require(hue in ColorConstants.HsvHueRange)
            require(saturation in ColorConstants.HsvSaturationRange)
            require(value in ColorConstants.HsvValueRange)
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            "h=$hue,s=$saturation,v=$value"
    }
}