package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test

class ColorInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        coEvery { init() } just runs
    }

    lateinit var sut: ColorInputViewModel

    val uiData: ColorInputData
        get() = sut.dataStateFlow.value

    @Test
    fun `mediator is initialized when sut is created`() {
        createSut()

        coVerify(exactly = 1) { mediator.init() }
    }

    @Test
    fun `initial data is set on initialization`() {
        createSut()

        uiData.selectedViewType shouldBe ViewType.Hex
    }

    @Test
    fun `changing input type to Rgb updates data with Rgb view type`() {
        createSut()

        uiData.onInputTypeChange(ViewType.Rgb)

        uiData.selectedViewType shouldBe ViewType.Rgb
    }

    fun createSut() =
        ColorInputViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            eventStore = mockk(),
            mediator = mediator,
            hexViewModelFactory = { _, _, _ -> mockk() },
            rgbViewModelFactory = { _, _, _ -> mockk() },
        ).also {
            sut = it
        }
}