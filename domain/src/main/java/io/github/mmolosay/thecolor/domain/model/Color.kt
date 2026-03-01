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
            require(value in Range)
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            value
                .toString(16)
                .padStart(6, '0')
                .uppercase()
                .let { "#$it" }

        companion object {
            val Range = 0..0xFFFFFF
        }
    }

    data class Rgb(
        val r: Int,
        val g: Int,
        val b: Int,
    ) : Color {

        init {
            require(listOf(r, g, b).all { it in ComponentRange })
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            "r=$r,g=$g,b=$b"

        companion object {
            val ComponentRange = 0..255
        }
    }

    data class Hsv(
        val hue: Float,
        val saturation: Float,
        val value: Float,
    ) : Color {

        init {
            require(hue in HueRange)
            require(saturation in SaturationRange)
            require(value in ValueRange)
        }

        // for a convenient presentation in debugger
        override fun toString(): String =
            "h=$hue,s=$saturation,v=$value"

        companion object {
            val HueRange = 0f..<360f
            val SaturationRange = 0f..1f
            val ValueRange = 0f..1f
        }
    }
}