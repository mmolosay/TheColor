package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test

class ColorInputRgbViewModelTest {

    val rInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val gInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val bInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val viewData = ColorInputRgbViewData(rInputField, gInputField, bInputField)

    val mediator: ColorInputMediator = mockk {
        every { rgbStateFlow } returns emptyFlow()
        every { send<ColorPrototype.Hex>(any()) } just runs
    }

    val sut = ColorInputRgbViewModel(
        viewData = viewData,
        mediator = mediator,
        defaultDispatcher = StandardTestDispatcher(),
    )

    val uiData: ColorInputRgbUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `filtering keeps only digits`() {
        val result = uiData.rInputField.filterUserInput("abc1def2ghi3")

        result.string shouldBe "123"
    }

    @Test
    fun `filtering keeps only first 3 characters`() {
        val result = uiData.rInputField.filterUserInput("1234567890")

        result.string shouldBe "123"
    }
}