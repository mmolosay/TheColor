package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorStateWithSource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ColorInputMediatorTest {

    lateinit var sut: ColorInputMediator

    @Test
    fun `when SUT is initialized, then 'colorStateFlow' has correct initial value`() {
        createSut()

        val expectedValue = ColorStateWithSource(
            colorState = ColorState.AbsentOrInvalid,
            sourceInputType = null,
        )
        colorStateWithSource shouldBe expectedValue
    }

    @Test
    fun `when 'null' color is sent, then 'colorStateFlow' is updated with 'AbsentOrInvalid' color state`() {
        createSut()

        sut.send(
            color = null,
            from = null,
        )

        colorStateWithSource.colorState.shouldBeInstanceOf<ColorState.AbsentOrInvalid>()
    }

    @Test
    fun `when not-null color is sent, then 'colorStateFlow' is updated with 'Valid' color state`() {
        createSut()

        sut.send(
            color = Color.Hex(0x0),
            from = null,
        )

        colorStateWithSource.colorState.shouldBeInstanceOf<ColorState.Valid>()
    }

    fun createSut() =
        ColorInputMediator().also {
            sut = it
        }

    val colorStateWithSource: ColorStateWithSource
        get() = sut.colorStateFlow.value
}