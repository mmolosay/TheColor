package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeValidator
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ColorPrototypeValidatorTest(
    val prototype: ColorPrototype,
    val expectedValid: Boolean,
) {

    val sut = ColorPrototypeValidator()

    @Test
    fun `color prototype should be valid or not valid as expected`() {
        val result = with(sut) { prototype.isValid() }

        withClue("Prototype $prototype should be valid=$expectedValid") {
            result shouldBe expectedValid
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ ColorPrototype.Hex(null) shouldBeValid false,
            /* #1  */ ColorPrototype.Hex(-0x1) shouldBeValid false,
            /* #2  */ ColorPrototype.Hex(-0xF0F8FF) shouldBeValid false,
            /* #3  */ ColorPrototype.Hex(0x0) shouldBeValid true,
            /* #4  */ ColorPrototype.Hex(0xA) shouldBeValid true,
            /* #5  */ ColorPrototype.Hex(0x1AC) shouldBeValid true,
            /* #6  */ ColorPrototype.Hex(0xB23F0) shouldBeValid true,
            /* #7  */ ColorPrototype.Hex(0xAD8027) shouldBeValid true,
            /* #8  */ ColorPrototype.Hex(0xFFFFFF) shouldBeValid true,
            /* #9  */ ColorPrototype.Hex(0x1000000) shouldBeValid false,
            /* #10 */ ColorPrototype.Hex(0x963FF20) shouldBeValid false,

            /* #11 */ ColorPrototype.Rgb(null, 1, 2) shouldBeValid false,
            /* #12 */ ColorPrototype.Rgb(0, null, 2) shouldBeValid false,
            /* #13 */ ColorPrototype.Rgb(0, 1, null) shouldBeValid false,
            /* #14 */ ColorPrototype.Rgb(null, null, null) shouldBeValid false,
            /* #15 */ ColorPrototype.Rgb(-1, -2, -3) shouldBeValid false,
            /* #16 */ ColorPrototype.Rgb(-1, -2, 3) shouldBeValid false,
            /* #17 */ ColorPrototype.Rgb(-1, 2, 3) shouldBeValid false,
            /* #18 */ ColorPrototype.Rgb(0, 0, 0) shouldBeValid true,
            /* #19 */ ColorPrototype.Rgb(1, 2, 3) shouldBeValid true,
            /* #20 */ ColorPrototype.Rgb(0, 191, 255) shouldBeValid true,
            /* #21 */ ColorPrototype.Rgb(0, 0, 256) shouldBeValid false,
        )

        infix fun ColorPrototype.shouldBeValid(expectedValid: Boolean): Array<Any> =
            arrayOf(this, expectedValid)
    }
}