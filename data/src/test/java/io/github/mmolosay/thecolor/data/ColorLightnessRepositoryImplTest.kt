package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.kotest.matchers.floats.shouldBeLessThan
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.math.abs

@RunWith(Parameterized::class)
class ColorLightnessRepositoryImplTest(
    val color: Color,
    val expectedLightness: Float,
) {

    val colorConverter: ColorConverter = ColorConverter()
    val colorMapper: ColorMapper = ColorMapper()

    val sut = ColorLightnessRepositoryImpl(
        colorConverter = colorConverter,
        colorMapper = colorMapper,
    )

    @Test
    fun `color lightness validation`() {
        val lightness = with(sut) { hslLightness(color) }

        lightness shouldBeInEqualityRangeWith expectedLightness
    }

    infix fun Float.shouldBeInEqualityRangeWith(other: Float) {
        val delta = abs(this - other)
        val threshold = 0.0001f
        delta shouldBeLessThan threshold
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            /* #0  */ Color.Hex(0xFFFFFF) shouldHaveLightness 1.0f,
            /* #1  */ Color.Hex(0x000000) shouldHaveLightness 0.0f,
            /* #2  */ Color.Hex(0xF0F8FF) shouldHaveLightness 0.9706f,
            /* #3  */ Color.Hex(0x123456) shouldHaveLightness 0.2039f,
            /* #4  */ Color.Hex(0x1A803F) shouldHaveLightness 0.302f,
            /* #5  */ Color.Hex(0xF54021) shouldHaveLightness 0.5451f,
            /* #6  */ Color.Hex(0xF3A505) shouldHaveLightness 0.4863f,
        )

        infix fun Color.shouldHaveLightness(expectedLightness: Float): Array<Any> =
            arrayOf(this, expectedLightness)
    }
}