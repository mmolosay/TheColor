package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ColorInputMediatorTest {

    lateinit var sut: ColorInputMediator

    @Test
    fun `when SUT is initialized, then 'colorStateFlow' has correct initial value`() {
        createSut()

        val expectedValue = ColorState(color = null, source = null)
        colorState shouldBe expectedValue
        colorState shouldBe ColorInputMediator.InitialColorState
    }

    @Test
    fun `when 'null' color is set, then 'colorStateFlow' is updated with 'null' color`() {
        createSut()

        sut.set(color = null, source = null)

        colorState.color shouldBe null
    }

    @Test
    fun `when not-null color is set, then 'colorStateFlow' is updated with the provided color`() {
        createSut()

        val color = Color.Hex(0x0)
        sut.set(color = color, source = null)

        colorState.color shouldBe color
    }

    fun createSut() =
        ColorInputMediator().also {
            sut = it
        }

    val colorState: ColorState
        get() = sut.colorStateFlow.value
}