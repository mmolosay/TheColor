package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.kotest.assertions.withClue
import io.kotest.matchers.floats.shouldBeLessThan
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.math.abs

class GetColorLightnessUseCaseImplTest {

    val colorConverter: ColorConverter = ColorConverter()
    val colorMapper: ColorMapper = ColorMapper()

    val sut = GetColorLightnessUseCaseImpl(
        colorConverter = colorConverter,
        colorMapper = colorMapper,
    )

    @ParameterizedTest
    @MethodSource("labData")
    fun `color lightness in LAB is as expected`(
        color: Color,
        expectedLightness: Float,
    ) {
        val lightness = with(sut) { color.labLightness() }

        lightness shouldBeInEqualityRangeWith expectedLightness
    }

    infix fun Float.shouldBeInEqualityRangeWith(other: Float) {
        val delta = abs(this - other)
        val threshold = 0.0001f
        withClue("$this should be in equality range with $other") {
            delta shouldBeLessThan threshold
        }
    }

    companion object {

        @JvmStatic
        fun labData() = listOf(
            /* #0  */ Color.Hex(0xFFFFFF) shouldHaveLightness 1.0000f,
            /* #1  */ Color.Hex(0x000000) shouldHaveLightness 0.0000f,
            /* #2  */ Color.Hex(0xF0F8FF) shouldHaveLightness 0.9718f,
            /* #3  */ Color.Hex(0x123456) shouldHaveLightness 0.2104f,
            /* #4  */ Color.Hex(0x1A803F) shouldHaveLightness 0.4700f,
            /* #5  */ Color.Hex(0xF54021) shouldHaveLightness 0.5527f,
            /* #6  */ Color.Hex(0xF3A505) shouldHaveLightness 0.7353f,
            /* #7  */ Color.Hex(0xC0E904) shouldHaveLightness 0.8675f,
        )

        infix fun Color.shouldHaveLightness(expectedLightness: Float): Array<Any> =
            arrayOf(this, expectedLightness)
    }
}