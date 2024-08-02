package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase.Request
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

/**
 * In some cases SUT ViewModel will use mocked instance of [CreateColorSchemeDataUseCase].
 * It is done to simplify tests which don't check contents of returned data: we can just return mock from the use case.
 *
 * In other cases (majority), we want to check contents of returned data.
 * For that we pass real instance of [CreateColorSchemeDataUseCase] to ViewModel.
 * This way the code of use case is treated like internal private part of ViewModel.
 * This approach produces data as if it was in production, meaning that contents are plausible
 * and appropriate for tests that verify values.
 */
class ColorSchemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val commandProvider: ColorCenterCommandProvider = mockk {
        every { commandFlow } returns emptyFlow()
    }
    val getColorScheme: GetColorSchemeUseCase = mockk()
    val createDataMock: CreateColorSchemeDataUseCase = mockk()
    val colorToColorInt: ColorToColorIntUseCase = mockk {
        every { any<Color>().toColorInt() } returns mockk(relaxed = true)
    }
    val isColorLight: IsColorLightUseCase = mockk {
        every { any<Color>().isLight(threshold = any()) } returns false
    }
    val createDataReal = CreateColorSchemeDataUseCase(
        colorToColorInt = colorToColorInt,
        isColorLight = isColorLight,
    )

    lateinit var sut: ColorSchemeViewModel

    @Test
    fun `emission of 'fetch data' command results in emission of Loading state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns mockk()
            every {
                createDataMock(
                    scheme = any(),
                    config = any(),
                    onSwatchSelect = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = any(),
                )
            } returns mockk()
            createSut()

            // "then" block
            launch {
                sut.dataStateFlow
                    .drop(1) // replayed initial state
                    .first() should beOfType<DataState.Loading>()
            }

            // "when" block
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)
        }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns Result.Success(value = mockk())
            every {
                createDataMock(
                    scheme = any(),
                    config = any(),
                    onSwatchSelect = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = any(),
                )
            } returns mockk()
            createSut()

            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    @Test
    fun `selecting new mode updates selected mode`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.selectedMode shouldBe Mode.Analogic
        }

    @Test
    fun `selecting new mode that is different from the active mode results in 'Changes Present'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new mode that is same as the active mode results in 'Changes None'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)
            sut.data.onModeSelect(Mode.Triad)
            sut.data.changes.asPresent().applyChanges()

            sut.data.onModeSelect(Mode.Triad)

            sut.data.changes should beOfType<Changes.None>()
        }

    @Test
    fun `selecting new swatch count updates selected swatch count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.selectedSwatchCount shouldBe SwatchCount.Thirteen
        }

    @Test
    fun `selecting new swatch count that is different from the active swatch count results in 'Changes Present'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.changes should beOfType<Changes.Present>()
        }

    @Test
    fun `selecting new swatch count that is same as the active swatch count results in 'Changes None'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)
            sut.data.changes.asPresent().applyChanges()

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.changes should beOfType<Changes.None>()
        }

    @Test
    fun `calling 'apply changes' uses color of last 'fetch data' command as seed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                Result.Success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )
            val seedColor = Color.Hex(0x123456)
            val command = ColorCenterCommand.FetchData(color = seedColor, colorRole = null)
            commandFlow.emit(command)
            sut.data.onModeSelect(Mode.Triad)

            sut.data.changes.asPresent().applyChanges()

            val requests = mutableListOf<Request>()
            coVerify { getColorScheme.invoke(request = capture(requests)) }
            requests.last().seed shouldBe seedColor
        }

    /**
     * GIVEN
     *  1. fetching color scheme will end with failure.
     *  2. SUT is initialized.
     *
     * WHEN
     *  [FetchData][ColorCenterCommand.FetchData] command is emitted and data fetching ends with failure
     *
     * THEN
     *  updated data state is [DataState.Error].
     */
    @Test
    fun `emission of 'fetch data' command that triggers failing data fetching results in emission of 'DataState Error'`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorScheme(request = any()) } returns
                HttpFailure.UnknownHost(cause = mockk())
            createSut()

            val command = ColorCenterCommand.FetchData(color = mockk(), colorRole = null)
            commandFlow.emit(command)

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    /**
     * GIVEN
     *  1. SUT is initialized.
     *  2. [FetchData][ColorCenterCommand.FetchData] command is emitted and initial data is fetched.
     *  3. selected mode and swatch count are changed.
     *  4. changes are applied, but this time data fetching returns failure and data state
     *  is set to [DataState.Error].
     *
     * WHEN
     *  [ColorSchemeError.tryAgain] is invoked
     *
     * THEN
     *  data is fetched successfully and mode / swatch count that were set are used in request.
     */
    @Test
    fun `invoking 'try again' action of 'DataState Error' with changed selected values uses those values for repeated request`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fun mockGetColorSchemeReturnsSuccess() {
                coEvery { getColorScheme(request = any<Request>()) } returns
                    Result.Success(value = someDomainColorScheme())
            }

            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            mockGetColorSchemeReturnsSuccess()
            createSut(
                createData = createDataReal,
            )
            val seedColor = Color.Hex(0x123456)
            val command = ColorCenterCommand.FetchData(color = seedColor, colorRole = null)
            commandFlow.emit(command)
            sut.data.onModeSelect(Mode.Triad)
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)
            coEvery { getColorScheme(request = any()) } returns
                HttpFailure.UnknownHost(cause = mockk())
            sut.data.changes.asPresent().applyChanges()
            mockGetColorSchemeReturnsSuccess()

            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>().error.tryAgain()

            val requests = mutableListOf<Request>()
            coVerify { getColorScheme.invoke(request = capture(requests)) }
            requests.last().run {
                mode shouldBe Mode.Triad
                swatchCount shouldBe 13
            }
        }

    fun createSut(
        createData: CreateColorSchemeDataUseCase = createDataMock,
    ) =
        ColorSchemeViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            commandProvider = commandProvider,
            getColorScheme = getColorScheme,
            createData = createData,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    fun someDomainColorScheme(): DomainColorScheme =
        DomainColorScheme(
            swatchDetails = listOf(
                ColorDetails(
                    color = Color.Hex(0x123456),
                    colorHexString = ColorDetails.ColorHexString(
                        withNumberSign = "#123456",
                        withoutNumberSign = "123456",
                    ),
                    colorTranslations = mockk(),
                    colorName = "Color#1",
                    exact = mockk(),
                    matchesExact = false,
                    distanceFromExact = 123,
                ),
                ColorDetails(
                    color = Color.Hex(0x1A803F),
                    colorHexString = ColorDetails.ColorHexString(
                        withNumberSign = "#1A803F",
                        withoutNumberSign = "1A803F",
                    ),
                    colorTranslations = mockk(),
                    colorName = "Color#2",
                    exact = mockk(),
                    matchesExact = false,
                    distanceFromExact = 80,
                ),
            ),
        )

    val ColorSchemeViewModel.data: ColorSchemeData
        get() =
            this.dataStateFlow.value.shouldBeInstanceOf<DataState.Ready>().data

    fun Changes.asPresent(): Changes.Present =
        this.shouldBeInstanceOf<Changes.Present>()
}