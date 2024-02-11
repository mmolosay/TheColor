package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ColorSchemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getInitialModels: GetInitialDataModelsUseCase = mockk()
    val getColorScheme: GetColorSchemeUseCase = mockk()
    val colorSchemeDataModelsFactory: ColorSchemeDataModelsFactory = mockk()

    lateinit var sut: ColorSchemeViewModel

    val ColorSchemeViewModel.data: ColorSchemeData
        get() {
            this.dataStateFlow.value should beOfType<State.Ready>() // // assertion for clear failure message
            return (this.dataStateFlow.value as State.Ready).data
        }

    @Before
    fun setUp() {
        mockkObject(ColorSchemeDataFactory)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial 'null' models initialize flow with Loading state`() {
        every { getInitialModels() } returns null

        createSut()

        sut.dataStateFlow.value should beOfType<State.Loading>()
    }

    @Test
    fun `initial not-null models initialize flow with Ready state`() {
        every { getInitialModels() } returns mockk()
        every { ColorSchemeDataFactory.create(models = any(), actions = any()) } returns mockk()

        createSut()

        sut.dataStateFlow.value should beOfType<State.Ready>()
    }

    @Test
    fun `call to 'get color scheme' emits Loading state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialModels: ColorSchemeData.Models = mockk()
            every { getInitialModels() } returns initialModels
            every {
                ColorSchemeDataFactory.create(models = initialModels, actions = any())
            } returns mockk {
                every { selectedMode } returns Mode.Monochrome
                every { selectedSwatchCount } returns SwatchCount.Six
            }
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val models: ColorSchemeData.Models = mockk()
            every {
                colorSchemeDataModelsFactory.create(scheme = any(), config = any())
            } returns models
            every {
                ColorSchemeDataFactory.create(
                    models = models,
                    actions = any()
                )
            } returns mockk()
            createSut()

            // "then" block
            launch {
                sut.dataStateFlow
                    .drop(1) // replayed initial state
                    .first() should beOfType<State.Loading>()
            }

            // "when" block
            sut.getColorScheme(seed = mockk())
        }

    @Test
    fun `call to 'get color scheme' emits Ready state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } returns null
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            every {
                colorSchemeDataModelsFactory.create(scheme = any(), config = any())
            } returns mockk()
            every { ColorSchemeDataFactory.create(models = any(), actions = any()) } returns mockk()

            createSut()

            sut.getColorScheme(seed = mockk())

            sut.dataStateFlow.value should beOfType<State.Ready>()
        }

    @Test
    fun `selecting new mode updates selected mode`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.selectedMode shouldBe Mode.Analogic
        }

    @Test
    fun `selecting new mode that is different from the active mode makes 'apply changes' button visible`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new mode that is same as the active mode makes 'apply changes' button hidden`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Analogic,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onModeSelect(Mode.Monochrome)

            sut.data.changes should beOfType<Changes.None>()
        }

    @Test
    fun `selecting new swatch count updates selected swatch count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.selectedSwatchCount shouldBe SwatchCount.Thirteen
        }

    @Test
    fun `selecting new swatch count that is different from the active swatch count makes 'apply changes' button visible`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new swatch count that is same as the active swatch count makes 'apply changes' button hidden`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialModels() } answers {
                ColorSchemeData.Models(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Thirteen,
                    hasChanges = false,
                )
            }
            createSut()

            sut.data.onSwatchCountSelect(SwatchCount.Six)

            sut.data.changes should beOfType<Changes.None>()
        }

//    @Test
//    fun `calling 'apply changes' applies selected values to their actual conterparts`() =
//        runTest(mainDispatcherRule.testDispatcher) {
//            val actionsSlot = slot<Actions>()
//            every { getInitialState(actions = capture(actionsSlot)) } answers {
//                ColorSchemeData(
//                    swatches = listOf(),
//                    activeMode = Mode.Monochrome,
//                    selectedMode = Mode.Analogic,
//                    onModeSelect = {},
//                    activeSwatchCount = SwatchCount.Six,
//                    selectedSwatchCount = SwatchCount.Thirteen,
//                    onSwatchCountSelect = {},
//                    changes = Changes.Present(applyChanges = actionsSlot.captured.applyChanges),
//                ).let {
//                    State.Ready(it)
//                }
//            }
//            createSut()
//
//            sut.data.changes should beOfType<Changes.Present>()
//            (sut.data.changes as Changes.Present).applyChanges()
//
//            // TODO: this test fails at the moment. 'lastUsedSeed' is not initialized
//            sut.data.selectedMode shouldBe sut.data.activeMode
//            sut.data.selectedSwatchCount shouldBe sut.data.selectedSwatchCount
//        }

    fun createSut() =
        ColorSchemeViewModel(
            getInitialModels = getInitialModels,
            getColorScheme = getColorScheme,
            colorSchemeDataModelsFactory = colorSchemeDataModelsFactory,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}