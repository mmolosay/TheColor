package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandProvider
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateColorDetailsDataUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateSeedDataUseCase
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

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
@OptIn(ExperimentalCoroutinesApi::class)
class ColorDetailsViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val commandProvider: ColorDetailsCommandProvider = mockk()
    val eventStore: ColorDetailsEventStore = mockk {
        coEvery { send(event = any()) } just runs
    }
    val colorRepository: ColorRepository = mockk()
    val createDataMock: CreateColorDetailsDataUseCase = mockk()
    val colorToColorInt: ColorToColorIntUseCase = mockk {
        every { any<Color>().toColorInt() } returns mockk(relaxed = true)
    }
    val createDataReal = CreateColorDetailsDataUseCase(
        colorToColorInt = colorToColorInt,
    )
    val createSeedData: CreateSeedDataUseCase = mockk(relaxed = true)

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `SUT remains with initial Idle state if there's no 'fetch data' command emitted`() {
        every { commandProvider.commandFlow } returns emptyFlow()

        createSut()

        sut.dataStateFlow.value shouldBe DataState.Idle
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true)
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.Success(fetchedDetails)
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Error state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    HttpFailure.Timeout(
                cause = Exception("test exception"),
            )
            createSut()

            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    @Test
    fun `emission of 'fetch data' command results in emmision of seed color data`() =
        runTest(testDispatcher) {
            val color = mockk<Color.Hex>()
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true)
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.Success(fetchedDetails)
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            sut.currentSeedDataFlow.value shouldNotBe null
        }

    @Test
    fun `emission of 'fetch data' command cancells previous 'fetch data' job, so that repository is only accessed once`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true)
            val getColorDetailsDeferred = CompletableDeferred<Result<ColorDetails>>()
            coEvery {
                colorRepository.getColorDetails(color = any())
            } coAnswers {
                getColorDetailsDeferred.await()
            }
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            kotlin.run emitFirstCommand@{
                val command = ColorDetailsCommand.FetchData(
                    color = mockk<Color.Hex>(), colorRole = null
                )
                commandFlow.emit(command)
            }
            kotlin.run emitSecondCommand@{
                val command = ColorDetailsCommand.FetchData(
                    color = mockk<Color.Hex>(), colorRole = null
                )
                commandFlow.emit(command)
            }
            getColorDetailsDeferred.complete(value = Result.Success(fetchedDetails))

            // verify that component that is called deep inside 'fetchColorDetails()' is only called once:
            // the first coroutine is cancelled thus it never goes deep enough to trigger this component.
            coVerify(exactly = 1) {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            }
        }

    @Test
    fun `emission of 'set color details' command results in emmision of seed color data`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.Success(value = mockk())
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            val domainDetails: ColorDetails = mockk(relaxed = true)
            val command = ColorDetailsCommand.SetColorDetails(domainDetails)
            commandFlow.emit(command)

            sut.currentSeedDataFlow.value shouldNotBe null
        }

    @Test
    fun `invoking 'go to exact color' sends appropriate event to color details event store`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true) {
                every { exact.color } returns Color.Hex(0x123456)
                every { matchesExact } returns false
            }
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.Success(fetchedDetails)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            val exactMatch = data.exactMatch.shouldBeInstanceOf<ColorDetailsData.ExactMatch.No>()
            exactMatch.goToExactColor()

            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = Color.Hex(0x123456),
                    colorRole = ColorRole.Exact,
                )
                eventStore.send(expectedEvent)
            }
        }

    /**
     * GIVEN [FetchData][ColorDetailsCommand.FetchData] command with `null` color role is emtted
     *
     * WHEN [FetchData][ColorDetailsCommand.FetchData] command with [ColorRole.Exact] is emitted
     *
     * THEN initial color for this exact color is recalled and [ColorDetailsData.initialColorData] is not null
     */
    @Test
    fun `emission of 'fetch data' command with color type 'exact' results in present initial color data`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
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
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns
                    Result.Success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.Success(fetchedDetailsForExactColor)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(initialColor, colorRole = null))

            commandFlow.emit(ColorDetailsCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData shouldNotBe null
        }

    /**
     * GIVEN [FetchData][ColorDetailsCommand.FetchData] command with `null` color role is emtted
     *
     * WHEN [FetchData][ColorDetailsCommand.FetchData] command with [ColorRole.Exact] is emitted
     *
     * THEN initial color for this exact color is recalled, [initialColorData][ColorDetailsData.initialColorData] is not null,
     * and its [initialColor][ColorDetailsData.InitialColorData.initialColor] is correct.
     */
    @Test
    fun `emission of 'fetch data' command with color type 'exact' results in present initial color data with correct color value`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val initialColorInt = io.github.mmolosay.thecolor.presentation.api.ColorInt(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
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
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns
                    Result.Success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.Success(fetchedDetailsForExactColor)
            every { with(colorToColorInt) { initialColor.toColorInt() } } returns initialColorInt
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(initialColor, colorRole = null))

            commandFlow.emit(ColorDetailsCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData.shouldNotBeNull().initialColor shouldBe initialColorInt
        }

    /**
     * GIVEN there is a data with [initialColorData][ColorDetailsData.initialColorData]
     *
     * WHEN invoking [goToInitialColor][ColorDetailsData.InitialColorData.goToInitialColor]
     *
     * THEN event depicting it is sent to [ColorDetailsEventStore].
     */
    @Test
    fun `invoking 'go to initial color' sends appropriate event to color details event store`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val initialColorInt = io.github.mmolosay.thecolor.presentation.api.ColorInt(0x1A803F)
            val exactColor = Color.Hex(0x123456)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
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
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns
                    Result.Success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.Success(fetchedDetailsForExactColor)
            every { with(colorToColorInt) { initialColor.toColorInt() } } returns initialColorInt
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(initialColor, colorRole = null))
            commandFlow.emit(ColorDetailsCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData.shouldNotBeNull().goToInitialColor()

            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
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
     *  [FetchData][ColorDetailsCommand.FetchData] command is emitted and data fetching ends with failure
     *
     * THEN
     *  updated data state is [DataState.Error].
     */
    @Test
    fun `emission of 'fetch data' command that triggers failing data fetching results in emission of 'DataState Error'`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    HttpFailure.UnknownHost(cause = mockk())
            createSut()

            val command = ColorDetailsCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    /**
     * GIVEN
     *  1. SUT is initialized.
     *  2. [FetchData][ColorDetailsCommand.FetchData] command with color X is emitted and initial data is fetched.
     *  3. next [ColorRepository.getColorDetails] will end with failure.
     *  4. new [FetchData][ColorDetailsCommand.FetchData] command with color Y is emitted, but this time
     *  data fetching returns failure and data state is set to [DataState.Error].
     *  5. next [ColorRepository.getColorDetails] will end with success.
     *
     * WHEN
     *  [ColorDetailsError.tryAgain] is invoked
     *
     * THEN
     *  data is fetched successfully for color Y from last [FetchData][ColorDetailsCommand.FetchData]
     *  command.
     */
    @Test
    fun `invoking 'try again' action of 'DataState Error' will use color from last 'fetch data' command`() =
        runTest(testDispatcher) {
            fun mockGetColorDetailsReturnsSuccess() {
                val fetchedDetails: ColorDetails = mockk(relaxed = true) {
                    every { this@mockk.color } returns Color.Hex(0x000000) // doesn't matter
                    every { exact.color } returns Color.Hex(0x000000) // doesn't matter
                }
                coEvery { colorRepository.getColorDetails(color = any()) } returns
                        Result.Success(value = fetchedDetails)
            }

            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
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
            val command1 = ColorDetailsCommand.FetchData(
                color = color1, colorRole = null
            )
            commandFlow.emit(command1)
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    HttpFailure.UnknownHost(cause = mockk())
            val color2 = Color.Hex(0x222222)
            val command2 = ColorDetailsCommand.FetchData(
                color = color2, colorRole = null
            )
            commandFlow.emit(command2)
            mockGetColorDetailsReturnsSuccess()

            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>().error.tryAgain()

            val colors = mutableListOf<Color>()
            coVerify {
                colorRepository.getColorDetails(color = capture(colors))
            }
            colors.last() shouldBe color2
        }

    @Test
    fun `emission of 'set color details' command results in emission of Ready state`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.Success(value = mockk())
            every {
                createDataMock(
                    details = any(),
                    goToExactColor = any(),
                    initialColor = any(),
                    goToInitialColor = any(),
                )
            } returns mockk()
            createSut()

            val domainDetails: ColorDetails = mockk(relaxed = true)
            val command = ColorDetailsCommand.SetColorDetails(domainDetails)
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    fun createSut(
        createData: CreateColorDetailsDataUseCase = createDataMock,
        coroutineDispatcher: CoroutineDispatcher = testDispatcher,
    ) =
        ColorDetailsViewModel(
            coroutineScope = CoroutineScope(context = coroutineDispatcher),
            commandProvider = commandProvider,
            eventStore = eventStore,
            colorRepository = colorRepository,
            createData = createData,
            createSeedData = createSeedData,
            ioDispatcher = coroutineDispatcher,
            defaultDispatcher = coroutineDispatcher,
        ).also {
            sut = it
        }
}