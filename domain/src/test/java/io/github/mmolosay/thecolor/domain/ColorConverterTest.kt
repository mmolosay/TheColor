package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

abstract class ColorConverterTest {

    val sut = ColorConverter()

    companion object {
        infix fun <ColorSource, ColorDest> ColorSource.convertsTo(expected: ColorDest) =
            arrayOf(this, expected)
    }
}

@RunWith(Parameterized::class)
class RgbToHex(
    val given: Color.Rgb,
    val expected: Color.Hex,
) : ColorConverterTest() {

    @Test
    fun `RGB color is converted to expected HEX color`() {
        val hex = with(sut) { given.toHex() }

        withClue("RGB color $given should be HEX color $expected") {
            hex shouldBe expected
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Rgb(0, 0, 0) convertsTo Color.Hex(0x000000),
            /* #1  */ Color.Rgb(8, 16, 32) convertsTo Color.Hex(0x081020),
            /* #2  */ Color.Rgb(0, 50, 177) convertsTo Color.Hex(0x032B1),
            /* #3  */ Color.Rgb(97, 0, 200) convertsTo Color.Hex(0x6100C8),
            /* #4  */ Color.Rgb(105, 11, 0) convertsTo Color.Hex(0x690B00),
            /* #5  */ Color.Rgb(240, 248, 255) convertsTo Color.Hex(0xF0F8FF),
            /* #6  */ Color.Rgb(255, 255, 255) convertsTo Color.Hex(0xFFFFFF),
        )
    }
}

@RunWith(Parameterized::class)
class HexToRgb(
    val given: Color.Hex,
    val expected: Color.Rgb,
) : ColorConverterTest() {

    @Test
    fun `HEX color is converted to expected RGB color`() {
        val hex = with(sut) { given.toRgb() }

        withClue("HEX color $given should be RGB color $expected") {
            hex shouldBe expected
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Hex(0x000000) convertsTo Color.Rgb(0, 0, 0),
            /* #1  */ Color.Hex(0x081020) convertsTo Color.Rgb(8, 16, 32),
            /* #2  */ Color.Hex(0x032B1) convertsTo Color.Rgb(0, 50, 177),
            /* #3  */ Color.Hex(0x6100C8) convertsTo Color.Rgb(97, 0, 200),
            /* #4  */ Color.Hex(0x690B00) convertsTo Color.Rgb(105, 11, 0),
            /* #5  */ Color.Hex(0xF0F8FF) convertsTo Color.Rgb(240, 248, 255),
            /* #6  */ Color.Hex(0xFFFFFF) convertsTo Color.Rgb(255, 255, 255),
        )
    }
}