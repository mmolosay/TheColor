package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.api.ColorRole
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    fun `invoking 'go to exact color' sends appropriate event to color center event store`() =
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

    /**
     * GIVEN [FetchData][ColorCenterCommand.FetchData] command with `null` color role is emtted
     *
     * WHEN [FetchData][ColorCenterCommand.FetchData] command with [ColorRole.Exact] is emitted
     *
     * THEN initial color for this exact color is recalled and [ColorDetailsData.initialColorData] is not null
     */
    @Test
    fun `emission of 'fetch data' command with color type 'exact' results in present initial color data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetailsForColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns initialColor
                every { exact.color } returns exactColor
                every { matchesExact } returns false
            }
            val fetchedDetailsForExactColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns exactColor
                every { exact.color } returns exactColor
                every { matchesExact } returns true
            }
            coEvery { getColorDetails.invoke(initialColor) } returns
                Result.Success(fetchedDetailsForColor)
            coEvery { getColorDetails.invoke(exactColor) } returns
                Result.Success(fetchedDetailsForExactColor)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorCenterCommand.FetchData(initialColor, colorRole = null))

            commandFlow.emit(ColorCenterCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData shouldNotBe null
        }

    /**
     * GIVEN [FetchData][ColorCenterCommand.FetchData] command with `null` color role is emtted
     *
     * WHEN [FetchData][ColorCenterCommand.FetchData] command with [ColorRole.Exact] is emitted
     *
     * THEN initial color for this exact color is recalled, [initialColorData][ColorDetailsData.initialColorData] is not null,
     * and its [initialColor][ColorDetailsData.InitialColorData.initialColor] is correct.
     */
    @Test
    fun `emission of 'fetch data' command with color type 'exact' results in present initial color data with correct color value`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val initialColorInt = io.github.mmolosay.thecolor.presentation.api.ColorInt(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetailsForColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns initialColor
                every { exact.color } returns exactColor
                every { matchesExact } returns false
            }
            val fetchedDetailsForExactColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns exactColor
                every { exact.color } returns exactColor
                every { matchesExact } returns true
            }
            coEvery { getColorDetails.invoke(initialColor) } returns
                Result.Success(fetchedDetailsForColor)
            coEvery { getColorDetails.invoke(exactColor) } returns
                Result.Success(fetchedDetailsForExactColor)
            every { with(colorToColorInt) { initialColor.toColorInt() } } returns initialColorInt
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorCenterCommand.FetchData(initialColor, colorRole = null))

            commandFlow.emit(ColorCenterCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData.shouldNotBeNull().initialColor shouldBe initialColorInt
        }

    /**
     * GIVEN there is a data with [initialColorData][ColorDetailsData.initialColorData]
     *
     * WHEN invoking [goToInitialColor][ColorDetailsData.InitialColorData.goToInitialColor]
     *
     * THEN event depicting it is sent to [ColorCenterEventStore].
     */
    @Test
    fun `invoking 'go to initial color' sends appropriate event to color center event store`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val initialColorInt = io.github.mmolosay.thecolor.presentation.api.ColorInt(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetailsForColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns initialColor
                every { exact.color } returns exactColor
                every { matchesExact } returns false
            }
            val fetchedDetailsForExactColor: ColorDetails = mockk(relaxed = true) {
                every { this@mockk.color } returns exactColor
                every { exact.color } returns exactColor
                every { matchesExact } returns true
            }
            coEvery { getColorDetails.invoke(initialColor) } returns
                Result.Success(fetchedDetailsForColor)
            coEvery { getColorDetails.invoke(exactColor) } returns
                Result.Success(fetchedDetailsForExactColor)
            every { with(colorToColorInt) { initialColor.toColorInt() } } returns initialColorInt
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorCenterCommand.FetchData(initialColor, colorRole = null))
            commandFlow.emit(ColorCenterCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData.shouldNotBeNull().goToInitialColor()

            coVerify {
                val expectedEvent = ColorCenterEvent.ColorSelected(
                    color = Color.Hex(0x1A803F),
                    colorRole = ColorRole.Initial,
                )
                eventStore.send(expectedEvent)
            }
        }

    /**
     * GIVEN
     *  1. fetching color details will end with failure.
     *  2. SUT is initialized.
     *
     * WHEN
     *  [FetchData][ColorCenterCommand.FetchData] command is emitted and data fetching ends with failure
     *
     * THEN
     *  updated data state is [DataState.Error].
     */
    @Test
    fun `emission of 'fetch data' command that triggers failing data fetching results in emission of 'DataState Error'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails(color = any()) } returns
                HttpFailure.UnknownHost(cause = mockk())
            createSut()

            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    /**
     * GIVEN
     *  1. SUT is initialized.
     *  2. [FetchData][ColorCenterCommand.FetchData] command with color X is emitted and initial data is fetched.
     *  3. next [getColorDetails] will end with failure.
     *  4. new [FetchData][ColorCenterCommand.FetchData] command with color Y is emitted, but this time
     *  data fetching returns failure and data state is set to [DataState.Error].
     *  5. next [getColorDetails] will end with success.
     *
     * WHEN
     *  [ColorDetailsError.tryAgain] is invoked
     *
     * THEN
     *  data is fetched successfully for color Y from last [FetchData][ColorCenterCommand.FetchData]
     *  command.
     */
    @Test
    fun `invoking 'try again' action of 'DataState Error' will use color from last 'fetch data' command`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fun mockGetColorDetailsReturnsSuccess() {
                val fetchedDetails: ColorDetails = mockk(relaxed = true) {
                    every { this@mockk.color } returns Color.Hex(0x000000) // doesn't matter
                    every { exact.color } returns Color.Hex(0x000000) // doesn't matter
                }
                coEvery { getColorDetails(color = any()) } returns
                    Result.Success(value = fetchedDetails)
            }

            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            mockGetColorDetailsReturnsSuccess()
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()
            val color1 = Color.Hex(0x111111)
            val command1 = ColorCenterCommand.FetchData(
                color = color1, colorRole = null
            )
            commandFlow.emit(command1)
            coEvery { getColorDetails(color = any()) } returns
                HttpFailure.UnknownHost(cause = mockk())
            val color2 = Color.Hex(0x222222)
            val command2 = ColorCenterCommand.FetchData(
                color = color2, colorRole = null
            )
            commandFlow.emit(command2)
            mockGetColorDetailsReturnsSuccess()

            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>().error.tryAgain()

            val colors = mutableListOf<Color>()
            coVerify {
                getColorDetails.invoke(color = capture(colors))
            }
            colors.last() shouldBe color2
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