package io.github.mmolosay.thecolor.presentation.home

import app.cash.turbine.test
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.MutableViewModelEventFlow
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsFactory
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsStore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSession
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateColorDataUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.DoesColorBelongToSessionUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.testing.MockColorInputMediatorComponents
import io.github.mmolosay.thecolor.presentation.input.testing.mockSet
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.ClosedLatch
import io.github.mmolosay.thecolor.utils.OpenLatch
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
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val colorInputMediatorComponents = MockColorInputMediatorComponents()
    val colorInputMediator = colorInputMediatorComponents.mediator
    val colorInputGroupViewModel: ColorInputGroupViewModel = mockk(relaxed = true)
    lateinit var colorInputSubmitAction: ColorInputSubmitAction
    val colorInputGroupViewModelFactory = object : ColorInputGroupViewModel.Factory {
        override fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputGroupViewModel {
            colorInputSubmitAction = submitAction
            return colorInputGroupViewModel
        }
    }

    val colorPreviewViewModel: ColorPreviewViewModel = mockk(relaxed = true)

    val colorDetailsViewModel: ColorDetailsViewModel = mockk(relaxed = true)

    val colorSchemeViewModel: ColorSchemeViewModel = mockk(relaxed = true)

    val colorCenterViewModel: ColorCenterViewModel = mockk(relaxed = true) {
        every { colorDetailsViewModel } returns this@HomeViewModelTest.colorDetailsViewModel
        every { colorSchemeViewModel } returns this@HomeViewModelTest.colorSchemeViewModel
    }
    lateinit var colorCenterComponentsStore: ColorCenterComponentsStore
    val colorCenterComponentsStoreFactory = object : ColorCenterComponentsStore.Factory {
        override fun create(
            viewModelScope: CoroutineScope,
        ): ColorCenterComponentsStore {
            val factory = ColorCenterComponentsFactory(
                colorCenterViewModelFactory = { _, _, _ -> colorCenterViewModel },
                colorDetailsViewModelFactory = { _ -> colorDetailsViewModel },
            ) { _ -> colorSchemeViewModel }
            return ColorCenterComponentsStore(
                viewModelScope = viewModelScope,
                factory = factory,
            ).also {
                colorCenterComponentsStore = it
            }
        }
    }

    val createColorData: CreateColorDataUseCase = mockk()

    // real implementation, there's no need to have mock for color comparison
    val colorComparator = ColorComparator(
        colorConverter = ColorConverter(),
    )

    // real implementation, there's no need to have mock for color comparison
    val doesColorBelongToSession = DoesColorBelongToSessionUseCase(
        colorComparator = colorComparator,
    )

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        val value = ResumeFromLastSearchedColorOnStartup(enabled = false)
        val result = PrefState.Result.HasValue(value)
        val prefState = PrefState.Ready(result)
        every { flowOfResumeFromLastSearchedColorOnStartup } returns MutableStateFlow(prefState)
    }
    val lastSearchedColorRepository: LastSearchedColorRepository = mockk {
        coEvery { setLastSearchedColor(color = any()) } just runs
    }
    val getPredictableRandomColor: GetPredictableRandomColorUseCase = mockk()

    lateinit var sut: HomeViewModel

    @Test // ANCHOR:Label=0
    fun `given color from Color Input is not 'null', when SUT is created, then data has 'CanProceed Yes'`() {
        mockStoresWithEmptyFlows()
        every { colorInputMediator.colorStateFlow } returns run {
            val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
            MutableStateFlow(value)
        }

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `given color from color input is 'null', when SUT is created, then data has 'CanProceed No'`() {
        mockStoresWithEmptyFlows()
        every { colorInputMediator.colorStateFlow } returns run {
            val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
            MutableStateFlow(value)
        }

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has 'CanProceed Yes'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            // from other tests, we know that this will produce 'CanProceed.No' in data
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            createSut()

            val color = mockk<Color>()
            run emitColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            data.canProceed should beOfType<CanProceed.Yes>()
        }

    @Test
    fun `when receiving a 'null' color from Color Input, then data has 'CanProceed No'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            createSut()

            run emitColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            data.canProceed should beOfType<CanProceed.No>()
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. 'proceed' was already invoked and there's a color in Color Center
     *
     * WHEN
     * 1. [ColorDetailsEvent.SelectColorAction] for "exact" color is emitted (e.g. due to user clicking on "go to exact" button)
     * 2. the event is handled and "exact" color is sent to [ColorInputMediator]
     * 3. the update of the [ColorInputMediator.colorStateFlow] is received and processed.
     * SUT checks whether the new color (which is "exact" color) belongs to the ongoing color session.
     *
     * THEN
     * "exact" color is confirmed to belong to the ongoing color session and it (session)
     * doesn't get finished, thus [HomeData.proceedResult] is not set to `null`.
     */
    @Test
    fun `when receiving a not-null RGB color from Color Input due to 'ColorSelected' event, then session is not finished`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColorInHex = Color.Hex(0x0)
            val exactColorInRgb = Color.Rgb(1, 2, 3)
            val exactColorInHex = Color.Hex(0x010203)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColorInHex, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColorInHex,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColorInHex
                        every { exact } returns mockk {
                            every { color } returns exactColorInHex
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            // clicking "Go to exact color"
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColorInRgb,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColorInRgb, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            // indicator of not finished session
            data.proceedResult shouldNotBe null
        }

    /**
     * Ongoing data transaction should wait until [ColorPreviewViewModel.setColor] job completes
     * (meaning that the new color has been processed by the [ColorPreviewViewModel])
     * before said data transaction finishes.
     */
    @Test
    fun `when receiving any color from Color Input, then 'is data being updated' flag stays 'true' until 'set color' of Color Preview completes`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            every { createColorData(color = any()) } returns mockk()
            val gateForSetColorMethod = ClosableSuspendGate(closed = true)
            coEvery { colorPreviewViewModel.setColor(color = any()) }
                .coAnswers {
                    async { gateForSetColorMethod.awaitOpen() }
                }
            createSut()

            val color = Color.Hex(0x0)
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 1)
                colorStateFlow.emit(value)
            }
            sut.flowOfDataUpdateLatch.value shouldBe ClosedLatch // data transaction has started and is ongoing

            gateForSetColorMethod.open()
            sut.flowOfDataUpdateLatch.value shouldBe OpenLatch // data transaction has finished
        }

    @Test
    fun `given there is a not-null color in Color Input, when 'proceed' action is invoked, then 'proceed' is executed`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val color = Color.Hex(0x0)
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { createColorData(color) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            coVerify {
                colorDetailsViewModel.setSeedColor(
                    color = color,
                    deferredDetails = any(),
                )
                colorSchemeViewModel.fetchColorScheme(seed = color)
            }
        }

    @Test // ANCHOR:Label=1
    fun `given there is a not-null color in Color Input, when 'proceed' action is invoked, then 'proceedResult' is updated`() {
        mockStoresWithEmptyFlows()
        every { colorInputMediator.colorStateFlow } returns run {
            val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
            MutableStateFlow(value)
        }
        val colorData: ProceedResult.Success.ColorData = mockk()
        every { createColorData(color = any()) } returns colorData
        createSut()

        // we know from other tests that it would be 'CanProceed.Yes'
        data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

        val proceedResult = data.proceedResult
        proceedResult should beOfType<ProceedResult.Success>()
        proceedResult.shouldBeInstanceOf<ProceedResult.Success>().colorData shouldBe colorData
    }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     *
     * WHEN
     * [colorInputSubmitAction] is invoked with [ColorInputValidationResult.Valid]
     *
     * THEN
     * [HomeViewModel.proceed] is invoked.
     */
    @Test
    fun `when 'submit action' of Color Input is invoked with 'Valid' color input validation result, then 'proceed' method is invoked`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val color = Color.Hex(0x0)
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { createColorData(color) } returns mockk()
            createSut()

            colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = ColorInputValidationResult.Valid(color),
            )

            coVerify {
                colorDetailsViewModel.setSeedColor(
                    color = color,
                    deferredDetails = any(),
                )
                colorSchemeViewModel.fetchColorScheme(seed = color)
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     *
     * WHEN
     * [colorInputSubmitAction] is invoked with [ColorInputValidationResult.Valid]
     *
     * THEN
     * [data] is updated with [ProceedResult.Success].
     */
    @Test
    fun `when 'submit action' of Color Input is invoked with 'Valid' color input validation result, then 'proceed' action is invoked, thus 'proceedResult' is set to 'Success'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = ColorInputValidationResult.Valid(color = mockk()),
            )

            val proceedResultAsSuccess =
                data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     *
     * WHEN
     * [colorInputSubmitAction] is invoked with [ColorInputValidationResult.Invalid]
     *
     * THEN
     * [data] is updated with [ProceedResult.InvalidSubmittedColor].
     */
    @Test // ANCHOR:Label=3
    fun `when 'submit action' of Color Input is invoked with 'Invalid' color input validation result, then 'proceed' action is not invoked, thus 'proceedResult' is set to 'InvalidSubmittedColor'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = mockk<ColorInputValidationResult.Invalid>(),
            )

            data.proceedResult should beOfType<ProceedResult.InvalidSubmittedColor>()
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     *
     * WHEN
     * [colorInputSubmitAction] is invoked with [ColorInputValidationResult.Valid]
     *
     * THEN
     * [colorInputSubmitAction] returns `true`.
     */
    @Test
    fun `when 'submit action' of Color Input is invoked with 'Valid' color input validation result, then submission is reported as accepted`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val wasAccepted = colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = ColorInputValidationResult.Valid(color = mockk()),
            )

            wasAccepted shouldBe true
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     *
     * WHEN
     * [colorInputSubmitAction] is invoked with [ColorInputValidationResult.Invalid]
     *
     * THEN
     * [colorInputSubmitAction] returns `false`.
     */
    @Test
    fun `when 'submit action' of Color Input is invoked with 'Invalid' color input validation result, then submission is reported as not accepted`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            val wasAccepted = colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = mockk<ColorInputValidationResult.Invalid>(),
            )

            wasAccepted shouldBe false
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputMediator]
     * 3. [sut] has [data] with [ProceedResult.InvalidSubmittedColor]
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
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = mockk<ColorInputValidationResult.Invalid>(),
            )
            // we know from other tests that it would be 'InvalidSubmittedColor'
            // REFERENCE:Label=3
            data.proceedResult.shouldBeInstanceOf<ProceedResult.InvalidSubmittedColor>().discard.invoke()

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then the 'set color and proceed' action is invoked, thus new color is set to color input mediator`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            coVerify {
                colorInputMediator.withLock(block = any())
                colorInputMediatorComponents.editor.set(color = exactColor, source = null)
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then 'proceed' method is invoked`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            coEvery {
                colorInputMediator.withLock(block = any())
            } coAnswers  {
                val block = firstArg<suspend (ColorInputMediator.Editor) -> Unit>()
                val editor = colorInputMediatorComponents.editor
                editor.mockSet { color, source ->
                    val value = ColorInputMediator.ColorState(color = color, source = source, id = 1)
                    colorStateFlow.emit(value)
                }
                block.invoke(editor)
            }
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }

            coVerify {
                colorDetailsViewModel.selectColor(
                    role = ColorRole.Exact,
                    deferredDetails = any(),
                )
                colorSchemeViewModel.fetchColorScheme(seed = exactColor)
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then 'proceedResult' is not cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            sut.dataFlow.test {
                // WHEN
                skipItems(1) // replayed value of 'StateFlow'
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
                // clicking "Go to exact color"
                run emitExactColorSelectedEvent@{
                    val event = ColorDetailsEvent.SelectColorAction(
                        color = exactColor,
                        colorRole = ColorRole.Exact,
                    )
                    colorDetailsEventFlow.emit(event)
                }
                run emitExactColorFromColorInput@{
                    val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 0)
                    colorStateFlow.emit(value)
                }

                // THEN
                awaitItem() shouldBe data // only the 2nd, expected emission
                data.proceedResult shouldNotBe null // the focus of this test
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, 'proceed' action is invoked, thus 'proceedResult' is updated`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            val proceedResultAsSuccess = data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
            coVerify {
                colorDetailsViewModel.selectColor(
                    role = ColorRole.Exact,
                    deferredDetails = any(),
                )
                colorSchemeViewModel.fetchColorScheme(seed = exactColor)
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. [sut] is proceeded with some color
     *
     * WHEN
     * 1. receiving two same [ColorDetailsEvent.SelectColorAction] events with color X in a quick succession
     * 2. then receiving a different [ColorDetailsEvent.SelectColorAction] event with color Y
     *
     * THEN
     *  nothing breaks: [HomeViewModel.proceed] is invoked for both color X and then for color Y.
     */
    @Test
    fun `when receiving two same 'ColorSelected' events from Color Details rapidly, and then receiving different 'ColorSelected' event, then 'proceed' action is invoked normally`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitExactColorSelectedEvents@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                coroutineScope {
                    launch { colorDetailsEventFlow.emit(event) } // 1st time
                    launch { colorDetailsEventFlow.emit(event) } // 2nd time
                }
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }
            run emitSeedColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = initialColor,
                    colorRole = ColorRole.Seed,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitInitialColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 2)
                colorStateFlow.emit(value)
            }

            coVerifyOrder {
                run {
                    colorDetailsViewModel.selectColor(
                        role = ColorRole.Exact,
                        deferredDetails = any(),
                    )
                    colorSchemeViewModel.fetchColorScheme(seed = exactColor)
                }
                run {
                    colorDetailsViewModel.selectColor(
                        role = ColorRole.Seed,
                        deferredDetails = any(),
                    )
                    colorSchemeViewModel.fetchColorScheme(seed = initialColor)
                }
            }
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then 'color scheme selected swatch data' is set`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorSchemeEventFlow = MutableViewModelEventFlow<ColorSchemeEvent>()
            every { colorSchemeViewModel.eventFlow } returns colorSchemeEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitSwatchSelectedEvent@{
                val event: ColorSchemeEvent.SelectSwatchAction = mockk(relaxed = true)
                colorSchemeEventFlow.emit(event)
            }

            data.colorSchemeSelectedSwatchData shouldNotBe null // assuming initially value is 'null'
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then 'set seed details' is invoked`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputMediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x0), source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorSchemeEventFlow = MutableViewModelEventFlow<ColorSchemeEvent>()
            every { colorSchemeViewModel.eventFlow } returns colorSchemeEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            run emitSwatchSelectedEvent@{
                val event: ColorSchemeEvent.SelectSwatchAction = mockk(relaxed = true)
                colorSchemeEventFlow.emit(event)
            }

            coVerify {
                val viewModel = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsViewModel
                    .shouldNotBeNull()
                viewModel.setSeedDetails(details = any())
            }
        }

    @Test // ANCHOR:Label=2
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitNullColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitNewColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = Color.Hex(0x1), source = null, id = 1)
                colorStateFlow.emit(value)
            }

            data.proceedResult shouldBe null
        }

    /**
     * Tests that when [DomainColorDetails] are received during the building
     * of the [ColorCenterSession], then colors are matched using [colorComparator] to finalize
     * building the session.
     */
    @Test
    fun `when receiving Color Details, then colors are compared using comparator`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColorInRgb = Color.Rgb(26, 128, 63)
            val initialColorInHex = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColorInRgb, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColorInRgb,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColorInHex
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
        }

    @Test
    fun `when 'proceed' is invoked and Color Input is cleared before color details arrive, then no exception is thrown`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            run proceedWithInitialColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            run emitNullColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            // "THEN"
            // if any exception inside SUT is thrown, then 'runTest()' will re-throw it and the test will fail
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's a color `c0` in [ColorInputMediator]. Exact color for it is `c1`.
     *
     * WHEN
     * 1. [HomeData.CanProceed.Yes.proceed] action is invoked.
     * It is a new Color Center session, so [sut] starts waiting for [DomainColorDetails] of color `c0`
     * to create a new [ColorCenterSession].
     * 2. [DomainColorDetails] of color `c0` hasn't arrived yet, so new [ColorCenterSession] is not
     * created yet.
     * 3. new color `c2` is emitted from [ColorInputMediator.colorStateFlow].
     * 4. [HomeData.CanProceed.Yes.proceed] action is invoked.
     * It is a new Color Center session, so [sut] starts waiting for [DomainColorDetails] of color `c2`
     * to create a new [ColorCenterSession].
     * 5. [DomainColorDetails] of proceeded color `c2` arrives, and a new [ColorCenterSession]
     * is created.
     * 6. [DomainColorDetails] of color `c0` finally arrive, but due to last
     * "proceeded with" color was `c2`, it is ignored, and no new [ColorCenterSession] is created.
     * 7. new color `c1` (see GIVEN) is emitted from [ColorInputMediator.colorStateFlow].
     * Due to it being an "exact" color for initial color `c0`, it belongs to the same [ColorCenterSession].
     * But current session is for color `c2`, thus this color is treated as a color from different session.
     *
     * THEN
     * [HomeData.proceedResult] is set to `null`.
     * If WHEN #6 led to creation of a new [ColorCenterSession] with seed being `c0`, then it would've
     * been the current session. Thus, `c1` would've been considered belonging to that session,
     * and [HomeData.proceedResult] wouldn't have been set to `null`.
     */
    @Test
    fun `when 'proceed' is invoked and new color is emitted from Color Input and proceeded with before color details of initial color arrive, then Color Center session is created for the latest proceeded color`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val firstColor = Color.Hex(0x0)
            val exactColorForFirst = Color.Hex(0x1)
            val secondColor = Color.Hex(0x2)
            val exactColorForSecond = Color.Hex(0x3)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = firstColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            val gateForDetailsOfFirstColor = ClosableSuspendGate(closed = true)
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = firstColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns firstColor
                        every { exact } returns mockk {
                            every { color } returns exactColorForFirst
                        }
                    }
                    launch {
                        gateForDetailsOfFirstColor.awaitOpen()
                        deferredDetails.complete(domainDetails)
                    }
                    return@coAnswers Job()
                }
            }
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = secondColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns secondColor
                        every { exact } returns mockk {
                            every { color } returns exactColorForSecond
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            run proceedWithFirstColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            run emitSecondColorFromColorInput@{
                val value =
                    ColorInputMediator.ColorState(color = secondColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }
            run proceedWithSecondColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            run completeDetailsOfFirstColor@{
                gateForDetailsOfFirstColor.open()
            }
            run emitExactColorForFirstColorFromColorInput@{
                val value =
                    ColorInputMediator.ColorState(color = exactColorForFirst, source = null, id = 2)
                colorStateFlow.emit(value)
            }

            data.proceedResult shouldBe null
        }

    @Test
    fun `when 'proceed' is invoked for second time before color details arrive, then no exception is thrown`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            every { colorInputMediator.colorStateFlow } returns run {
                val value =
                    ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            var invocationCount =
                0 // un-synchronized 'var' because 'UnconfinedTestDispatcher' is single-threaded
            val gates = List(size = 2) { ClosableSuspendGate(closed = true) }
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val currentInvocation = invocationCount++
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    launch {
                        gates[currentInvocation].awaitOpen()
                        deferredDetails.complete(domainDetails)
                    }
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            gates[0].open()

            // "THEN"
            // if any exception inside SUT is thrown, then 'runTest()' will re-throw it and the test will fail
            shouldNotThrowAny {
                gates[1].open()
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
                userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup
            } returns run {
                val value = ResumeFromLastSearchedColorOnStartup(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                MutableStateFlow(prefState)
            }
            val lastSearchedColor: Color = Color.Hex(0x1A803F)
            coEvery { lastSearchedColorRepository.getLastSearchedColor() } returns lastSearchedColor
            mockStoresWithEmptyFlows()
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = lastSearchedColor) } returns colorData
            run {
                val slotOfBlock = slot<suspend (ColorInputMediator.Editor) -> Unit>()
                coEvery {
                    colorInputMediator.withLock(block = capture(slotOfBlock))
                } coAnswers {
                    val block = slotOfBlock.captured
                    val editor = colorInputMediatorComponents.editor
                    editor.mockSet { color, source ->
                        val value =
                            ColorInputMediator.ColorState(color = color, source = source, id = 1)
                        colorStateFlow.emit(value)
                    }
                    block.invoke(editor)
                }
            }

            createSut()

            val proceedResultAsSuccess = data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    @Test
    fun `only 'seed' color of a Color Center session is persisted as a last searched color`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColor = Color.Hex(0x1)
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = initialColor, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val colorDetailsEventFlow = MutableViewModelEventFlow<ColorDetailsEvent>()
            every { colorDetailsViewModel.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            run {
                val slotOfDeferredDetails = slot<CompletableDeferred<DomainColorDetails>>()
                every {
                    colorDetailsViewModel.setSeedColor(
                        color = initialColor,
                        deferredDetails = capture(slotOfDeferredDetails),
                    )
                } coAnswers {
                    val deferredDetails = slotOfDeferredDetails.captured
                    val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                        every { color } returns initialColor
                        every { exact } returns mockk {
                            every { color } returns exactColor
                        }
                    }
                    deferredDetails.complete(domainDetails)
                    return@coAnswers Job()
                }
            }
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            run emitExactColorSelectedEvent@{
                val event = ColorDetailsEvent.SelectColorAction(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = exactColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            coVerify(exactly = 1) {
                lastSearchedColorRepository.setLastSearchedColor(color = initialColor)
            }
        }

    @Test
    fun `invoking 'randomize color' sets new randomized color to color input mediator`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val randomColor: Color.Hex = mockk()
            every { getPredictableRandomColor() } returns randomColor
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors } returns run {
                val value = DomainAutoProceedWithRandomizedColors(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                MutableStateFlow(prefState)
            }
            createSut()

            data.randomizeColor()

            coVerify(exactly = 1) {
                colorInputMediator.withLock(block = any())
                colorInputMediatorComponents.editor.set(color = randomColor, source = null)
            }
        }

    @Test
    fun `invoking 'randomize color' proceeds with it if 'auto proceed with randomized colors' feature is enabled`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorStateFlow = run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            every { colorInputMediator.colorStateFlow } returns colorStateFlow
            val randomColor: Color.Hex = mockk()
            every { getPredictableRandomColor() } returns randomColor

            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors } returns run {
                val value = DomainAutoProceedWithRandomizedColors(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                MutableStateFlow(prefState)
            }
            every { createColorData(color = any()) } returns mockk()
            createSut()

            data.randomizeColor()
            run emitRandomColorFromColorInput@{
                val value = ColorInputMediator.ColorState(color = randomColor, source = null, id = 1)
                colorStateFlow.emit(value)
            }

            coVerify(exactly = 1) {
                colorDetailsViewModel.setSeedColor(
                    color = randomColor,
                    deferredDetails = any(),
                )
                colorSchemeViewModel.fetchColorScheme(seed = randomColor)
            }
        }

    fun createSut() =
        HomeViewModel(
            colorInputMediator = colorInputMediator,
            colorInputGroupViewModelFactory = colorInputGroupViewModelFactory,
            colorPreviewViewModelFactory = { _ -> colorPreviewViewModel },
            colorCenterComponentsStoreFactory = colorCenterComponentsStoreFactory,
            createColorData = createColorData,
            colorComparator = colorComparator,
            doesColorBelongToSession = doesColorBelongToSession,
            userPreferencesRepository = userPreferencesRepository,
            lastSearchedColorRepository = lastSearchedColorRepository,
            getPredictableRandomColor = getPredictableRandomColor,
            defaultDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val data: HomeData
        get() = sut.dataFlow.value

    fun mockStoresWithEmptyFlows() {
        every { colorInputMediator.colorStateFlow } returns MutableStateFlow(ColorInputMediator.InitialColorState)
        every { colorDetailsViewModel.eventFlow } returns MutableViewModelEventFlow<ColorDetailsEvent>()
        every { colorSchemeViewModel.eventFlow } returns MutableViewModelEventFlow<ColorSchemeEvent>()
    }
}