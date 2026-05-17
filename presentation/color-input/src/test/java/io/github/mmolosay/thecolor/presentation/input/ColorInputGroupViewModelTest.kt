package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupData
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputGroupViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val mediator: ColorInputMediator = mockk()
    val userPreferencesRepository: UserPreferencesRepository = mockk()

    lateinit var sut: ColorInputGroupViewModel

    @Test
    fun `initial data is set on initialization`() {
        every {
            userPreferencesRepository.flowOfColorInputType
        } returns run {
            val value = DomainColorInputType.Hex
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }

        createSut()

        data.selectedInputType shouldBe DomainColorInputType.Hex
    }

    @Test
    fun `preferred input type is an initially selected one`() {
        every {
            userPreferencesRepository.flowOfColorInputType
        } returns run {
            val value = DomainColorInputType.Rgb
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }

        createSut()

        data.selectedInputType shouldBe DomainColorInputType.Rgb
    }

    @Test
    fun `preferred input type is first in the ordered list of input types`() {
        every {
            userPreferencesRepository.flowOfColorInputType
        } returns run {
            val value = DomainColorInputType.Rgb
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }

        createSut()

        data.orderedInputTypes.first() shouldBe DomainColorInputType.Rgb
    }

    @Test
    fun `changing input type to RGB updates data with RGB selected input type`() {
        every {
            userPreferencesRepository.flowOfColorInputType
        } returns run {
            val value = DomainColorInputType.Hex
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }
        createSut()

        data.onInputTypeChange(DomainColorInputType.Rgb)

        data.selectedInputType shouldBe DomainColorInputType.Rgb
    }

    fun createSut() =
        ColorInputGroupViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            mediator = mediator,
            submitAction = mockk(),
            hexViewModelFactory = { _, _, _ -> mockk() },
            rgbViewModelFactory = { _, _, _ -> mockk() },
            hsvViewModelFactory = { _, _ -> mockk() },
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val data: ColorInputGroupData
        get() = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
}