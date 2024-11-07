package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeValidator
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

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

    fun createSut() =
        ColorFactory(
            colorPrototypeValidator = colorPrototypeValidator,
        ).also {
            sut = it
        }
}