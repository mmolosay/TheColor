package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.ColorInt
import io.github.mmolosay.thecolor.presentation.ColorRole
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * In most cases SUT ViewModel will use mocked instance of [CreateColorDetailsDataUseCase].
 * It is done to simplify tests which don't check contents of returned data: we can just return mock from the use case.
 *
 * In some other cases, we want to check contents of returned data.
 * For that we pass real instance of [CreateColorDetailsDataUseCase] to ViewModel.
 * This way the code of use case is treated like internal private part of ViewModel.
 * This approach produces data as if it was in production, meaning that contents are plausible
 * and appropriate for tests that verify values.
 */
class ColorDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val commandProvider: ColorCenterCommandProvider = mockk()
    val eventStore: ColorCenterEventStore = mockk {
        coEvery { send(event = any()) } just runs
    }
    val getColorDetails: GetColorDetailsUseCase = mockk()
    val createDataMock: CreateColorDetailsDataUseCase = mockk()
    val colorToColorInt: ColorToColorIntUseCase = mockk {
        every { any<Color>().toColorInt() } returns mockk(relaxed = true)
    }
    val createDataReal = CreateColorDetailsDataUseCase(
        colorToColorInt = colorToColorInt,
    )

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `SUT remains with initial Idle state if there's no 'fetch data' command emitted`() {
        every { commandProvider.commandFlow } returns emptyFlow()

        createSut()

        sut.dataStateFlow.value shouldBe DataState.Idle
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true)
            coEvery { getColorDetails.invoke(any<Color>()) } returns Result.Success(fetchedDetails)
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            commandFlow.emit(ColorCenterCommand.FetchData(color, colorRole = null))

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Loading state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = mockk<Color.Hex>()
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails.invoke(any<Color>()) } returns Result.Success(value = mockk())
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            // "then"
            launch {
                sut.dataStateFlow
                    .drop(1) // ignore initial Idle state
                    .first() should beOfType<DataState.Loading>()
            }

            // "when"
            commandFlow.emit(ColorCenterCommand.FetchData(color, colorRole = null))
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

            commandFlow.emit(ColorCenterCommand.FetchData(color, colorRole = null))

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    @Test
    fun `invoking 'on exact click' sends appropriate event to color center event store`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true) {
                every { exact.color } returns Color.Hex(0x123456)
                every { matchesExact } returns false
            }
            coEvery { getColorDetails.invoke(any<Color>()) } returns Result.Success(fetchedDetails)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorCenterCommand.FetchData(color, colorRole = null))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            val exactMatch = data.exactMatch.shouldBeInstanceOf<ColorDetailsData.ExactMatch.No>()
            exactMatch.goToExactColor()

            coVerify {
                val expectedEvent = ColorCenterEvent.ColorSelected(
                    color = Color.Hex(0x123456),
                    colorRole = ColorRole.Exact,
                )
                eventStore.send(expectedEvent)
            }
        }

    fun createSut(
        createData: CreateColorDetailsDataUseCase = createDataMock,
    ) =
        ColorDetailsViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            commandProvider = commandProvider,
            eventStore = eventStore,
            getColorDetails = getColorDetails,
            createData = createData,
            ioDispatcher = mainDispatcherRule.testDispatcher,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}