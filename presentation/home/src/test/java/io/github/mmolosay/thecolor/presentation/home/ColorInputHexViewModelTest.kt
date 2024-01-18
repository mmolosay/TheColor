package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ColorInputHexViewModelTest {

    val viewData: ColorInputFieldUiData.ViewData = mockk(relaxed = true)

    val mediator: ColorInputMediator = mockk {
        every { hexStateFlow } returns emptyFlow()
        every { send<ColorPrototype.Hex>(any()) } just runs
    }

    val sut = ColorInputHexViewModel(
        viewData = viewData,
        mediator = mediator,
        defaultDispatcher = StandardTestDispatcher(),
    )

    val uiData: ColorInputHexUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `filtering keeps only digits and letter A-F`() = runTest {
        val result = uiData.inputField.filterUserInput("123abc_!.@ABG")

        result.string shouldBe "123AB"
    }

    @Test
    fun `filtering keeps only first 6 characters`() = runTest {
        val result = uiData.inputField.filterUserInput("123456789ABCDEF")

        result.string shouldBe "123456"
    }
}