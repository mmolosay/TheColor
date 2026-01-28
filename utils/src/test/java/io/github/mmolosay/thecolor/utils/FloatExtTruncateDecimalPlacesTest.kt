package io.github.mmolosay.thecolor.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Tests [Float.truncateDecimalPlaces].
 */
class FloatExtTruncateDecimalPlacesTest {

    @ParameterizedTest
    @MethodSource("data")
    fun `Float is truncated to expected number of decimal places`(
        number: Float,
        keep: Int,
        expectedResult: Float,
    ) {
        val result = number.truncateDecimalPlaces(keep)

        withClue("Float $number should be truncated to $expectedResult keeping $keep decimal places, but actual was $result") {
            result shouldBe expectedResult
        }
    }

    @Test // special case not included in @ParameterizedTest to avoid complicating 'TestCase'
    fun `when trying to truncate to negative number of decimal places, error is thrown`() {
        shouldThrow<IllegalArgumentException> {
            0.12345f.truncateDecimalPlaces(keep = -1)
        }
    }

    companion object {

        @JvmStatic
        fun data(): List<Array<Any>> = listOf(
            TestCase(
                number = 0f,
                keep = 0,
                expectedResult = 0f,
            ),
            TestCase(
                number = 0.12345f,
                keep = 3,
                expectedResult = 0.123f,
            ),
            TestCase(
                number = 0.12345f,
                keep = 7,
                expectedResult = 0.12345f,
            ),
        )
            .map { it.asArrayOfAnys() }

        data class TestCase(
            val number: Float,
            val keep: Int,
            val expectedResult: Float,
        )

        fun TestCase.asArrayOfAnys(): Array<Any> =
            arrayOf(number, keep, expectedResult)
    }
}