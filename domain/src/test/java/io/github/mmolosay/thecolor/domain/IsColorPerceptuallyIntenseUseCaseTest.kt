package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorMath
import io.github.mmolosay.thecolor.domain.usecase.IsColorPerceptuallyIntenseUseCase
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IsColorPerceptuallyIntenseUseCaseTest {

    val colorMath: ColorMath = mockk {
        every { Color.Hex(0x000000).toLab() } returns ColorMath.Lab(0f, 0f, 0f)
        every { Color.Hex(0xF9031B).toLab() } returns ColorMath.Lab(52.16f, 78.69f, 57.91f)
        every { Color.Hex(0x4A34FF).toLab() } returns ColorMath.Lab(39.8f, 66.93f, -95.29f)
        every { Color.Hex(0xFFFFFF).toLab() } returns ColorMath.Lab(100f, 0f, 0f)
    }
    val sut = IsColorPerceptuallyIntenseUseCase(
        colorMath = colorMath,
        colorConverter = ColorConverter(),
    )

    @ParameterizedTest
    @MethodSource("testCases")
    fun `color is or isn't perceptually intense as expected`(
        color: Color,
        expectedIsPerceptuallyIntense: Boolean,
    ) {
        val isPerceptuallyIntense = with(sut) { color.isPerceptuallyIntense() }

        withClue({
            val negationParticle = if (!expectedIsPerceptuallyIntense) "not" else ""
            "Color $color should $negationParticle be perceptually intense"
        }) {
            isPerceptuallyIntense shouldBe expectedIsPerceptuallyIntense
        }
    }

    companion object {

        @JvmStatic
        fun testCases() = listOf(
            /* #0  */ Color.Hex(0x000000) isPerceptuallyIntense false,
            /* #1  */ Color.Hex(0xF9031B) isPerceptuallyIntense true,
            /* #2  */ Color.Hex(0x4A34FF) isPerceptuallyIntense true,
            /* #3  */ Color.Hex(0xFFFFFF) isPerceptuallyIntense true,
        )

        infix fun Color.isPerceptuallyIntense(expected: Boolean): Array<Any> =
            arrayOf(this, expected)
    }
}