package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase.Request
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorSchemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getInitialModelsState: GetInitialModelsStateUseCase = mockk()
    val commandProvider: ColorCenterCommandProvider = mockk {
        every { commandFlow } returns emptyFlow()
    }
    val getColorScheme: GetColorSchemeUseCase = mockk()
    val createModels: CreateDataModelsUseCase = mockk()

    lateinit var sut: ColorSchemeViewModel

    @Test
    fun `initial Loading models state initializes flow with Loading state`() {
        every { getInitialModelsState() } returns State.Loading

        createSut()

        sut.dataStateFlow.value should beOfType<State.Loading>()
    }

    @Test
    fun `initial Ready models state initializes flow with Ready state`() {
        every { getInitialModelsState() } returns State.Ready(someModels())

        createSut()

        sut.dataStateFlow.value should beOfType<State.Ready<*>>()
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Loading state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Ready(someModels())
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any<Request>()) } returns mockk()
            every { createModels(scheme = any(), config = any()) } returns someModels()
            createSut()

            // "then" block
            launch {
                sut.dataStateFlow
                    .drop(1) // replayed initial state
                    .first() should beOfType<State.Loading>()
            }

            // "when" block
            val command = ColorCenterCommand.FetchData(color = mockk())
            commandFlow.emit(command)
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Loading
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any<Request>()) } returns Result.Success(value = mockk())
            every { createModels(scheme = any(), config = any()) } returns someModels()
            createSut()

            val command = ColorCenterCommand.FetchData(color = mockk())
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<State.Ready<*>>()
        }

    @Test
    fun `selecting new mode updates selected mode`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Ready(someModels())
            createSut()

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.selectedMode shouldBe Mode.Analogic
        }

    @Test
    fun `selecting new mode that is different from the active mode results in present changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Ready(someModels())
            createSut()

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new mode that is same as the active mode results in none changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns someModels().copy(
                activeMode = Mode.Monochrome,
                selectedMode = Mode.Analogic,
            ).let { State.Ready(it) }
            createSut()

            sut.data.onModeSelect(Mode.Monochrome)

            sut.data.changes should beOfType<Changes.None>()
        }

    @Test
    fun `selecting new swatch count updates selected swatch count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Ready(someModels())
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.selectedSwatchCount shouldBe SwatchCount.Thirteen
        }

    @Test
    fun `selecting new swatch count that is different from the active swatch count results in present changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Ready(someModels())
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new swatch count that is same as the active swatch count results in none changes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns someModels().copy(
                activeSwatchCount = SwatchCount.Six,
                selectedSwatchCount = SwatchCount.Thirteen,
            ).let { State.Ready(it) }
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Six)

            sut.data.changes should beOfType<Changes.None>()
        }

    @Test
    fun `calling 'apply changes' uses color of last 'fetch data' command as seed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModelsState() } returns State.Loading
            val color: Color = mockk()
            every { commandProvider.commandFlow } returns flowOf(ColorCenterCommand.FetchData(color))
            coEvery { getColorScheme(request = any<Request>()) } returns Result.Success(value = mockk())
            every { createModels(scheme = any(), config = any()) } returns someModels().copy(
                activeMode = Mode.Monochrome, // different modes for Changes.Present
                selectedMode = Mode.Analogic, // different modes for Changes.Present
            )
            createSut()

            sut.data.changes.asPresent().applyChanges()

            val requests = mutableListOf<Request>()
            coVerify { getColorScheme.invoke(request = capture(requests)) }
            requests.last().seed shouldBe color
        }

    fun createSut() =
        ColorSchemeViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            getInitialModelsState = getInitialModelsState,
            commandProvider = commandProvider,
            getColorScheme = getColorScheme,
            createModels = createModels,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    fun someModels() =
        ColorSchemeData.Models(
            swatches = listOf(),
            activeMode = Mode.Monochrome,
            selectedMode = Mode.Monochrome,
            activeSwatchCount = SwatchCount.Six,
            selectedSwatchCount = SwatchCount.Six,
        )

    val ColorSchemeViewModel.data: ColorSchemeData
        get() {
            this.dataStateFlow.value should beOfType<State.Ready<*>>() // assertion for clear failure message
            return (this.dataStateFlow.value as State.Ready).data
        }

    fun Changes.asPresent(): Changes.Present {
        this should beOfType<Changes.Present>() // assertion for clear failure message
        return (this as Changes.Present)
    }
}