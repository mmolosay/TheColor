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

        // for a convenient presentation in debugger
        override fun toString(): String =
            "r=$r,g=$g,b=$b"
    }
}