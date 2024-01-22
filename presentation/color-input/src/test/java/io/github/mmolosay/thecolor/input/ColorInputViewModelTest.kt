package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.ColorInputUiData.ViewType
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Rule
import org.junit.Test

class ColorInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val viewData: ColorInputUiData.ViewData = mockk {
        every { hexLabel } returns "mocked"
        every { rgbLabel } returns "mocked"
    }

    val mediator: ColorInputMediator = mockk {
        coEvery { init() } just runs
    }

    lateinit var sut: ColorInputViewModel

    val uiData: ColorInputUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `mediator is initialized when sut is created`() {
        createSut()

        coVerify(exactly = 1) { mediator.init() }
    }

    @Test
    fun `initial UiData is set on initialization`() {
        every { viewData.hexLabel } returns "hex label"
        every { viewData.rgbLabel } returns "rgb label"

        createSut()

        uiData.viewType shouldBe ViewType.Hex
        uiData.hexLabel shouldBe "hex label"
        uiData.rgbLabel shouldBe "rgb label"
    }

    @Test
    fun `changing input type to Rgb updates UiData with Rgb view type`() {
        createSut()

        uiData.onInputTypeChange(ViewType.Rgb)

        uiData.viewType shouldBe ViewType.Rgb
    }

    fun createSut() =
        ColorInputViewModel(
            viewData = viewData,
            mediator = mediator,
        ).also {
            sut = it
        }
}