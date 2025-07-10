package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.viewModelScope
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorComparator
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsStore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionBuilder
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateColorDataUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.DoesColorBelongToSessionUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModelDiModule
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ProceedExecutor
import io.github.mmolosay.thecolor.presentation.home.viewmodel.components
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEventStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Provider
import kotlin.time.Duration.Companion.seconds
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.model.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class HomeViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val colorInputMediator: ColorInputMediator = mockk(relaxed = true)
    val colorInputColorStore: ColorInputColorStore = spyk() // for actual impl of 'wouldEmitIfSet()'
    val colorInputEventStore: ColorInputEventStore = mockk()

    val colorProcessedConfirmationChannelForColorPreviewReal: Channel<Color?> =
        HomeViewModelDiModule.provideColorProcessedConfirmationChannelForColorPreview()
    val colorProcessedConfirmationChannelForColorPreviewMock: Channel<Color?> =
        mockk(relaxed = true)

    val colorDetailsViewModel: ColorDetailsViewModel = mockk(relaxed = true)
    val colorDetailsCommandStore: ColorDetailsCommandStore = mockk {
        coEvery { issue(command = any()) } just runs
    }
    val colorDetailsCommandStoreProvider: Provider<ColorDetailsCommandStore> = mockk {
        every { get() } returns colorDetailsCommandStore
    }
    val colorDetailsEventStore: ColorDetailsEventStore = mockk()
    val colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore> = mockk {
        every { get() } returns colorDetailsEventStore
    }

    val colorSchemeViewModel: ColorSchemeViewModel = mockk(relaxed = true)
    val colorSchemeCommandStore: ColorSchemeCommandStore = mockk {
        coEvery { issue(command = any()) } just runs
    }
    val colorSchemeCommandStoreProvider: Provider<ColorSchemeCommandStore> = mockk {
        every { get() } returns colorSchemeCommandStore
    }
    val colorSchemeEventStore: ColorSchemeEventStore = mockk()
    val colorSchemeEventStoreProvider: Provider<ColorSchemeEventStore> = mockk {
        every { get() } returns colorSchemeEventStore
    }

    val colorCenterViewModel: ColorCenterViewModel = mockk(relaxed = true)
    val colorCenterComponentsStore = ColorCenterComponentsStore(
        viewModelScope = CoroutineScope(testDispatcher),
        colorDetailsCommandStoreProvider = colorDetailsCommandStoreProvider,
        colorDetailsEventStoreProvider = colorDetailsEventStoreProvider,
        colorDetailsViewModelFactory = { _, _, _ -> colorDetailsViewModel },
        colorSchemeCommandStoreProvider = colorSchemeCommandStoreProvider,
        colorSchemeEventStoreProvider = colorSchemeEventStoreProvider,
        colorSchemeViewModelFactory = { _, _, _ -> colorSchemeViewModel },
        colorCenterViewModelFactory = { _, _, _ -> colorCenterViewModel },
    )

    val proceedExecutor: ProceedExecutor = mockk {
        coEvery { this@mockk.invoke(color = any(), colorRole = any()) } just runs
    }
    val proceedExecutorFactory: ProceedExecutor.Factory = mockk {
        every { create(
            colorDetailsCommandStore = any(),
            colorSchemeCommandStore = any(),
        ) } returns proceedExecutor
    }

    val createColorData: CreateColorDataUseCase = mockk()

    // real implementation, there's no need to have mock for color comparison
    val doesColorBelongToSession = DoesColorBelongToSessionUseCase(
        colorComparator = ColorComparator(
            colorConverter = ColorConverter(),
        ),
    )

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        val disabled = ResumeFromLastSearchedColorOnStartup(enabled = false)
        every { flowOfResumeFromLastSearchedColorOnStartup() } returns flowOf(disabled)
    }
    val lastSearchedColorRepository: LastSearchedColorRepository = mockk {
        coEvery { setLastSearchedColor(color = any()) } just runs
    }
    val colorFactory: ColorFactory = mockk()

    lateinit var sut: HomeViewModel

    @Test
    fun `given color from Color Input is not 'null', when SUT is created, then data has 'CanProceed Yes'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { colorSchemeEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `given color from color input is 'null', when SUT is created, then data has 'CanProceed No'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { colorSchemeEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has 'CanProceed Yes'`() =
        runTest(testDispatcher) {
            // from other tests, we know that this will produce 'CanProceed.No' in data
            val colorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            createSut()

            val color = mockk<Color>()
            run emitColorFromColorInput@{
                colorFlow.emit(color)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(color)
            }

            data.canProceed should beOfType<CanProceed.Yes>()
        }

    @Test
    fun `when receiving a 'null' color from Color Input, then data has 'CanProceed No'`() =
        runTest(testDispatcher) {
            val colorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            createSut()

            val color: Color? = null
            run emitColorFromColorInput@{
                colorFlow.emit(color)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(color)
            }

            data.canProceed should beOfType<CanProceed.No>()
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. 'proceed' was already invoked and there's a color in Color Center
     *
     * WHEN
     * 1. [ColorDetailsEvent.ColorSelected] for "exact" color is emitted (e.g. due to user clicking on "go to exact" button)
     * 2. the event is handled and "exact" color is sent to [ColorInputMediator]
     * 3. the update of the [ColorInputColorStore] received and processed. SUT checks whether the
     * new color (which is "exact" color) belongs to the ongoing color session.
     *
     * THEN
     * "exact" color is confirmed to belong to the ongoing color session and it (session)
     * doesn't get finished, thus [HomeData.proceedResult] is not set to `null`.
     */
    @Test
    fun `when receiving a not-null RGB color from Color Input due to 'ExactColorSelected', then session is not finished`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            val exactColor = Color.Rgb(1, 2, 3)
            run emitDataFetchedEvent@{
                val exactColorButHex = Color.Hex(0x010203)
                val event = ColorDetailsEvent.DataFetched(
                    domainDetails = mockk(relaxed = true) {
                        every { exact } returns mockk {
                            every { color } returns exactColorButHex
                        }
                    },
                )
                eventsFlow.emit(event)
            }
            // clicking "Go to exact color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventsFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                colorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }

            // indicator of not finished session
            data.proceedResult shouldNotBe null
        }


    @Test
    fun `when receiving any color from Color Input, then 'proceed' is not executed until Color Preview confirms that it has processed this color as well`() =
        runTest(testDispatcher) {
            val colorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            coEvery {
                // relaxed suspend functions (such as receive()) will return a stub immediately.
                // Channel.receiveAllUntil() contains forever loop, which will result in OOM if receive() doesn't suspend
                colorProcessedConfirmationChannelForColorPreviewMock.receive()
            } coAnswers {
                suspendCancellableCoroutine {}
            }
            createSut(
                colorProcessedConfirmationChannelForColorPreview = colorProcessedConfirmationChannelForColorPreviewMock,
            )

            val color = mockk<Color>()
            run emitExactColorFromColorInput@{
                colorFlow.emit(color)
            }

            coVerify(exactly = 0) {
                colorProcessedConfirmationChannelForColorPreviewMock.send(color)
            }
            coVerify(exactly = 0) {
                proceedExecutor.invoke(color = color, colorRole = any())
            }
            sut.viewModelScope.cancel()
        }

    @Test
    fun `invoking 'proceed' action from UI invokes 'proceed executor'`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            coVerify {
                proceedExecutor.invoke(color = any(), colorRole = any())
            }
        }

    @Test
    fun `invoking 'proceed' action updates 'proceedResult'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { colorSchemeEventStore.eventFlow } returns emptyFlow()
        val colorData: ProceedResult.Success.ColorData = mockk()
        every { createColorData(color = any()) } returns colorData
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

        val proceedResultAsSuccess = data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
        proceedResultAsSuccess.colorData shouldBe colorData
    }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
     *
     * WHEN
     * [ColorInputEvent.Submit] with valid color is sent
     *
     * THEN
     * [proceedExecutor] is invoked.
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then 'proceed executor' is invoked`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
                onConsumed = {},
            )
            eventsFlow.emit(event)

            coVerify {
                proceedExecutor.invoke(color = any(), colorRole = any())
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
     *
     * WHEN
     * [ColorInputEvent.Submit] with valid color is sent
     *
     * THEN
     * [data] is updated with [ProceedResult.Success].
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then 'proceed' action is invoked, thus 'proceedResult' is set to 'Success'`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
                onConsumed = {},
            )
            eventsFlow.emit(event)

            val proceedResultAsSuccess =
                data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
     *
     * WHEN
     * [ColorInputEvent.Submit] with invalid color is sent
     *
     * THEN
     * [data] is updated with [ProceedResult.InvalidSubmittedColor].
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Invalid' color input state, then 'proceed' action is not invoked, thus 'proceedResult' is set to 'InvalidSubmittedColor'`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = mockk<ColorInputState.Invalid>(),
                onConsumed = {},
            )
            eventsFlow.emit(event)

            data.proceedResult should beOfType<ProceedResult.InvalidSubmittedColor>()
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
     *
     * WHEN
     * [ColorInputEvent.Submit] with valid color is sent
     *
     * THEN
     * [ColorInputEvent.Submit.onConsumed] is invoked with `true` value for `wasAccepted` parameter.
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then submission is reported as accepted`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
                onConsumed = mockk(relaxed = true),
            )
            eventsFlow.emit(event)

            verify {
                event.onConsumed.invoke(wasAccepted = true)
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
     *
     * WHEN
     * [ColorInputEvent.Submit] with invalid color is sent
     *
     * THEN
     * [ColorInputEvent.Submit.onConsumed] is invoked with `false` value for `wasAccepted` parameter.
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Invalid' color input state, then submission is reported as not accepted`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = mockk<ColorInputState.Invalid>(),
                onConsumed = mockk(relaxed = true),
            )
            eventsFlow.emit(event)

            verify {
                event.onConsumed.invoke(wasAccepted = false)
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. [sut] has [data] with [ProceedResult.InvalidSubmittedColor]
     *
     * WHEN
     * [ProceedResult.InvalidSubmittedColor.discard] is invoked
     *
     * THEN
     * [data] is updated and `proceedResult` is set to `null`.
     */
    @Test
    fun `given that 'ProceedResult InvalidSubmittedColor' is set in data, when its 'discard' callback is invoked, then proceed result value is set to 'null'`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = mockk<ColorInputState.Invalid>(),
                onConsumed = {},
            )
            eventsFlow.emit(event)

            // we know from other tests that it would be 'InvalidSubmittedColor'
            data.proceedResult.shouldBeInstanceOf<ProceedResult.InvalidSubmittedColor>().discard.invoke()

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, 'set color and proceed' action is invoked, thus new color is sent to color input mediator`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event = ColorDetailsEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            eventsFlow.emit(event)

            coVerify {
                colorInputMediator.send(color = Color.Hex(0x123456), from = null)
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, 'proceed executor' is invoked`() =
        runTest(testDispatcher) {
            val colorInputColorFlow = MutableStateFlow(value = mockk<Color>())
            coEvery {
                colorInputMediator.send(color = any(), from = any())
            } coAnswers  {
                val sentColor = firstArg<Color>()
                colorInputColorFlow.emit(sentColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(sentColor)
            }
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            val exactColor = Color.Hex(0x123456)
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { exact } returns mockk {
                        every { color } returns exactColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }

            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventsFlow.emit(event)
            }

            coVerify {
                proceedExecutor.invoke(color = exactColor, colorRole = ColorRole.Exact)
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then 'proceedResult' is not cleared`() =
        runTest(testDispatcher) {
            val colorFlow = MutableStateFlow(value = mockk<Color>())
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            val exactColor = Color.Hex(0x123456)
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { exact } returns mockk {
                        every { color } returns exactColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }

            val dataEmissions = mutableListOf<HomeData>()
            val dataEmissionsCollectionJob = launch {
                sut.dataFlow
                    .take(2)
                    .toList(destination = dataEmissions)
            }
            // clicking "Go to exact color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventsFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                colorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val event = ColorDetailsEvent.DataFetched(
                    domainDetails = mockk(relaxed = true),
                )
                eventsFlow.emit(event)
            }

            // the list is limited by 2 elements:
            // 1st: should NOT be emitted
            // 2nd: follows the 1st one and should be emitted
            dataEmissions.size shouldBe 1 // only the 2nd, expected emission
            dataEmissions.single() shouldBe data // this 2nd data is the current one
            data.proceedResult shouldNotBe null // the focus of this test
            dataEmissionsCollectionJob.cancel()
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, 'proceed' action is invoked, thus 'proceedResult' is updated`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event = ColorDetailsEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            eventsFlow.emit(event)

            val proceedResultAsSuccess =
                data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. [sut] is proceeded with some color
     *
     * WHEN
     * 1. receiving two same [ColorDetailsEvent.ColorSelected] events with color X in a quick succession
     * 2. then receiving a different [ColorDetailsEvent.ColorSelected] event with color Y
     *
     * THEN
     *  nothing breaks: [proceedExecutor] is invoked for both color X and then for color Y.
     */
    @Test
    fun `when receiving two same 'ColorSelected' events from Color Details rapidly, and then receiving different 'ColorSelected' event, then 'proceed' action is invoked normally`() =
        runTest(testDispatcher, timeout = 5.seconds) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color>(value = initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            val exactColor = Color.Hex(0x1)
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                    every { exact } returns mockk {
                        every { color } returns exactColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }

            // clicking "Go to exact color"
            run emitExactColorSelectedEvents@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                launch {
                    eventsFlow.emit(event) // 1st time
                    eventsFlow.emit(event) // 2nd time
                }
            }
            run emitExactColorFromColorInput@{
                colorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns exactColor
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }
            // clicking "Go back to initial color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = initialColor,
                    colorRole = ColorRole.Initial,
                )
                eventsFlow.emit(event)
            }
            run emitInitialColorFromColorInput@{
                colorFlow.emit(initialColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(initialColor)
            }
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }

            coVerifyOrder {
                proceedExecutor.invoke(color = exactColor, colorRole = ColorRole.Exact)
                proceedExecutor.invoke(color = initialColor, colorRole = ColorRole.Initial)
            }
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then 'color scheme selected swatch data' is set`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorSchemeEvent>()
            every { colorSchemeEventStore.eventFlow } returns eventsFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event: ColorSchemeEvent.SwatchSelected = mockk(relaxed = true)
            eventsFlow.emit(event)

            data.colorSchemeSelectedSwatchData shouldNotBe null // assuming initially value is 'null'
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then command is sent to 'selected swatch color details'`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorSchemeEvent>()
            every { colorSchemeEventStore.eventFlow } returns eventsFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event: ColorSchemeEvent.SwatchSelected = mockk(relaxed = true)
            eventsFlow.emit(event)

            coEvery {
                val commandStore = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsCommandStore
                    .shouldNotBeNull()
                commandStore.issue(command = any<ColorDetailsCommand.SetColorDetails>())
            }
        }

    @Test
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            colorFlow.emit(null)

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            colorFlow.emit(Color.Hex(0x1))

            data.proceedResult shouldBe null
        }

    @Test
    fun `when 'proceed' is invoked and Color Input is cleared before 'DataFetched' event arrives, then no exception is thrown`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            colorFlow.emit(null)
            val event = run eventForInitialColor@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                    every { exact } returns mockk {
                        every { color } returns mockk()
                    }
                }
                ColorDetailsEvent.DataFetched(domainDetails)
            }

            shouldNotThrowAny {
                eventsFlow.emit(event)
            }
        }

    @Test
    fun `when 'proceed' is invoked for second time before 'DataFetched' event arrives, then no exception is thrown`() =
        runTest(testDispatcher) {
            val currentColor = Color.Hex(0x0) // name 'color' conflicts with fields of DomainColorDetails
            val colorFlow = MutableStateFlow<Color?>(currentColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                every { color } returns currentColor
                every { exact } returns mockk {
                    every { color } returns mockk()
                }
            }
            // emit event for first invocation of 'proceed'
            kotlin.run {
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }
            // emit event for second invocation of 'proceed'
            shouldNotThrowAny {
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }
        }

    /**
     * GIVEN
     * 1. "should resume from last searched color on app startup" is enabled
     * 2. last searched color is successfully retrieved
     *
     * WHEN
     * [sut] is created
     *
     * THEN
     * last searched color is proceeded with and [data] is updated with [ProceedResult.Success].
     */
    @Test
    fun `when 'resume from last searched color on app startup' is enabled, then 'proceed' action is invoked for this color, thus 'proceedResult' is set to 'Success'`() =
        runTest(testDispatcher) {
            every {
                userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup()
            } returns kotlin.run {
                val enabled = ResumeFromLastSearchedColorOnStartup(enabled = true)
                flowOf(enabled)
            }
            val lastSearchedColor: Color = Color.Hex(0x1A803F)
            coEvery { lastSearchedColorRepository.getLastSearchedColor() } returns lastSearchedColor
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            coEvery {
                colorInputMediator.send(color = any(), from = any())
            } coAnswers  {
                val sentColor = firstArg<Color>()
                colorInputColorFlow.emit(sentColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(sentColor)
            }
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = lastSearchedColor) } returns colorData

            createSut()

            val proceedResultAsSuccess =
                data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    @Test
    fun `only 'seed' color of a Color Center session is persisted as a last searched color`() =
        runTest(testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            val exactColor = Color.Hex(0x123456)
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { exact } returns mockk {
                        every { color } returns exactColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                eventsFlow.emit(event)
            }

            // clicking "Go to exact color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                eventsFlow.emit(event)
            }
            run emitExactColor@{
                colorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val event = ColorDetailsEvent.DataFetched(
                    domainDetails = mockk(relaxed = true),
                )
                eventsFlow.emit(event)
            }

            coVerify(exactly = 1) {
                lastSearchedColorRepository.setLastSearchedColor(color = initialColor)
            }
        }

    @Test
    fun `invoking 'randomize color' sends new randomized color to color input mediator`() =
        runTest(testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val randomColor: Color.Hex = mockk()
            every { colorFactory.random() } returns randomColor
            val featureValue = DomainAutoProceedWithRandomizedColors(enabled = false)
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors() } returns MutableStateFlow(featureValue)
            createSut()

            data.randomizeColor()

            coVerify(exactly = 1) {
                colorInputMediator.send(color = randomColor, from = null)
            }
        }

    @Test
    fun `invoking 'randomize color' proceeds with it if 'auto proceed with randomized colors' feature is enabled`() =
        runTest(testDispatcher) {
            val colorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val randomColor: Color.Hex = mockk()
            every { colorFactory.random() } returns randomColor
            val featureValue = DomainAutoProceedWithRandomizedColors(enabled = true)
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors() } returns MutableStateFlow(featureValue)
            every { createColorData(color = any()) } returns mockk()
            createSut()

            data.randomizeColor()
            run emitColorFromColorInput@{
                colorFlow.emit(randomColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(randomColor)
            }

            coVerify(exactly = 1) {
                proceedExecutor.invoke(color = randomColor, colorRole = null)
            }
        }

    /*
     * It's hard to unit test the difference that "is data being updated" flag makes.
     * It requires to control execution of `randomizeColor()` method on suspension points.
     */
    @Test
    fun `invoking 'randomize color' starts data transaction and sets 'is data being updated' flag first to true and then to false`() =
        runTest(testDispatcher) {
            val colorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { colorSchemeEventStore.eventFlow } returns emptyFlow()
            val randomColor: Color.Hex = mockk()
            every { colorFactory.random() } returns randomColor
            val featureValue = DomainAutoProceedWithRandomizedColors(enabled = true)
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors() } returns MutableStateFlow(
                featureValue
            )
            every { createColorData(color = any()) } returns mockk()
            createSut()

            val listOfIsDataBeingUpdatedValues = mutableListOf<Boolean>()
            val isDataBeingUpdatedCollectionJob = launch {
                sut.flowOfIsDataBeingUpdated
                    .drop(1) // replayed value
                    .take(2) // 1st update to true, 2nd update to false
                    .toList(listOfIsDataBeingUpdatedValues)
            }

            data.randomizeColor()
            run emitColorFromColorInput@{
                colorFlow.emit(randomColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(randomColor)
            }

            isDataBeingUpdatedCollectionJob.join()
            listOfIsDataBeingUpdatedValues shouldBe listOf(true, false)
        }

    fun createSut(
        colorProcessedConfirmationChannelForColorPreview: Channel<Color?> = colorProcessedConfirmationChannelForColorPreviewReal,
    ) =
        HomeViewModel(
            colorInputMediatorFactory = { _ -> colorInputMediator },
            colorInputViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorProcessedConfirmationChannelForColorPreview = colorProcessedConfirmationChannelForColorPreview,
            colorPreviewViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            colorCenterComponentsStoreFactory = { _ -> colorCenterComponentsStore },
            proceedExecutorFactory = proceedExecutorFactory,
            createColorData = createColorData,
            colorCenterSessionBuilder = ColorCenterSessionBuilder(),
            doesColorBelongToSession = doesColorBelongToSession,
            userPreferencesRepository = userPreferencesRepository,
            lastSearchedColorRepository = lastSearchedColorRepository,
            colorFactory = colorFactory,
            defaultDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value
}