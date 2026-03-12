package io.github.mmolosay.thecolor.presentation.details

import app.cash.turbine.test
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
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
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ColorRoleData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateColorDetailsDataUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateSeedDataUseCase
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
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
    val createDataMock: CreateColorDetailsDataUseCase = mockk {
        every {
            invoke(
                details = any(),
                colorRole = any(),
                goToExactColor = any(),
                goToInitialColor = any(),
                getInitialColorOfExactColor = any(),
            )
        } returns mockk()
    }
    val createDataReal = CreateColorDetailsDataUseCase(
        colorToColorInt = ColorToColorIntUseCase(
            colorConverter = ColorConverter(),
        ),
    )
    val createSeedData: CreateSeedDataUseCase = mockk(relaxed = true)
    val colorComparator = ColorComparator(
        colorConverter = ColorConverter(),
    )

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `given SUT is created, when no 'fetch data' command emitted, then SUT remains with initial Idle state`() {
        every { commandProvider.commandFlow } returns emptyFlow()

        createSut()

        sut.dataStateFlow.value should beOfType<DataState.Idle>()
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color) } returns run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color
                }
                Result.success(details)
            }
            createSut()

            sut.dataStateFlow.test {
                run {
                    val command = ColorDetailsCommand.FetchData(color, colorRole = null)
                    commandFlow.emit(command)
                }

                expectMostRecentItem() should beOfType<DataState.Ready>()
            }
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Error state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.Timeout,
                    cause = Exception("test exception"),
                )
                Result.failure(exception)
            }
            createSut()

            sut.dataStateFlow.test {
                run {
                    val command = ColorDetailsCommand.FetchData(color, colorRole = null)
                    commandFlow.emit(command)
                }

                expectMostRecentItem() should beOfType<DataState.Error>()
            }
        }

    @Test
    fun `emission of 'fetch data' command results in emission of seed color data`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color
                }
                Result.success(details)
            }
            createSut()

            sut.currentSeedDataFlow.test {
                run {
                    val command = ColorDetailsCommand.FetchData(color, colorRole = null)
                    commandFlow.emit(command)
                }

                expectMostRecentItem() shouldNotBe null
            }
        }

    @Test
    fun `emission of 'fetch data' command cancels previous 'fetch data' job, so that repository is only accessed once`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            val getColorDetailsDeferred = CompletableDeferred<Result<ColorDetails>>()
            coEvery {
                colorRepository.getColorDetails(color = any())
            } coAnswers {
                getColorDetailsDeferred.await()
            }
            createSut()

            val color1 = Color.Hex(0x0)
            run emitFirstCommand@{
                val command = ColorDetailsCommand.FetchData(
                    color = color1, colorRole = null
                )
                commandFlow.emit(command)
            }
            val color2 = Color.Hex(0x1)
            run emitSecondCommand@{
                val command = ColorDetailsCommand.FetchData(
                    color = color2, colorRole = null
                )
                commandFlow.emit(command)
            }
            run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color1
                }
                val value = Result.success(details)
                getColorDetailsDeferred.complete(value)
            }

            // verify that component that is called deep inside 'fetchColorDetails()' is only called once:
            // the first coroutine is canceled thus it never goes deep enough to trigger this component.
            coVerify(exactly = 1) {
                createDataMock(
                    details = any(),
                    colorRole = any(),
                    goToExactColor = any(),
                    goToInitialColor = any(),
                    getInitialColorOfExactColor = any(),
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
            createSut()

            sut.currentSeedDataFlow.test {
                run {
                    val details = mockk<ColorDetails>(relaxed = true)
                    val command = ColorDetailsCommand.SetColorDetails(details)
                    commandFlow.emit(command)
                }

                expectMostRecentItem() shouldNotBe null
            }
        }

    @Test
    fun `invoking 'go to exact color' sends appropriate event to color details event store`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(initialColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                }
                Result.success(details)
            }
            createSut(
                createData = createDataReal,
            )

            run {
                val command = ColorDetailsCommand.FetchData(initialColor, colorRole = null)
                commandFlow.emit(command)
            }
            sut.data.goToExactColor()

            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventStoreMock.send(expectedEvent)
            }
        }

    /**
     * Tests that
     * 1. SUT "recalls" an initial color `cI` for specified exact color `cE`
     * 2. SUT verifies that color `cE` from the [ColorDetailsCommand.FetchData] is indeed a [ColorRole.Exact]
     * for the initial color `cI`
     * 3. SUT produces correct [ColorDetailsData] with [ColorDetailsData.colorRoleData] being a [ColorRoleData.Exact].
     */
    @Test
    fun `emission of 'fetch data' command with color role 'exact' results in emission of the correct data`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                }
                Result.success(details)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                }
                Result.success(details)
            }
            createSut(
                createData = createDataReal,
            )

            sut.dataStateFlow.test {
                run {
                    val command = ColorDetailsCommand.FetchData(initialColor, colorRole = null)
                    commandFlow.emit(command)
                }
                run {
                    val command = ColorDetailsCommand.FetchData(exactColor, ColorRole.Exact)
                    commandFlow.emit(command)
                }

                val data = expectMostRecentItem().shouldBeInstanceOf<DataState.Ready>().data
                data.colorRoleData should beOfType<ColorRoleData.Exact>()
            }
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
            val exactColor = Color.Hex(0x126B40)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                }
                Result.success(details)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                }
                Result.success(details)
            }
            createSut(
                createData = createDataReal,
            )

            run {
                val command = ColorDetailsCommand.FetchData(initialColor, colorRole = null)
                commandFlow.emit(command)
            }
            run {
                val command = ColorDetailsCommand.FetchData(exactColor, colorRole = ColorRole.Exact)
                commandFlow.emit(command)
            }

            val colorRoleData = sut.data.colorRoleData.shouldBeInstanceOf<ColorRoleData.Exact>()
            colorRoleData.initialColor shouldBe ColorInt(0x1A803F)
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
            // GIVEN
            val initialColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = initialColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                }
                Result.success(details)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                }
                Result.success(details)
            }
            createSut(
                createData = createDataReal,
            )

            // WHEN
            run {
                val command = ColorDetailsCommand.FetchData(initialColor, colorRole = null)
                commandFlow.emit(command)
            }
            run {
                val command = ColorDetailsCommand.FetchData(exactColor, colorRole = ColorRole.Exact)
                commandFlow.emit(command)
            }
            sut.data.goToInitialColor()

            // THEN
            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = initialColor,
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

            run {
                val command = ColorDetailsCommand.FetchData(color = mockk(), colorRole = null)
                commandFlow.emit(command)
            }

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
            createSut()

            val color1 = Color.Hex(0x0)
            run {
                val command = ColorDetailsCommand.FetchData(color = color1, colorRole = null)
                commandFlow.emit(command)
            }
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
            val color2 = Color.Hex(0x1)
            run {
                val command = ColorDetailsCommand.FetchData(color = color2, colorRole = null)
                commandFlow.emit(command)
            }
            mockGetColorDetailsReturnsSuccess()

            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>().error.tryAgain()
            val capturedColors = mutableListOf<Color>()
            coVerify {
                colorRepository.getColorDetails(color = capture(capturedColors))
            }
            capturedColors.last() shouldBe color2
        }

    @Test
    fun `emission of 'set color details' command results in emission of Ready state`() =
        runTest(testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorDetailsCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { colorRepository.getColorDetails(color = any()) } returns
                    Result.success(value = mockk())
            createSut()

            run {
                val details: ColorDetails = mockk(relaxed = true)
                val command = ColorDetailsCommand.SetColorDetails(details)
                commandFlow.emit(command)
            }

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
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns initialColor
                    every { colorName } returns "initial"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                    every { this@mockk.hashCode() } returns -1066639853
                }
                Result.success(details)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { colorName } returns "exact"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                    every { this@mockk.hashCode() } returns 1802741316
                }
                Result.success(details)
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
            colorComparator = colorComparator,
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
        get() = this.colorRoleData.shouldBeInstanceOf<ColorRoleData.Initial>().goToExactColor

    val ColorDetailsData.goToInitialColor: () -> Unit
        get() = this.colorRoleData.shouldBeInstanceOf<ColorRoleData.Exact>().goToInitialColor
}