package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.api.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.api.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.api.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.api.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.api.ColorRole
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.HomeData.ColorData
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
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
    val colorCenterCommandStore: ColorCenterCommandStore = mockk()
    val colorCenterEventStore: ColorCenterEventStore = mockk()
    val createColorFromColorInput: CreateColorDataUseCase = mockk()

    lateinit var sut: HomeViewModel

    @Test
    fun `given color from Color Input is not 'null', when SUT is created, then data has 'CanProceed Yes'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `given color from color input is 'null', when SUT is created, then data has 'CanProceed No'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has 'CanProceed Yes'`() {
        // from other tests, we know that this will produce 'CanProceed.No' in data
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = mockk<Color>()

        data.canProceed should beOfType<CanProceed.Yes>()
    }


    @Test
    fun `when receiving a 'null' color from Color Input, then data has 'CanProceed No'`() {
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = null

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `invoking 'proceed' action issues 'FetchData' command to Color Center`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        every { createColorFromColorInput(color = any()) } returns mockk()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()

        coVerify {
            colorCenterCommandStore.issue(command = any<ColorCenterCommand.FetchData>())
        }
    }

    @Test
    fun `invoking 'proceed' action updates 'colorUsedToProceed'`() {
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        val colorUsedToProceed: ColorData = mockk()
        every { createColorFromColorInput(color = any()) } returns colorUsedToProceed
        createSut()

        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()

        data.colorUsedToProceed shouldBe colorUsedToProceed
    }

    @Test
    fun `when receiving a 'Sumbit' event from Color Input with 'Valid' color input state, then 'proceed' action is invoked, thus 'FetchData' command is issued to Color Center`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorCenterEventStore.eventFlow } returns emptyFlow()
            every { createColorFromColorInput(color = any()) } returns mockk()
            coEvery { colorCenterCommandStore.issue(command = any()) } just runs
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
            )
            eventsFlow.emit(event)

            coVerify {
                colorCenterCommandStore.issue(command = any<ColorCenterCommand.FetchData>())
            }
        }

    @Test
    fun `when receiving a 'Submit' event from Color Input with 'Valid' color input state, then 'proceed' action is invoked, thus 'colorUsedToProceed' is updated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { colorCenterEventStore.eventFlow } returns emptyFlow()
            coEvery { colorCenterCommandStore.issue(command = any()) } just runs
            val colorUsedToProceed: ColorData = mockk()
            every { createColorFromColorInput(color = any()) } returns colorUsedToProceed
            createSut()

            val event = ColorInputEvent.Submit(
                colorInput = mockk(),
                colorInputState = ColorInputState.Valid(color = mockk()),
            )
            eventsFlow.emit(event)

            data.colorUsedToProceed shouldBe colorUsedToProceed
        }

    @Test
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'set color and proceed' action is invoked, thus new color is sent to color input mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorCenterEvent>()
            every { colorCenterEventStore.eventFlow } returns eventsFlow
            every { createColorFromColorInput(color = any()) } returns mockk()
            coEvery { colorCenterCommandStore.issue(any()) } just runs
            createSut()

            val event = ColorCenterEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            eventsFlow.emit(event)

            coVerify {
                colorInputMediator.send(color = Color.Hex(0x123456), from = null)
            }
        }

    @Test
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'proceed' action is invoked, thus 'FetchData' command is issued to Color Center`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorCenterEvent>()
            every { colorCenterEventStore.eventFlow } returns eventsFlow
            every { createColorFromColorInput(color = any()) } returns mockk()
            coEvery { colorCenterCommandStore.issue(command = any()) } just runs
            createSut()

            val event = ColorCenterEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            eventsFlow.emit(event)

            coVerify {
                colorCenterCommandStore.issue(command = any<ColorCenterCommand.FetchData>())
            }
        }

    @Test
    fun `when receiving a 'ExactColorSelected' event from Color Center, 'proceed' action is invoked, thus 'colorUsedToProceed' is updated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { colorInputEventStore.eventFlow } returns emptyFlow()
            val eventsFlow = MutableSharedFlow<ColorCenterEvent>()
            every { colorCenterEventStore.eventFlow } returns eventsFlow
            coEvery { colorCenterCommandStore.issue(command = any()) } just runs
            val colorUsedToProceed: ColorData = mockk()
            every { createColorFromColorInput(color = any()) } returns colorUsedToProceed
            createSut()

            val event = ColorCenterEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            eventsFlow.emit(event)

            data.colorUsedToProceed shouldBe colorUsedToProceed
        }

    @Test
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'colorUsedToProceed' is cleared`() {
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        every { createColorFromColorInput(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()
        colorFlow.value = null

        data.colorUsedToProceed shouldBe null
    }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'colorUsedToProceed' is cleared`() {
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorCenterEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        every { createColorFromColorInput(color = any()) } returns mockk()
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().action.invoke()
        colorFlow.value = Color.Hex(0x1)

        data.colorUsedToProceed shouldBe null
    }

    fun createSut() =
        HomeViewModel(
            colorInputMediatorFactory = { _ -> colorInputMediator },
            colorInputViewModelFactory = { _, _, _ -> mockk() },
            colorPreviewViewModelFactory = { _, _ -> mockk() },
            colorCenterViewModelFactory = { _, _, _ -> mockk() },
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorCenterCommandStore = colorCenterCommandStore,
            colorCenterEventStore = colorCenterEventStore,
            createColorData = createColorFromColorInput,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value
}