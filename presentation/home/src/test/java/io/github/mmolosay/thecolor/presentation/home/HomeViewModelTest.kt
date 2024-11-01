package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorComparator
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionBuilder
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateColorDataUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.DoesColorBelongToSessionUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import javax.inject.Provider
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val colorInputMediator: ColorInputMediator = mockk(relaxed = true)
    val colorCenterViewModel: ColorCenterViewModel = mockk(relaxed = true)
    val colorCenterViewModelFactory: ColorCenterViewModel.Factory = mockk {
        every {
            create(
                coroutineScope = any(),
                colorDetailsCommandProvider = any(),
                colorDetailsEventStore = any(),
                colorSchemeCommandProvider = any(),
            )
        } returns colorCenterViewModel
    }
    val colorInputColorStore: ColorInputColorStore = mockk()
    val colorInputEventStore: ColorInputEventStore = mockk()
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
    val colorSchemeCommandStore: ColorSchemeCommandStore = mockk {
        coEvery { issue(command = any()) } just runs
    }
    val colorSchemeCommandStoreProvider: Provider<ColorSchemeCommandStore> = mockk {
        every { get() } returns colorSchemeCommandStore
    }
    val createColorData: CreateColorDataUseCase = mockk()

    // real implementation, there's no need to have mock for color comparison
    val doesColorBelongToSession = DoesColorBelongToSessionUseCase(
        colorComparator = ColorComparator(
            colorConverter = ColorConverter(),
        ),
    )

    lateinit var sut: HomeViewModel

    @Test
    fun `given color from Color Input is not 'null', when SUT is created, then data has 'CanProceed Yes'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `given color from color input is 'null', when SUT is created, then data has 'CanProceed No'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has 'CanProceed Yes'`() {
        // from other tests, we know that this will produce 'CanProceed.No' in data
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = mockk<Color>()

        data.canProceed should beOfType<CanProceed.Yes>()
    }


    @Test
    fun `when receiving a 'null' color from Color Input, then data has 'CanProceed No'`() {
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = null

        data.canProceed should beOfType<CanProceed.No>()
    }

    /**
     * - GIVEN that 'proceed' was already invoked and there's a color in Color Center
     * - WHEN
     *  1. [ColorDetailsEvent.ColorSelected] for "exact" color is emitted (e.g. due to user clicking on "go to exact" button)
     *  2. the event is handled and "exact" color is sent to [ColorInputMediator]
     *  3. the update of the [ColorInputColorStore] received and processed. SUT checks whether the
     *  new color (which is "exact" color) belongs to the ongoing color session.
     * - THEN "exact" color is confirmed to belong to the ongoing color session and it (session)
     * is not ended, thus [HomeData.proceedResult] is not set to `null`.
     */
    @Test
    fun `when receiving a not-null RGB color from Color Input due to 'ExactColorSelected', then session is not finished`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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
            run emitExactColor@{
                colorFlow.emit(exactColor)
            }

            // indicator of not finished session
            data.proceedResult shouldNotBe null
        }

    @Test
    fun `invoking 'proceed' action issues 'FetchData' command to Color Details and Color Scheme`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { createColorData(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

        coVerify {
            colorDetailsCommandStore.issue(command = any<ColorDetailsCommand.FetchData>())
            colorSchemeCommandStore.issue(command = any<ColorSchemeCommand.FetchData>())
        }
    }

    @Test
    fun `invoking 'proceed' action updates 'proceedResult'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        val colorData: ProceedResult.Success.ColorData = mockk()
        every { createColorData(color = any()) } returns colorData
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

        val proceedResultAsSuccess = data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
        proceedResultAsSuccess.colorData shouldBe colorData
    }

    /**
     * - GIVEN that there's some color in [ColorInputColorStore] and SUT is created
     * - WHEN [ColorInputEvent.Submit] with valid color is sent
     * - THEN [ColorDetailsCommand.FetchData] is emitted from [colorDetailsCommandStore].
     */
    @Test
    fun `when receiving a 'Sumbit' event from Color Input with 'Valid' color input state, then 'proceed' action is invoked, thus 'FetchData' command is issued to Color Details and Color Scheme`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
            every { createColorData(color = any()) } returns mockk()
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
                onConsumed = {},
            )
            eventsFlow.emit(event)

            coVerify {
                colorDetailsCommandStore.issue(command = any<ColorDetailsCommand.FetchData>())
                colorSchemeCommandStore.issue(command = any<ColorSchemeCommand.FetchData>())
            }
        }

    /**
     * - GIVEN that there's some color in [ColorInputColorStore] and SUT is created
     * - WHEN [ColorInputEvent.Submit] with valid color is sent
     * - THEN [data] is updated with [ProceedResult.Success].
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then 'proceed' action is invoked, thus 'proceedResult' is set to 'Success'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
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
     * - GIVEN that there's some color in [ColorInputColorStore] and SUT is created
     * - WHEN [ColorInputEvent.Submit] with invalid color is sent
     * - THEN [data] is updated with [ProceedResult.InvalidSubmittedColor].
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Invalid' color input state, then 'proceed' action is not invoked, thus 'proceedResult' is set to 'InvalidSubmittedColor'`() =
        runTest(mainDispatcherRule.testDispatcher) {
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
     * - GIVEN that there's some color in [ColorInputColorStore] and SUT is created
     * - WHEN [ColorInputEvent.Submit] with valid color is sent
     * - THEN [ColorInputEvent.Submit.onConsumed] is invoked with `true` value for `wasAccepted` parameter.
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then submission is reported as accepted'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorDetailsEventStore.eventFlow } returns emptyFlow()
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
     * - GIVEN that there's some color in [ColorInputColorStore] and SUT is created
     * - WHEN [ColorInputEvent.Submit] with invalid color is sent
     * - THEN [ColorInputEvent.Submit.onConsumed] is invoked with `false` value for `wasAccepted` parameter.
     */
    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Invalid' color input state, then submission is reported as not accepted'`() =
        runTest(mainDispatcherRule.testDispatcher) {
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
     * - GIVEN that SUT has [data] with [ProceedResult.InvalidSubmittedColor]
     * - WHEN [ProceedResult.InvalidSubmittedColor.discard] is invoked
     * - THEN [data] is updated and `proceedResult` is set to `null`.
     */
    @Test
    fun `given that 'ProceedResult InvalidSubmittedColor' is set in data, when its 'discard' callback is invoked, then proceed result value is set to 'null'`() =
        runTest(mainDispatcherRule.testDispatcher) {
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

            // we know from other tests that it would be 'InvalidSubmittedColor'
            data.proceedResult.shouldBeInstanceOf<ProceedResult.InvalidSubmittedColor>().discard.invoke()

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving a 'ExactColorSelected' event from Color Details, 'set color and proceed' action is invoked, thus new color is sent to color input mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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
    fun `when receiving a 'ExactColorSelected' event from Color Details, 'proceed' action is invoked, thus 'FetchData' command is issued to Color Details and Color Scheme`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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
                colorDetailsCommandStore.issue(command = any<ColorDetailsCommand.FetchData>())
                colorSchemeCommandStore.issue(command = any<ColorSchemeCommand.FetchData>())
            }
        }

    @Test
    fun `when receiving a 'ExactColorSelected' event from Color Details, then 'proceedResult' is not cleared`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val colorFlow = MutableStateFlow(value = mockk<Color>())
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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
            run emitExactColor@{
                colorFlow.emit(exactColor)
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
    fun `when receiving a 'ExactColorSelected' event from Color Details, 'proceed' action is invoked, thus 'proceedResult' is updated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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

    @Test
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() {
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { createColorData(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
        colorFlow.value = null

        data.proceedResult shouldBe null
    }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() {
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { createColorData(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
        colorFlow.value = Color.Hex(0x1)

        data.proceedResult shouldBe null
    }

    @Test
    fun `when 'proceed' is invoked for second color, then 'ColorCenterViewModel' is recreated to reset its state`() {
        val firstColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow(firstColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { createColorData(color = any()) } returns mockk()
        createSut()
        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed() // proceed with first color
        val secondColor = Color.Hex(0x1)
        colorFlow.value = secondColor
        clearMocks(
            colorDetailsCommandStoreProvider,
            colorDetailsEventStoreProvider,
            colorSchemeCommandStoreProvider,
            colorCenterViewModelFactory,
            answers = false,
            recordedCalls = true, // only clear recorded calls
            childMocks = false,
            verificationMarks = false,
            exclusionRules = false,
        )

        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed() // proceed with second color

        coVerify(exactly = 1) {
            colorDetailsCommandStoreProvider.get()
            colorDetailsEventStoreProvider.get()
            colorSchemeCommandStoreProvider.get()
            colorCenterViewModelFactory.create(
                coroutineScope = any(),
                colorDetailsCommandProvider = any(),
                colorDetailsEventStore = any(),
                colorSchemeCommandProvider = any(),
            )
        }
    }

    @Test
    fun `when 'proceed' is invoked and Color Input is cleared before 'DataFetched' event arrives, then no exception is thrown`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialColor = Color.Hex(0x0)
            val colorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorFlow
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
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

    fun createSut() =
        HomeViewModel(
            colorInputMediatorFactory = { _ -> colorInputMediator },
            colorInputViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            colorPreviewViewModelFactory = { _, _ -> mockk(relaxed = true) },
            colorCenterViewModelFactory = colorCenterViewModelFactory,
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorDetailsCommandStoreProvider = colorDetailsCommandStoreProvider,
            colorDetailsEventStoreProvider = colorDetailsEventStoreProvider,
            colorSchemeCommandStoreProvider = colorSchemeCommandStoreProvider,
            createColorData = createColorData,
            colorCenterSessionBuilder = ColorCenterSessionBuilder(),
            doesColorBelongToSession = doesColorBelongToSession,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value
}