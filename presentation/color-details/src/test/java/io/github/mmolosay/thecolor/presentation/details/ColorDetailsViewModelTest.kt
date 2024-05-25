package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val commandProvider: ColorCenterCommandProvider = mockk()
    val getColorDetails: GetColorDetailsUseCase = mockk()
    val createData: CreateColorDetailsDataUseCase = mockk()

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `SUT remains with initial Idle state if there's no 'fetch data' command emitted`() {
        every { commandProvider.commandFlow } returns emptyFlow()

        createSut()

        sut.dataStateFlow.value shouldBe State.Idle
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails.invoke(any<Color>()) } returns Result.Success(value = mockk())
            every { createData(details = any()) } returns mockk()
            createSut()

            commandFlow.emit(ColorCenterCommand.FetchData(color))

            sut.dataStateFlow.value should beOfType<State.Ready>()
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Loading state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = mockk<Color.Hex>()
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails.invoke(any<Color>()) } returns Result.Success(value = mockk())
            every { createData(details = any()) } returns mockk()
            createSut()

            // "then"
            launch {
                sut.dataStateFlow
                    .drop(1) // ignore initial Idle state
                    .first() should beOfType<State.Loading>()
            }

            // "when"
            commandFlow.emit(ColorCenterCommand.FetchData(color))
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Error state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails.invoke(any<Color>()) } returns HttpFailure.Timeout(
                cause = Exception("test exception"),
            )
            createSut()

            commandFlow.emit(ColorCenterCommand.FetchData(color))

            sut.dataStateFlow.value should beOfType<State.Error>()
        }

    fun createSut() =
        ColorDetailsViewModel(
            commandProvider = commandProvider,
            getColorDetails = getColorDetails,
            createData = createData,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}