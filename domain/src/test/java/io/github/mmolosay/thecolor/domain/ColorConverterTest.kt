package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ColorConverterTest {

    val sut = ColorConverter()

    @ParameterizedTest
    @MethodSource("hexToRgbData")
    fun `HEX color is converted to expected RGB color`(
        given: Color.Hex,
        expected: Color.Rgb,
    ) {
        val rgb = with(sut) { given.toRgb() }

        withClue("HEX color $given should be RGB color $expected, but was $rgb") {
            rgb shouldBe expected
        }
    }

    @ParameterizedTest
    @MethodSource("rgbToHexData")
    fun `RGB color is converted to expected HEX color`(
        given: Color.Rgb,
        expected: Color.Hex,
    ) {
        val hex = with(sut) { given.toHex() }

        withClue("RGB color $given should be HEX color $expected, but was $hex") {
            hex shouldBe expected
        }
    }

    @ParameterizedTest
    @MethodSource("hexToHsvData")
    fun `HEX color is converted to expected HSV color`(
        given: Color.Hex,
        expected: Color.Hsv,
    ) {
        val hsv = with(sut) { given.toHsv() }

        withClue("HEX color $given should be HSV color $expected, but was $hsv") {
            hsv shouldBe expected
        }
    }

    @ParameterizedTest
    @MethodSource("hsvToHexData")
    fun `HSV color is converted to expected HEX color`(
        given: Color.Hsv,
        expected: Color.Hex,
    ) {
        val hex = with(sut) { given.toHex() }

        withClue("Hsv color $given should be HEX color $expected, but was $hex") {
            hex shouldBe expected
        }
    }

    companion object {

        @JvmStatic
        fun hexToRgbData() = listOf(
            /* #0  */ Color.Hex(0x000000) convertsTo Color.Rgb(0, 0, 0),
            /* #1  */ Color.Hex(0x081020) convertsTo Color.Rgb(8, 16, 32),
            /* #2  */ Color.Hex(0x0032B1) convertsTo Color.Rgb(0, 50, 177),
            /* #3  */ Color.Hex(0x6100C8) convertsTo Color.Rgb(97, 0, 200),
            /* #4  */ Color.Hex(0x690B00) convertsTo Color.Rgb(105, 11, 0),
            /* #5  */ Color.Hex(0xF0F8FF) convertsTo Color.Rgb(240, 248, 255),
            /* #6  */ Color.Hex(0xFFFFFF) convertsTo Color.Rgb(255, 255, 255),
        )

        @JvmStatic
        fun rgbToHexData() = listOf(
            /* #0  */ Color.Rgb(0, 0, 0) convertsTo Color.Hex(0x000000),
            /* #1  */ Color.Rgb(8, 16, 32) convertsTo Color.Hex(0x081020),
            /* #2  */ Color.Rgb(0, 50, 177) convertsTo Color.Hex(0x0032B1),
            /* #3  */ Color.Rgb(97, 0, 200) convertsTo Color.Hex(0x6100C8),
            /* #4  */ Color.Rgb(105, 11, 0) convertsTo Color.Hex(0x690B00),
            /* #5  */ Color.Rgb(240, 248, 255) convertsTo Color.Hex(0xF0F8FF),
            /* #6  */ Color.Rgb(255, 255, 255) convertsTo Color.Hex(0xFFFFFF),
        )

        @JvmStatic
        fun hexToHsvData() = listOf(
            /* #0  */ Color.Hex(0x000000) convertsTo Color.Hsv(0f, 0f, 0f),
            /* #1  */ Color.Hex(0x081020) convertsTo Color.Hsv(220f, 0.75f, 0.1254f),
            /* #2  */ Color.Hex(0x0032B1) convertsTo Color.Hsv(223.05f, 1f, 0.6941f),
            /* #3  */ Color.Hex(0x6100C8) convertsTo Color.Hsv(269.1f, 1f, 0.7843f),
            /* #4  */ Color.Hex(0x690B00) convertsTo Color.Hsv(6.28f, 1f, 0.4117f),
            /* #5  */ Color.Hex(0xF0F8FF) convertsTo Color.Hsv(208f, 0.0588f, 1f),
            /* #6  */ Color.Hex(0xFFFFFF) convertsTo Color.Hsv(0f, 0f, 1f),
            /* #7  */ Color.Hex(0x5EBE4A) convertsTo Color.Hsv(109.65f, 0.6105f, 0.7450f),
        )

        @JvmStatic
        fun hsvToHexData() = listOf(
            /* #0  */ Color.Hsv(0f, 0f, 0f) convertsTo Color.Hex(0x000000),
            /* #1  */ Color.Hsv(220f, 0.75f, 0.1254f) convertsTo Color.Hex(0x081020),
            /* #2  */ Color.Hsv(223.05f, 1f, 0.6941f) convertsTo Color.Hex(0x0032B1),
            /* #3  */ Color.Hsv(269.1f, 1f, 0.7843f) convertsTo Color.Hex(0x6100C8),
            /* #4  */ Color.Hsv(6.28f, 1f, 0.4117f) convertsTo Color.Hex(0x690B00),
            /* #5  */ Color.Hsv(208f, 0.0588f, 1f) convertsTo Color.Hex(0xF0F8FF),
            /* #6  */ Color.Hsv(0f, 0f, 1f) convertsTo Color.Hex(0xFFFFFF),
            /* #7  */ Color.Hsv(109.65f, 0.6105f, 0.7450f) convertsTo Color.Hex(0x5EBE4A),
        )

        infix fun <ColorSource, ColorDest> ColorSource.convertsTo(expected: ColorDest) =
            arrayOf(this, expected)
    }
}