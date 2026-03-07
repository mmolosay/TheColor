package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototype
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ColorFactoryTest {

    val colorPrototypeValidator: ColorPrototypeValidator = mockk()

    lateinit var sut: ColorFactory

    @Test
    fun `invalid hex prototype creates null`() {
        val prototype = ColorPrototype.Hex(0xf0f8ff)
        every { with(colorPrototypeValidator) { prototype.isValid() } } returns false
        createSut()

        val color = sut.from(prototype)

        color shouldBe null
    }

    @Test
    fun `valid hex prototype creates color`() {
        val prototype = ColorPrototype.Hex(0xf0f8ff)
        every { with(colorPrototypeValidator) { prototype.isValid() } } returns true
        createSut()

        val color = sut.from(prototype)

        color shouldBe Color.Hex(0xf0f8ff)
    }

    @Test
    fun `invalid rgb prototype creates null`() {
        val prototype = ColorPrototype.Rgb(0, 191, 255)
        every { with(colorPrototypeValidator) { prototype.isValid() } } returns false
        createSut()

        val color = sut.from(prototype)

        color shouldBe null
    }

    @Test
    fun `valid rgb prototype creates color`() {
        val prototype = ColorPrototype.Rgb(0, 191, 255)
        every { with(colorPrototypeValidator) { prototype.isValid() } } returns true
        createSut()

        val color = sut.from(prototype)

        color shouldBe Color.Rgb(0, 191, 255)
    }

    @Test
    fun `random color is a valid color`() {
        createSut()

        val color = sut.random()
        val intValue = color.shouldBeInstanceOf<Color.Hex>().value

        intValue shouldBeInRange 0..0xFFFFFF
    }

    fun createSut() =
        ColorFactory(
            colorPrototypeValidator = colorPrototypeValidator,
        ).also {
            sut = it
        }
}