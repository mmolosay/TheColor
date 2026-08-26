package io.github.mmolosay.thecolor.presentation.scheme

import app.cash.turbine.test
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.color.ColorScheme
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.domain.exception.DomainException
import io.github.mmolosay.thecolor.domain.exception.DomainFailure
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.CreateColorSchemeDataUseCase
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

/**
 * In some cases SUT ViewModel will use mocked instance of [io.github.mmolosay.thecolor.presentation.scheme.viewmodel.CreateColorSchemeDataUseCase].
 * It is done to simplify tests which don't check contents of returned data: we can just return mock from the use case.
 *
 * In other cases (majority), we want to check contents of returned data.
 * For that we pass real instance of [io.github.mmolosay.thecolor.presentation.scheme.viewmodel.CreateColorSchemeDataUseCase] to ViewModel.
 * This way the code of use case is treated like internal private part of ViewModel.
 * This approach produces data as if it was in production, meaning that contents are plausible
 * and appropriate for tests that verify values.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ColorSchemeViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val colorRepository: ColorRepository = mockk()
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
    fun `when 'fetch color scheme' is invoked, then SUT emits 'Loading' state`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = mockk())
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

            sut.dataStateFlow.test {
                sut.fetchColorScheme(seed = mockk())

                skipItems(1) // replayed value of 'StateFlow'
                awaitItem() should beOfType<DataState.Loading>()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when 'fetch color scheme' is invoked, then SUT emits 'Ready' state`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = mockk())
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

            sut.fetchColorScheme(seed = mockk())

            sut.dataStateFlow.value should beOfType<DataState.Ready>()
        }

    @Test
    fun `when 'fetch color scheme' is invoked, then the ongoing job is canceled, so that repository is only accessed once`() =
        runTest(testDispatcher) {
            val fetchedScheme: ColorScheme = mockk(relaxed = true)
            val getColorSchemeDeferred = CompletableDeferred<Result<ColorScheme>>()
            coEvery {
                colorRepository.getColorScheme(request = any())
            } coAnswers {
                getColorSchemeDeferred.await()
            }
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

            val color1 = Color.Hex(0x0)
            sut.fetchColorScheme(color1)
            val color2 = Color.Hex(0x1)
            sut.fetchColorScheme(color2)
            getColorSchemeDeferred.complete(value = Result.success(fetchedScheme))

            // verify that component that is called deep inside 'setSeedColor()' is only called once:
            // the first coroutine is canceled thus it never goes deep enough to trigger this component.
            coVerify(exactly = 1) {
                createDataMock(
                    scheme = any(),
                    config = any(),
                    onSwatchSelect = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = any(),
                )
            }
        }

    @Test
    fun `when a new mode is selected, then the 'selected mode' is updated`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onModeSelect(Mode.Analogic)

            sut.data.selectedMode shouldBe Mode.Analogic
        }

    @Test
    fun `when a new mode is selected and it is different from the active mode, then SUT emits data with 'Changes Present'`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onModeSelect(Mode.Analogic)

            sut.data.hasChangesToApply should beOfType<Changes.Present>()
        }

    @Test
    fun `when a new mode is selected and it is the same as the active mode, then SUT emits data with 'Changes None'`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onModeSelect(Mode.Triad)
            sut.data.hasChangesToApply.asPresent().applyChanges()
            sut.data.onModeSelect(Mode.Triad)

            sut.data.hasChangesToApply should beOfType<Changes.None>()
        }

    @Test
    fun `when a new swatch count is selected, then the 'selected swatch count' is updated`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.selectedSwatchCount shouldBe SwatchCount.Thirteen
        }

    @Test
    fun `when a new swatch count is selected and it is different from the active swatch count, then SUT emits data with 'Changes Present'`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.hasChangesToApply should beOfType<Changes.Present>()
        }

    @Test
    fun `when a new swatch count is selected and it is the same as the active swatch count, then SUT emits data with 'Changes None'`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seed = mockk())
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)
            sut.data.hasChangesToApply.asPresent().applyChanges()
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.hasChangesToApply should beOfType<Changes.None>()
        }

    @Test
    fun `when 'apply changes' is invoked, then the color of last 'fetch color scheme' action is used`() =
        runTest(testDispatcher) {
            val seedColor = Color.Hex(0x123456)
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seedColor)
            sut.data.onModeSelect(Mode.Triad)
            sut.data.hasChangesToApply.asPresent().applyChanges()

            val requests = mutableListOf<GetColorSchemeRequest>()
            coVerify { colorRepository.getColorScheme(request = capture(requests)) }
            requests.last().seed shouldBe seedColor
        }

    @Test
    fun `when 'fetch data' is invoked and data fetching fails, then SUT emits 'Error' state`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
            createSut()

            sut.fetchColorScheme(seed = mockk())

            sut.dataStateFlow.value should beOfType<DataState.Error>()
        }

    /**
     * GIVEN
     * SUT is initialized.
     *
     * WHEN
     * 1. [ColorSchemeViewModel.fetchColorScheme] is invoked and initial data is fetched.
     * 2. selected mode and swatch count are changed.
     * 3. changes are applied, but this time data fetching returns failure and data state
     * is set to [DataState.Error].
     * 4. [io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeError.tryAgain] is invoked
     *
     * THEN
     * data is fetched successfully and mode / swatch count that were set in WHEN #2 are used in request.
     */
    @Test
    fun `when 'try again' is invoked, then SUT uses values from the action that has failed and is being retried`() =
        runTest(testDispatcher) {
            fun mockGetColorSchemeReturnsSuccess() {
                coEvery { colorRepository.getColorScheme(request = any()) } returns
                        Result.success(value = someDomainColorScheme())
            }

            val seedColor = Color.Hex(0x123456)
            mockGetColorSchemeReturnsSuccess()
            createSut(
                createData = createDataReal,
            )

            sut.fetchColorScheme(seedColor)
            sut.data.onModeSelect(Mode.Triad)
            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)
            coEvery { colorRepository.getColorScheme(request = any()) } returns run {
                val exception = DomainException(
                    failure = DomainFailure.Http.UnknownHost,
                    cause = mockk(),
                )
                Result.failure(exception)
            }
            sut.data.hasChangesToApply.asPresent().applyChanges()
            mockGetColorSchemeReturnsSuccess()

            sut.dataStateFlow.value.shouldBeInstanceOf<DataState.Error>().error.tryAgain()

            val requests = mutableListOf<GetColorSchemeRequest>()
            coVerify { colorRepository.getColorScheme(request = capture(requests)) }
            requests.last().run {
                mode shouldBe Mode.Triad
                swatchCount shouldBe 13
            }
        }

    @Test
    fun `when 'on swatch select' is invoked, then SUT sends appropriate event to the event store`() =
        runTest(testDispatcher) {
            coEvery { colorRepository.getColorScheme(request = any()) } returns
                    Result.success(value = someDomainColorScheme())
            val selectedSwatchColor = ColorInt(0x1A803F)
            every {
                with(colorToColorInt) { Color.Hex(0x1A803F).toColorInt() }
            } returns selectedSwatchColor
            createSut(
                createData = createDataReal,
            )

            sut.eventFlow.test {
                // WHEN
                val seedColor = Color.Hex(0x123456)
                sut.fetchColorScheme(seedColor)
                val indexOfSelectedSwatch = 1
                sut.data.onSwatchSelect(indexOfSelectedSwatch)

                // THEN
                val emittedEvent = awaitItem()
                emittedEvent.shouldBeInstanceOf<ColorSchemeEvent.SwatchSelected>()
                emittedEvent.swatch.color shouldBe selectedSwatchColor
            }
        }

    fun createSut(
        createData: CreateColorSchemeDataUseCase = createDataMock,
        coroutineDispatcher: CoroutineDispatcher = testDispatcher,
    ) =
        ColorSchemeViewModel(
            coroutineScope = CoroutineScope(context = coroutineDispatcher),
            colorRepository = colorRepository,
            createData = createData,
            defaultDispatcher = coroutineDispatcher,
            ioDispatcher = coroutineDispatcher,
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