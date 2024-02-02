package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.input.ColorInputData.ViewType
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class ColorInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        coEvery { init() } just runs
        // TODO: probably will require some changes / refinements when View UI is gone
        every { colorStateFlow } returns MutableSharedFlow()
    }

    lateinit var sut: ColorInputViewModel

    val uiData: ColorInputData
        get() = sut.dataFlow.value

    @Test
    fun `mediator is initialized when sut is created`() {
        createSut()

        coVerify(exactly = 1) { mediator.init() }
    }

    @Test
    fun `initial data is set on initialization`() {
        createSut()

        uiData.viewType shouldBe ViewType.Hex
    }

    @Test
    fun `changing input type to Rgb updates data with Rgb view type`() {
        createSut()

        uiData.onInputTypeChange(ViewType.Rgb)

        uiData.viewType shouldBe ViewType.Rgb
    }

    fun createSut() =
        ColorInputViewModel(
            mediator = mediator,
        ).also {
            sut = it
        }
}