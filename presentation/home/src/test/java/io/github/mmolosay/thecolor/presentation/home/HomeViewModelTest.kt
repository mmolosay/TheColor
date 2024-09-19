package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.details.ColorRole
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
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
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val colorInputMediator: ColorInputMediator = mockk(relaxed = true)
    val colorInputColorStore: ColorInputColorStore = mockk()
    val colorInputEventStore: ColorInputEventStore = mockk()
    val colorDetailsCommandStore: ColorDetailsCommandStore = mockk {
        coEvery { issue(command = any()) } just runs
    }
    val colorDetailsEventStore: ColorDetailsEventStore = mockk()
    val colorSchemeCommandStore: ColorSchemeCommandStore = mockk {
        coEvery { issue(command = any()) } just runs
    }
    val createColorData: CreateColorDataUseCase = mockk()

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

    @Test
    fun `invoking 'proceed' action issues 'FetchData' command to Color Details and Color Scheme`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns emptyFlow()
        every { createColorData(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()

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
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()

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
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'set color and proceed' action is invoked, thus new color is sent to color input mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

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
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'proceed' action is invoked, thus 'FetchData' command is issued to Color Details and Color Scheme`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

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
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'proceed' action is invoked, thus 'proceedResult' is updated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns eventsFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

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
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()
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
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()
        colorFlow.value = Color.Hex(0x1)

        data.proceedResult shouldBe null
    }

    fun createSut() =
        HomeViewModel(
            colorInputMediatorFactory = { _ -> colorInputMediator },
            colorInputViewModelFactory = { _, _, _ -> mockk() },
            colorPreviewViewModelFactory = { _, _ -> mockk() },
            colorCenterViewModelFactory = { _, _, _, _ -> mockk() },
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorDetailsCommandStore = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeCommandStore = colorSchemeCommandStore,
            createColorData = createColorData,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value
}