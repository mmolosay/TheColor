package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.thecolor.presentation.input.api.ColorInput
import io.kotest.matchers.shouldBe
import org.junit.Test

class ColorInputMapperTest {

    val sut = ColorInputMapper()

    @Test
    fun `invalid hex color input is converted to prototype with null`() {
        val colorInput = ColorInput.Hex("gibberish")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype.value shouldBe null
    }

    @Test
    fun `hex color input 1F is converted to prototype 0x1F`() {
        val colorInput = ColorInput.Hex("1F")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype.value shouldBe 0x1F
    }

    @Test
    fun `hex color input 1AC is converted to prototype 0x11AACC`() {
        val colorInput = ColorInput.Hex("1AC")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype.value shouldBe 0x11AACC
    }

    @Test
    fun `hex color input F0F8FF is converted to prototype 0xF0F8FF`() {
        val colorInput = ColorInput.Hex("F0F8FF")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype.value shouldBe 0xF0F8FF
    }

    @Test
    fun `invalid rgb color input is converted to prototype with null`() {
        val colorInput = ColorInput.Rgb(r = "0", g = "191", b = "gibberish")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype.b shouldBe null
    }

    @Test
    fun `rgb color input 0 191 255 is converted to prototype 0 191 255`() {
        val colorInput = ColorInput.Rgb(r = "0", g = "191", b = "255")

        val prototype = with(sut) { colorInput.toPrototype() }

        prototype shouldBe ColorPrototype.Rgb(0, 191, 255)
    }

    @Test
    fun `hex color 0x0 is converted to color input 000000`() {
        val color = Color.Hex(0x0)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Hex("000000")
    }

    @Test
    fun `hex color 0xB is converted to color input 00000B`() {
        val color = Color.Hex(0xB)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Hex("00000B")
    }

    @Test
    fun `hex color 0x1AC is converted to color input 0001AC`() {
        val color = Color.Hex(0x1AC)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Hex("0001AC")
    }

    @Test
    fun `hex color 0xF0F8FF is converted to color input F0F8FF`() {
        val color = Color.Hex(0xF0F8FF)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Hex("F0F8FF")
    }

    @Test
    fun `rgb color 0 0 0 is converted to color input 0 0 0`() {
        val color = Color.Rgb(0, 0, 0)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Rgb("0", "0", "0")
    }

    @Test
    fun `rgb color 0 191 255 is converted to color input 0 191 255`() {
        val color = Color.Rgb(0, 191, 255)

        val colorInput = with(sut) { color.toColorInput() }

        colorInput shouldBe ColorInput.Rgb("0", "191", "255")
    }
}