package io.github.mmolosay.thecolor.presentation.details

import app.cash.turbine.test
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.exception.DomainException
import io.github.mmolosay.thecolor.domain.exception.DomainFailure
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData.ColorRoleData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateColorDetailsDataUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateSubjectColorDataUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
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

    val commandStore = DiModule.ProvideModule.provideCommandStore()
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
                selectSeedColor = any(),
                selectExactColor = any(),
                getSeedColor = any(),
            )
        } returns mockk()
    }
    val createDataReal = CreateColorDetailsDataUseCase(
        colorToColorInt = ColorToColorIntUseCase(
            colorConverter = ColorConverter(),
        ),
    )
    val createSubjectColorData: CreateSubjectColorDataUseCase = mockk(relaxed = true)
    val colorComparator = ColorComparator(
        colorConverter = ColorConverter(),
    )

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `given SUT is created, when no 'set seed color' command emitted, then SUT remains with initial 'Idle' state`() {
        createSut()

        sut.dataStateFlow.value should beOfType<DataState.Idle>()
    }

    @Test
    fun `when 'set seed color' command is emitted, then SUT emits 'Ready' state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            coEvery { colorRepository.getColorDetails(color) } returns run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color
                    every { exact.color } returns Color.Hex(0x126B40)
                }
                Result.success(details)
            }
            createSut()

            sut.dataStateFlow.test {
                run {
                    val command = ColorDetailsCommand.SetSeedColor(color)
                    commandStore.channel.send(command)
                }

                expectMostRecentItem() should beOfType<DataState.Ready>()
            }
        }

    @Test
    fun `when 'set seed color' command is emitted, then SUT emits 'Error' state`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
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
                    val command = ColorDetailsCommand.SetSeedColor(color)
                    commandStore.channel.send(command)
                }

                expectMostRecentItem() should beOfType<DataState.Error>()
            }
        }

    @Test
    fun `when 'set seed color' command is emitted, then SUT emits subject color data`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            coEvery { colorRepository.getColorDetails(color = any()) } returns run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color
                    every { exact.color } returns Color.Hex(0x126B40)
                }
                Result.success(details)
            }
            createSut()

            sut.subjectColorDataFlow.test {
                run {
                    val command = ColorDetailsCommand.SetSeedColor(color)
                    commandStore.channel.send(command)
                }

                expectMostRecentItem() shouldNotBe null
            }
        }


    @Test
    fun `when 'set seed color' command is emitted, then previous 'set seed color' job is canceled, so that repository is only accessed once`() =
        runTest(testDispatcher) {
            val color1 = Color.Hex(0x0)
            val color2 = Color.Hex(0x1)
            coEvery {
                colorRepository.getColorDetails(color = color1)
            } coAnswers {
                suspendCancellableCoroutine {}
            }
            val getColorDetailsDeferred = CompletableDeferred<Result<ColorDetails>>()
            coEvery {
                colorRepository.getColorDetails(color = color2)
            } coAnswers {
                getColorDetailsDeferred.await()
            }
            createSut()

            run emitFirstCommand@{
                val command = ColorDetailsCommand.SetSeedColor(color = color1)
                commandStore.channel.send(command)
            }
            run emitSecondCommand@{
                val command = ColorDetailsCommand.SetSeedColor(color = color2)
                commandStore.channel.send(command)
            }
            run {
                val details = mockk<ColorDetails> {
                    every { this@mockk.color } returns color2
                    every { exact.color } returns Color.Hex(0x3)
                }
                val value = Result.success(details)
                getColorDetailsDeferred.complete(value)
            }

            // verify that component that is called deep inside 'process(command)' is only called once:
            // the first coroutine is canceled thus it never goes deep enough to trigger this component.
            coVerify(exactly = 1) {
                createDataMock(
                    details = any(),
                    colorRole = any(),
                    selectSeedColor = any(),
                    selectExactColor = any(),
                    getSeedColor = any(),
                )
            }
        }

    @Test
    fun `when 'set seed details' command is emitted, then SUT emits subject color data`() =
        runTest(testDispatcher) {
            createSut()

            sut.subjectColorDataFlow.test {
                run {
                    val details = mockk<ColorDetails>(relaxed = true)
                    val command = ColorDetailsCommand.SetSeedDetails(details)
                    commandStore.channel.send(command)
                }

                expectMostRecentItem() shouldNotBe null
            }
        }

    @Test
    fun `when 'select exact color' is invoked, then SUT sends appropriate event to the event store`() =
        runTest(testDispatcher) {
            val seedColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            coEvery { colorRepository.getColorDetails(seedColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns seedColor
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                }
                Result.success(details)
            }
            createSut(
                createData = createDataReal,
            )

            run {
                val command = ColorDetailsCommand.SetSeedColor(seedColor)
                commandStore.channel.send(command)
            }
            sut.data.selectExactColor()

            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventStoreMock.send(expectedEvent)
            }
        }

    @Test
    fun `when 'select seed color' is invoked, then SUT sends appropriate event to the event store`() =
        runTest(testDispatcher) {
            // GIVEN
            val seedColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            coEvery { colorRepository.getColorDetails(color = seedColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns seedColor
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
                val command = ColorDetailsCommand.SetSeedColor(seedColor)
                commandStore.channel.send(command)
            }
            run {
                val command = ColorDetailsCommand.SelectColor(ColorRole.Exact)
                commandStore.channel.send(command)
            }
            sut.data.selectSeedColor()

            // THEN
            coVerify {
                val expectedEvent = ColorDetailsEvent.ColorSelected(
                    color = seedColor,
                    colorRole = ColorRole.Seed,
                )
                eventStoreMock.send(expectedEvent)
            }
        }

    /**
     * Tests that
     * 1. SUT "recalls" the correct 'exact' color `cE` for the currently set 'seed' color `cS`
     * when a [ColorDetailsCommand.SelectColor] command is issued.
     * 2. SUT produces correct [ColorDetailsData] with [ColorDetailsData.colorRoleData] being a [ColorRoleData.Exact].
     */
    @Test
    fun `when 'select color' with the color role 'exact' is emitted, then SUT emits correct data`() =
        runTest(testDispatcher) {
            val seedColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            coEvery { colorRepository.getColorDetails(color = seedColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns seedColor
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
                    val command = ColorDetailsCommand.SetSeedColor(seedColor)
                    commandStore.channel.send(command)
                }
                run {
                    val command = ColorDetailsCommand.SelectColor(ColorRole.Exact)
                    commandStore.channel.send(command)
                }

                val data = expectMostRecentItem().shouldBeInstanceOf<DataState.Ready>().data
                data.colorRoleData should beOfType<ColorRoleData.Exact>()
            }
        }

    @Test
    fun `when 'set seed color' command is emitted, then SUT cancels the ongoing 'try again' action`() =
        runTest(testDispatcher) {
            val color1 = Color.Hex(0x0)
            val color2 = Color.Hex(0x1)
            coEvery { colorRepository.getColorDetails(color = color1) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
            val detailsOfColor2 = mockk<ColorDetails>(relaxed = true) {
                every { this@mockk.color } returns color2
                every { exact.color } returns Color.Hex(0x3)
                every { matchesExact } returns false
            }
            coEvery { colorRepository.getColorDetails(color = color2) } returns run {
                Result.success(detailsOfColor2)
            }
            createSut()

            run {
                val command = ColorDetailsCommand.SetSeedColor(color1)
                commandStore.channel.send(command)
            }
            val getColorDetailsDeferred = CompletableDeferred<Result<ColorDetails>>()
            coEvery { colorRepository.getColorDetails(color = color1) } coAnswers {
                getColorDetailsDeferred.await()
            }
            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>()
                .error.tryAgain.invoke()
            run {
                val command = ColorDetailsCommand.SetSeedColor(color2)
                commandStore.channel.send(command)
            }
            // complete Deferred in case SUT hasn't canceled the job
            val detailsOfColor1 = mockk<ColorDetails> {
                every { this@mockk.color } returns color1
                every { exact.color } returns Color.Hex(0x2)
            }
            run {
                val value = Result.success(detailsOfColor1)
                getColorDetailsDeferred.complete(value)
            }

            // verify that component that is called deep inside 'process(command)' is only called once:
            // the 'try again' coroutine is canceled thus it never goes deep enough to trigger this component.
            coVerify(exactly = 0) {
                createDataMock(
                    details = detailsOfColor1,
                    colorRole = any(),
                    selectSeedColor = any(),
                    selectExactColor = any(),
                    getSeedColor = any(),
                )
            }
            coVerify(exactly = 1) {
                createDataMock(
                    details = detailsOfColor2,
                    colorRole = any(),
                    selectSeedColor = any(),
                    selectExactColor = any(),
                    getSeedColor = any(),
                )
            }
        }

    @Test
    fun `when 'try again' is invoked, then SUT uses color from the command that has failed and is being retried`() =
        runTest(testDispatcher) {
            fun mockGetColorDetailsReturnsSuccess() {
                val fetchedDetails: ColorDetails = mockk(relaxed = true) {
                    every { this@mockk.color } returns Color.Hex(0x000000) // doesn't matter
                    every { exact.color } returns Color.Hex(0x000000) // doesn't matter
                }
                coEvery { colorRepository.getColorDetails(color = any()) } returns
                        Result.success(value = fetchedDetails)
            }

            mockGetColorDetailsReturnsSuccess()
            createSut()

            val color1 = Color.Hex(0x0)
            run {
                val command = ColorDetailsCommand.SetSeedColor(color = color1)
                commandStore.channel.send(command)
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
                val command = ColorDetailsCommand.SetSeedColor(color = color2)
                commandStore.channel.send(command)
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
    fun `when 'set seed details' command is emitted, then SUT emits 'Ready' state`() =
        runTest(testDispatcher) {
            createSut()

            run {
                val details: ColorDetails = mockk(relaxed = true)
                val command = ColorDetailsCommand.SetSeedDetails(details)
                commandStore.channel.send(command)
            }

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    /**
     * Tests that [ColorDetailsViewModel] adds new [ColorDetails] objects in the end of the set,
     * thus keeping it ordered chronologically and enabling all private methods to iterate it correctly.
     */
    @Test
    fun `when pairs of 'select exact'-'select seed' actions are invoked multiple times, then SUT emits correct data`() =
        runTest(testDispatcher) {
            // GIVEN
            val seedColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            coEvery { colorRepository.getColorDetails(color = seedColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns seedColor
                    every { colorName } returns "seed"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns false
                    every { this@mockk.hashCode() } returns -1066639853 // custom hashcode to break the insertion order if SUT is broken
                }
                Result.success(details)
            }
            coEvery { colorRepository.getColorDetails(color = exactColor) } returns run {
                val details = mockk<ColorDetails>(relaxed = true) {
                    every { this@mockk.color } returns exactColor
                    every { colorName } returns "exact"
                    every { exact.color } returns exactColor
                    every { matchesExact } returns true
                    every { this@mockk.hashCode() } returns 1802741316 // custom hashcode to break the insertion order if SUT is broken
                }
                Result.success(details)
            }
            createSut(
                eventStore = eventStoreReal,
                createData = createDataReal,
            )

            // WHEN
            suspend fun emitSelectColorCommand(event: ColorDetailsEvent) {
                event.shouldBeInstanceOf<ColorDetailsEvent.ColorSelected>()
                val command = ColorDetailsCommand.SelectColor(colorRole = event.colorRole)
                commandStore.channel.send(command)
            }
            run fetchDataForSeedColor@{
                val command = ColorDetailsCommand.SetSeedColor(color = seedColor)
                commandStore.channel.send(command)
            }

            val event1 = async { eventStoreReal.eventFlow.first() }
            sut.data.selectExactColor()
            emitSelectColorCommand(event = event1.await())

            val event2 = async { eventStoreReal.eventFlow.first() }
            sut.data.selectSeedColor()
            emitSelectColorCommand(event = event2.await())

            val event3 = async { eventStoreReal.eventFlow.first() }
            sut.data.selectExactColor()
            emitSelectColorCommand(event = event3.await())

            val event4 = async { eventStoreReal.eventFlow.first() }
            sut.data.selectSeedColor()
            emitSelectColorCommand(event = event4.await())

            // THEN
            sut.data.colorName shouldBe "seed"
        }

    fun createSut(
        eventStore: ColorDetailsEventStore = eventStoreMock,
        createData: CreateColorDetailsDataUseCase = createDataMock,
        coroutineDispatcher: CoroutineDispatcher = testDispatcher,
    ) =
        ColorDetailsViewModel(
            coroutineScope = CoroutineScope(context = coroutineDispatcher),
            commandStore = commandStore,
            eventStore = eventStore,
            colorRepository = colorRepository,
            createData = createData,
            createSubjectColorData = createSubjectColorData,
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

    val ColorDetailsData.selectSeedColor: () -> Unit
        get() = this.colorRoleData.shouldBeInstanceOf<ColorRoleData.Exact>().selectSeedColor

    val ColorDetailsData.selectExactColor: () -> Unit
        get() = this.colorRoleData.shouldBeInstanceOf<ColorRoleData.Seed>().selectExactColor
}