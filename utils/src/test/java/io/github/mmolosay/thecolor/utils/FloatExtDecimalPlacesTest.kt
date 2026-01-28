package io.github.mmolosay.thecolor.utils

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Tests [Float.decimalPlaces].
 */
class FloatExtDecimalPlacesTest {

    @ParameterizedTest
    @MethodSource("decimalPlacesData")
    fun `Float has expected number of decimal places`(
        number: Float,
        expectedDecimalPlaces: Int,
    ) {
        val decimalPlaces = number.decimalPlaces()

        withClue("Float $number should have $expectedDecimalPlaces decimal places, but actual was $decimalPlaces") {
            decimalPlaces shouldBe expectedDecimalPlaces
        }
    }

    companion object {

        @JvmStatic
        fun decimalPlacesData() = listOf(
            0f shouldHaveDecimalPlaces 0,
            0.1f shouldHaveDecimalPlaces 1,
            0.12345f shouldHaveDecimalPlaces 5,
            10f shouldHaveDecimalPlaces 0,
            100.1f shouldHaveDecimalPlaces 1,
            -0f shouldHaveDecimalPlaces 0,
            -0.1f shouldHaveDecimalPlaces 1,
            -0.12345f shouldHaveDecimalPlaces 5,
            1e-6f shouldHaveDecimalPlaces 6,
            @Suppress("FloatingPointLiteralPrecision")
            2.7182818284f shouldHaveDecimalPlaces 7, // will be 2.7182817 in runtime
        )

        infix fun Float.shouldHaveDecimalPlaces(expected: Int): Array<Any> =
            arrayOf(this, expected)
    }
}