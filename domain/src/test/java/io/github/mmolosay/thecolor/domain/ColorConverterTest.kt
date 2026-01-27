package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ColorConverterTest {

    val sut = ColorConverter()

    @ParameterizedTest
    @MethodSource("rgbToHexData")
    fun `RGB color is converted to expected HEX color`(
        given: Color.Rgb,
        expected: Color.Hex,
    ) {
        val hex = with(sut) { given.toHex() }

        withClue("RGB color $given should be HEX color $expected") {
            hex shouldBe expected
        }
    }

    @ParameterizedTest
    @MethodSource("hexToRgbData")
    fun `HEX color is converted to expected RGB color`(
        given: Color.Hex,
        expected: Color.Rgb,
    ) {
        val rgb = with(sut) { given.toRgb() }

        withClue("HEX color $given should be RGB color $expected") {
            rgb shouldBe expected
        }
    }

    companion object {

        @JvmStatic
        fun rgbToHexData() = listOf(
            /* #0  */ Color.Rgb(0, 0, 0) convertsTo Color.Hex(0x000000),
            /* #1  */ Color.Rgb(8, 16, 32) convertsTo Color.Hex(0x081020),
            /* #2  */ Color.Rgb(0, 50, 177) convertsTo Color.Hex(0x032B1),
            /* #3  */ Color.Rgb(97, 0, 200) convertsTo Color.Hex(0x6100C8),
            /* #4  */ Color.Rgb(105, 11, 0) convertsTo Color.Hex(0x690B00),
            /* #5  */ Color.Rgb(240, 248, 255) convertsTo Color.Hex(0xF0F8FF),
            /* #6  */ Color.Rgb(255, 255, 255) convertsTo Color.Hex(0xFFFFFF),
        )

        @JvmStatic
        fun hexToRgbData() = listOf(
            /* #0  */ Color.Hex(0x000000) convertsTo Color.Rgb(0, 0, 0),
            /* #1  */ Color.Hex(0x081020) convertsTo Color.Rgb(8, 16, 32),
            /* #2  */ Color.Hex(0x032B1) convertsTo Color.Rgb(0, 50, 177),
            /* #3  */ Color.Hex(0x6100C8) convertsTo Color.Rgb(97, 0, 200),
            /* #4  */ Color.Hex(0x690B00) convertsTo Color.Rgb(105, 11, 0),
            /* #5  */ Color.Hex(0xF0F8FF) convertsTo Color.Rgb(240, 248, 255),
            /* #6  */ Color.Hex(0xFFFFFF) convertsTo Color.Rgb(255, 255, 255),
        )

        infix fun <ColorSource, ColorDest> ColorSource.convertsTo(expected: ColorDest) =
            arrayOf(this, expected)
    }
}