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
class ColorToAbstract(
    val color: Color,
    val expectedAbstract: Color.Abstract,
) : ColorConverterTest() {

    @Test
    fun `color to abstract conversion`() {
        val abstract = with(sut) { color.toAbstract() }

        withClue("Color $color should be $expectedAbstract") {
            abstract shouldBe expectedAbstract
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Hex(0x0) convertsTo Color.Abstract(0x0),
            /* #1  */ Color.Hex(0x127) convertsTo Color.Abstract(0x127),
            /* #2  */ Color.Hex(0x1AC) convertsTo Color.Abstract(0x1AC),
            /* #3  */ Color.Hex(0xF0F8FF) convertsTo Color.Abstract(0xF0F8FF),

            /* #4  */ Color.Rgb(0, 0, 0) convertsTo Color.Abstract(0x0),
            /* #5  */ Color.Rgb(8, 16, 32) convertsTo Color.Abstract(0x081020),
            /* #6  */ Color.Rgb(0, 50, 177) convertsTo Color.Abstract(0x032B1),
            /* #7  */ Color.Rgb(97, 0, 200) convertsTo Color.Abstract(0x6100C8),
            /* #8  */ Color.Rgb(105, 11, 0) convertsTo Color.Abstract(0x690B00),
            /* #9  */ Color.Rgb(240, 248, 255) convertsTo Color.Abstract(0xF0F8FF),
            /* #10 */ Color.Rgb(255, 255, 255) convertsTo Color.Abstract(0xFFFFFF),
        )
    }
}

@RunWith(Parameterized::class)
class AbstractToHex(
    val abstract: Color.Abstract,
    val expectedHex: Color.Hex,
) : ColorConverterTest() {

    @Test
    fun `abstract to hex conversion`() {
        val hex = with(sut) { abstract.toHex() }

        withClue("Abstract color $abstract should be $expectedHex") {
            hex shouldBe expectedHex
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Abstract(0x0) convertsTo Color.Hex(0x0),
            /* #1  */ Color.Abstract(0x127) convertsTo Color.Hex(0x127),
            /* #2  */ Color.Abstract(0x1AC) convertsTo Color.Hex(0x1AC),
            /* #3  */ Color.Abstract(0xF0F8FF) convertsTo Color.Hex(0xF0F8FF),
        )
    }
}

@RunWith(Parameterized::class)
class AbstractToRgb(
    val abstract: Color.Abstract,
    val expectedRgb: Color.Rgb,
) : ColorConverterTest() {

    @Test
    fun `abstract to rgb conversion`() {
        val hex = with(sut) { abstract.toRgb() }

        withClue("Abstract color $abstract should be $expectedRgb") {
            hex shouldBe expectedRgb
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Abstract(0x0) convertsTo Color.Rgb(0, 0, 0),
            /* #1  */ Color.Abstract(0x081020) convertsTo Color.Rgb(8, 16, 32),
            /* #2  */ Color.Abstract(0x032B1) convertsTo Color.Rgb(0, 50, 177),
            /* #3  */ Color.Abstract(0x6100C8) convertsTo Color.Rgb(97, 0, 200),
            /* #4  */ Color.Abstract(0x690B00) convertsTo Color.Rgb(105, 11, 0),
            /* #5  */ Color.Abstract(0xF0F8FF) convertsTo Color.Rgb(240, 248, 255),
            /* #6  */ Color.Abstract(0xFFFFFF) convertsTo Color.Rgb(255, 255, 255),
        )
    }
}