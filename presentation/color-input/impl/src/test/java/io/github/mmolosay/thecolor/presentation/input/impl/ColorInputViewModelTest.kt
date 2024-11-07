package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test

class ColorInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk()
    val userPreferencesRepository: UserPreferencesRepository = mockk()

    lateinit var sut: ColorInputViewModel

    @Test
    fun `initial data is set on initialization`() {
        every {
            userPreferencesRepository.flowOfColorInputType()
        } returns flowOf(UserPreferences.ColorInputType.Hex)

        createSut()

        data.selectedViewType shouldBe ViewType.Hex
    }

    @Test
    fun `preferred input type is an initially selected one`() {
        every {
            userPreferencesRepository.flowOfColorInputType()
        } returns flowOf(UserPreferences.ColorInputType.Rgb)

        createSut()

        data.selectedViewType shouldBe ViewType.Rgb
    }

    @Test
    fun `preferred input type is first in the ordered list of input types`() {
        every {
            userPreferencesRepository.flowOfColorInputType()
        } returns flowOf(UserPreferences.ColorInputType.Rgb)

        createSut()

        data.orderedViewTypes.first() shouldBe ViewType.Rgb
    }

    @Test
    fun `changing input type to RGB updates data with RGB view type`() {
        every {
            userPreferencesRepository.flowOfColorInputType()
        } returns flowOf(UserPreferences.ColorInputType.Hex)
        createSut()

        data.onInputTypeChange(ViewType.Rgb)

        data.selectedViewType shouldBe ViewType.Rgb
    }

    fun createSut() =
        ColorInputViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            eventStore = mockk(),
            mediator = mediator,
            hexViewModelFactory = { _, _, _ -> mockk() },
            rgbViewModelFactory = { _, _, _ -> mockk() },
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val data: ColorInputData
        get() = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
}