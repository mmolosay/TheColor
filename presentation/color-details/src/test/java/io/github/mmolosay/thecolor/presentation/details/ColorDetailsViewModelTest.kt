package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.exception.DomainException
import io.github.mmolosay.thecolor.domain.exception.DomainFailure
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandProvider
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ExactMatch
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
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
    val eventStoreMock: ColorDetailsEventStore = mockk {
        coEvery { send(event = any()) } just runs
    }
    val eventStoreReal = ColorDetailsEventStore()
    val colorRepository: ColorRepository = mockk()
    val createDataMock: CreateColorDetailsDataUseCase = mockk()
    val createDataReal = CreateColorDetailsDataUseCase(
        colorToColorInt = ColorToColorIntUseCase(
            colorConverter = ColorConverter(),
        ),
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
                    Result.success(fetchedDetails)
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
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.Timeout,
                    cause = Exception("test exception"),
                )
                Result.failure(exception)
            }
            createSut()

            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    @Test
    fun `emission of 'fetch data' command results in emission of seed color data`() =
        runTest(testDispatcher) {
            val color = mockk<Color.Hex>()
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val fetchedDetails: ColorDetails = mockk(relaxed = true)
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.success(fetchedDetails)
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
    fun `emission of 'fetch data' command cancels previous 'fetch data' job, so that repository is only accessed once`() =
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
            getColorDetailsDeferred.complete(value = Result.success(fetchedDetails))

            // verify that component that is called deep inside 'fetchColorDetails()' is only called once:
            // the first coroutine is canceled thus it never goes deep enough to trigger this component.
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
    fun `emission of 'set color details' command results in emission of seed color data`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.success(value = mockk())
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
                    Result.success(fetchedDetails)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(color, colorRole = null))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            val exactMatch = data.exactMatch.shouldBeInstanceOf<ExactMatch.No>()
            exactMatch.goToExactColor()

            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = Color.Hex(0x123456),
                    colorRole = ColorRole.Exact,
                )
                eventStoreMock.send(expectedEvent)
            }
        }

    /**
     * GIVEN [FetchData][ColorDetailsCommand.FetchData] command with `null` color role is emitted
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
                    Result.success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.success(fetchedDetailsForExactColor)
            createSut(
                createData = createDataReal,
            )
            commandFlow.emit(ColorDetailsCommand.FetchData(initialColor, colorRole = null))

            commandFlow.emit(ColorDetailsCommand.FetchData(exactColor, ColorRole.Exact))

            val data = sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data
            data.initialColorData shouldNotBe null
        }

    /**
     * GIVEN [FetchData][ColorDetailsCommand.FetchData] command with `null` color role is emitted
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
            val initialColorInt = ColorInt(0x1A803F)
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
                    Result.success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.success(fetchedDetailsForExactColor)
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
                    Result.success(fetchedDetailsForColor)
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns
                    Result.success(fetchedDetailsForExactColor)
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
                eventStoreMock.send(expectedEvent)
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
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
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
                        Result.success(value = fetchedDetails)
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
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
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
                    Result.success(value = mockk())
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

    /**
     * Tests that [ColorDetailsViewModel.cachedDetails] adds new elements in the end of the set,
     * thus enabling methods like [ColorDetailsViewModel.findCachedDetailsWithExactColor] to work as expected.
     */
    @Test
    fun `multiple repetitions of 'go to exact'-'go to initial' actions result in the correct data`() =
        runTest(testDispatcher) {
            // GIVEN
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns run {
                val colorDetails: ColorDetails = mockk(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { colorName } returns "initial"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                    every { this@mockk.hashCode() } returns -1066639853
                }
                Result.success(colorDetails)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val colorDetails: ColorDetails = mockk(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { colorName } returns "exact"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                    every { this@mockk.hashCode() } returns 1802741316
                }
                Result.success(colorDetails)
            }
            createSut(
                eventStore = eventStoreReal,
                createData = createDataReal,
            )

            // WHEN
            suspend fun emitFetchDataCommand(event: ColorDetailsEvent) {
                event.shouldBeInstanceOf<ColorDetailsEvent.ColorSelected>()
                val command = ColorDetailsCommand.FetchData(color = event.color, colorRole = event.colorRole)
                commandFlow.emit(command)
            }
            run fetchDataForInitialColor@{
                val command = ColorDetailsCommand.FetchData(color = initialColor, colorRole = null)
                commandFlow.emit(command)
            }

            val event1 = async { eventStoreReal.eventFlow.first() }
            sut.data.goToExactColor()
            emitFetchDataCommand(event = event1.await())

            val event2 = async { eventStoreReal.eventFlow.first() }
            sut.data.goToInitialColor()
            emitFetchDataCommand(event = event2.await())

            val event3 = async { eventStoreReal.eventFlow.first() }
            sut.data.goToExactColor()
            emitFetchDataCommand(event = event3.await())

            val event4 = async { eventStoreReal.eventFlow.first() }
            sut.data.goToInitialColor()
            emitFetchDataCommand(event = event4.await())

            // THEN
            sut.data.colorName shouldBe "initial"
        }

    fun createSut(
        eventStore: ColorDetailsEventStore = eventStoreMock,
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

    fun DataState.asReady(): DataState.Ready =
        this.shouldBeInstanceOf<DataState.Ready>()

    val ColorDetailsViewModel.data: ColorDetailsData
        get() = this.dataStateFlow.value.asReady().data

    val ColorDetailsData.goToExactColor: () -> Unit
        get() = this.exactMatch.shouldBeInstanceOf<ExactMatch.No>().goToExactColor

    val ColorDetailsData.goToInitialColor: () -> Unit
        get() = this.initialColorData.shouldNotBeNull().goToInitialColor
}