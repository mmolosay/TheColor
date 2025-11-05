package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputValidationResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ColorInputValidatorTest {

    val colorInputMapper: ColorInputMapper = mockk()
    val colorFactory: ColorFactory = mockk()

    val sut: ColorInputValidator = ColorInputValidator(
        colorInputMapper = colorInputMapper,
        colorFactory = colorFactory,
    )

    @ParameterizedTest
    @MethodSource("data")
    fun `validating given 'ColorInput' produces expected 'ColorInputValidationResult'`(
        givenColorInput: ColorInput,
        expectedColorInputValidationResult: ColorInputValidationResult,
        sutDependencies: SutDependencies?,
    ) {
        if (sutDependencies != null) {
            every {
                with(colorInputMapper) { givenColorInput.toPrototype() }
            } returns sutDependencies.prototypeFromColorInputMapper
            every {
                colorFactory.from(prototype = sutDependencies.prototypeFromColorInputMapper)
            } returns sutDependencies.parsedColorFromColorFactory
        }

        val validationResult = with(sut) { givenColorInput.validate() }

        validationResult shouldBe expectedColorInputValidationResult
    }

    data class SutDependencies(
        val prototypeFromColorInputMapper: ColorPrototype,
        val parsedColorFromColorFactory: Color?,
    )

    companion object {

        @JvmStatic
        fun data(): Array<Array<Any?>> = listOf(
            /* #0 */
            TestCase(
                givenColorInput = ColorInput.Hex(""),
                expectedColorInputValidationResult = ColorInputValidationResult.Invalid(
                    isEmpty = true,
                    isCompleteFromUserPerspective = false,
                ),
            ),
            /* #1 */
            TestCase(
                givenColorInput = ColorInput.Hex("0"),
                expectedColorInputValidationResult = ColorInputValidationResult.Invalid(
                    isEmpty = false,
                    isCompleteFromUserPerspective = false,
                ),
            ),
            /* #2 */
            TestCase(
                givenColorInput = ColorInput.Hex("01"),
                expectedColorInputValidationResult = ColorInputValidationResult.Invalid(
                    isEmpty = false,
                    isCompleteFromUserPerspective = false,
                ),
            ),
            /* #3 */
            run {
                val prototypeFromColorInputMapper: ColorPrototype = mockk()
                val parsedColorFromColorFactory: Color = mockk()
                TestCase(
                    givenColorInput = ColorInput.Hex("012"),
                    expectedColorInputValidationResult = ColorInputValidationResult.Valid(color = parsedColorFromColorFactory),
                    sutDependencies = SutDependencies(
                        prototypeFromColorInputMapper,
                        parsedColorFromColorFactory,
                    ),
                )
            },
            /* #4 */
            run {
                val prototypeFromColorInputMapper: ColorPrototype = mockk()
                val parsedColorFromColorFactory: Color? = null
                TestCase(
                    givenColorInput = ColorInput.Hex("012xxx"),
                    expectedColorInputValidationResult = ColorInputValidationResult.Invalid(
                        isEmpty = false,
                        isCompleteFromUserPerspective = true,
                    ),
                    sutDependencies = SutDependencies(
                        prototypeFromColorInputMapper,
                        parsedColorFromColorFactory,
                    ),
                )
            },
            /* #5 */
            run {
                val prototypeFromColorInputMapper: ColorPrototype = mockk()
                val parsedColorFromColorFactory: Color = mockk()
                TestCase(
                    givenColorInput = ColorInput.Hex("012345"),
                    expectedColorInputValidationResult = ColorInputValidationResult.Valid(color = parsedColorFromColorFactory),
                    sutDependencies = SutDependencies(
                        prototypeFromColorInputMapper,
                        parsedColorFromColorFactory,
                    ),
                )
            },
        )
            .map { it.asArrayOfAnys() }
            .toTypedArray()

        data class TestCase(
            val givenColorInput: ColorInput,
            val expectedColorInputValidationResult: ColorInputValidationResult,
            val sutDependencies: SutDependencies? = null,
        )

        fun TestCase.asArrayOfAnys(): Array<Any?> =
            arrayOf(givenColorInput, expectedColorInputValidationResult, sutDependencies)
    }
}