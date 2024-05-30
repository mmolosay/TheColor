package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.HomeData.ColorData
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
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

    val getInitialModels: GetInitialModelsUseCase = mockk()
    val colorInputColorStore: ColorInputColorStore = mockk()
    val colorInputEventStore: ColorInputEventStore = mockk()
    val colorCenterCommandStore: ColorCenterCommandStore = mockk()
    val createColorFromColorInput: CreateColorDataUseCase = mockk()

    lateinit var sut: HomeViewModel

    @Test
    fun `when initial models has 'canProceed == true', then data has CanProceed Yes`() {
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `when initial models has 'canProceed == false', then data has CanProceed No`() {
        every { getInitialModels() } returns initialModels(
            canProceed = false,
        )
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has CanProceed Yes`() {
        // initial models don't matter
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = mockk()

        data.canProceed should beOfType<CanProceed.Yes>()
    }


    @Test
    fun `when receiving a 'null' color from Color Input, then data has CanProceed No`() {
        // initial models don't matter
        every { getInitialModels() } returns initialModels(
            canProceed = false,
        )
        val colorFlow = MutableStateFlow<Color?>(null)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        createSut()

        colorFlow.value = null

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `invoking 'proceed' action issues 'FetchData' command to Color Center`() {
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(/*color*/ mockk())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { createColorFromColorInput(color = any()) } returns mockk()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        createSut()

        data.canProceedYes.action.invoke() // we know from other tests that it would be CanProceed.Yes

        coVerify { colorCenterCommandStore.issue(command = any<ColorCenterCommand.FetchData>()) }
    }

    @Test
    fun `invoking 'proceed' action updates 'colorUsedToProceed'`() {
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(/*color*/ mockk())
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        val colorUsedToProceed: ColorData = mockk()
        every { createColorFromColorInput(color = any()) } returns colorUsedToProceed
        createSut()

        data.canProceedYes.action.invoke() // we know from other tests that it would be CanProceed.Yes

        data.colorUsedToProceed shouldBe colorUsedToProceed
    }

    @Test
    fun `when receiving a 'Sumbit' event from Color Input, 'proceed' action is invoked, thus 'FetchData' command is issued to Color Center`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // initial models don't matter
            every { getInitialModels() } returns initialModels(
                canProceed = true,
            )
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(/*color*/ mockk())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            every { createColorFromColorInput(color = any()) } returns mockk()
            coEvery { colorCenterCommandStore.issue(any()) } just runs
            createSut()

            eventsFlow.emit(ColorInputEvent.Submit)

            coVerify { colorCenterCommandStore.issue(command = any<ColorCenterCommand.FetchData>()) }
        }

    @Test
    fun `when receiving a 'Submit' event from Color Input, 'proceed' action is invoked, thus 'colorUsedToProceed' is updated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // initial models don't matter
            every { getInitialModels() } returns initialModels(
                canProceed = true,
            )
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(/*color*/ mockk())
            val eventsFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns eventsFlow
            coEvery { colorCenterCommandStore.issue(command = any()) } just runs
            val colorUsedToProceed: ColorData = mockk()
            every { createColorFromColorInput(color = any()) } returns colorUsedToProceed
            createSut()

            eventsFlow.emit(ColorInputEvent.Submit)

            data.colorUsedToProceed shouldBe colorUsedToProceed
        }

    @Test
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'colorUsedToProceed' is cleared`() {
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        every { createColorFromColorInput(color = any()) } returns mockk()
        createSut()

        data.canProceedYes.action.invoke() // we know from other tests that it would be CanProceed.Yes
        colorFlow.value = null

        data.colorUsedToProceed shouldBe null
    }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'colorUsedToProceed' is cleared`() {
        every { getInitialModels() } returns initialModels(
            canProceed = true,
        )
        val initialColor = Color.Hex(0x0)
        val colorFlow = MutableStateFlow<Color?>(initialColor)
        every { colorInputColorStore.colorFlow } returns colorFlow
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        coEvery { colorCenterCommandStore.issue(command = any()) } just runs
        every { createColorFromColorInput(color = any()) } returns mockk()
        createSut()

        data.canProceedYes.action.invoke() // we know from other tests that it would be CanProceed.Yes
        colorFlow.value = Color.Hex(0x1)

        data.colorUsedToProceed shouldBe null
    }

    fun createSut() =
        HomeViewModel(
            colorInputViewModelFactory = { _, _, _ -> mockk() },
            colorPreviewViewModelFactory = { _, _ -> mockk() },
            colorCenterViewModelFactory = { _, _ -> mockk() },
            getInitialModelsFactory = { getInitialModels },
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorCenterCommandStore = colorCenterCommandStore,
            createColorData = createColorFromColorInput,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value

    val HomeData.canProceedYes: CanProceed.Yes
        get() {
            this.canProceed should beOfType<CanProceed.Yes>() // assertion for clear failure message
            return (this.canProceed as CanProceed.Yes)
        }

    fun initialModels(
        canProceed: Boolean,
    ) =
        HomeData.Models(
            canProceed = canProceed,
            colorUsedToProceed = null,
        )
}