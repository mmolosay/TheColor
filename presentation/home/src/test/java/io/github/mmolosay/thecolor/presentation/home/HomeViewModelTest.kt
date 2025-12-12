package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.viewModelScope
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorComparator
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.HomeViewModelTest.MyMatchers.match
import io.github.mmolosay.thecolor.presentation.home.HomeViewModelTest.MyMatchers.matchAny
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponents
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsFactory
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsStore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSession
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateColorDataUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.DoesColorBelongToSessionUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel.SuspendGates
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModelDiModule
import io.github.mmolosay.thecolor.presentation.home.viewmodel.components
import io.github.mmolosay.thecolor.presentation.input.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEventStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKVerificationScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import javax.inject.Provider
import io.github.mmolosay.thecolor.domain.model.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.model.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val colorInputMediator: ColorInputMediator = mockk(relaxed = true)
    val colorInputColorStore: ColorInputColorStore = spyk() // for actual impl of 'wouldEmitIfSet()'
    val colorInputEventStore: ColorInputEventStore = mockk()
    val colorInputGroupViewModel: ColorInputGroupViewModel = mockk(relaxed = true)
    lateinit var colorInputSubmitAction: ColorInputSubmitAction
    val colorInputGroupViewModelFactory = object : ColorInputGroupViewModel.Factory {
        override fun create(
            coroutineScope: CoroutineScope,
            eventStore: ColorInputEventStore,
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputGroupViewModel {
            colorInputSubmitAction = submitAction
            return colorInputGroupViewModel
        }
    }

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
    lateinit var colorCenterComponentsStore: ColorCenterComponentsStore
    val colorCenterComponentsStoreFactory = object : ColorCenterComponentsStore.Factory {
        override fun create(
            viewModelScope: CoroutineScope,
        ): ColorCenterComponentsStore {
            val factory = ColorCenterComponentsFactory(
                colorDetailsCommandStoreProvider = colorDetailsCommandStoreProvider,
                colorDetailsEventStoreProvider = colorDetailsEventStoreProvider,
                colorDetailsViewModelFactory = { _, _, _ -> colorDetailsViewModel },
                colorSchemeCommandStoreProvider = colorSchemeCommandStoreProvider,
                colorSchemeEventStoreProvider = colorSchemeEventStoreProvider,
                colorSchemeViewModelFactory = { _, _, _ -> colorSchemeViewModel },
                colorCenterViewModelFactory = { _, _, _ -> colorCenterViewModel },
            )
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
    val doesColorBelongToSession = DoesColorBelongToSessionUseCase(
        colorComparator = ColorComparator(
            colorConverter = ColorConverter(),
        ),
    )

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        val disabled = ResumeFromLastSearchedColorOnStartup(enabled = false)
        every { flowOfResumeFromLastSearchedColorOnStartup } returns MutableStateFlow(disabled)
    }
    val lastSearchedColorRepository: LastSearchedColorRepository = mockk {
        coEvery { setLastSearchedColor(color = any()) } just runs
    }
    val getPredictableRandomColor: GetPredictableRandomColorUseCase = mockk()

    lateinit var sut: HomeViewModel

    @Test // ANCHOR:Label=0
    fun `given color from Color Input is not 'null', when SUT is created, then data has 'CanProceed Yes'`() {
        mockStoresWithEmptyFlows()
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())

        createSut()

        data.canProceed should beOfType<CanProceed.Yes>()
    }

    @Test
    fun `given color from color input is 'null', when SUT is created, then data has 'CanProceed No'`() {
        mockStoresWithEmptyFlows()
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)

        createSut()

        data.canProceed should beOfType<CanProceed.No>()
    }

    @Test
    fun `when receiving a not-null color from Color Input, then data has 'CanProceed Yes'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            // from other tests, we know that this will produce 'CanProceed.No' in data
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            createSut()

            val color = mockk<Color>()
            run emitColorFromColorInput@{
                colorInputColorFlow.emit(color)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(color)
            }

            data.canProceed should beOfType<CanProceed.Yes>()
        }

    @Test
    fun `when receiving a 'null' color from Color Input, then data has 'CanProceed No'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            createSut()

            run emitColorFromColorInput@{
                val color: Color? = null
                colorInputColorFlow.emit(color)
                colorProcessedConfirmationChannelForColorPreviewReal.send(color)
            }

            data.canProceed should beOfType<CanProceed.No>()
        }

    @Test
    fun `when receiving a 'null' color from Color Input, then data is NOT updated until 'is data being updated' flag is set to 'true'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            every { createColorData(color = any()) } returns mockk()
            val gate = ClosableSuspendGate(closed = false)
            createSut(
                gateForDataUpdateGuard = gate,
            )

            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke() // REFERENCE:Label=0
            val data1 = data
            data1.proceedResult shouldNotBe null // REFERENCE:Label=1

            gate.close()
            run emitFirstColorFromColorInput@{
                val nullColor: Color? = null
                colorInputColorFlow.emit(nullColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(nullColor)
            }
            sut.flowOfIsDataBeingUpdated.value shouldBe false // hasn't updated due to closed gate
            val data2 = data
            data2.proceedResult shouldBe data1.proceedResult // hasn't updated yet

            gate.open()
            val data3 = data
            data3.proceedResult shouldBe null // REFERENCE:Label=2
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
            mockStoresWithEmptyFlows()
            val initialColorInHex = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color>(initialColorInHex)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            val exactColorInRgb = Color.Rgb(1, 2, 3)
            val exactColorInHex = Color.Hex(0x010203)
            run emitDataFetchedEvent@{
                val domainDetails = mockk<DomainColorDetails>(relaxed = true) {
                    every { color } returns initialColorInHex
                    every { exact } returns mockk {
                        every { color } returns exactColorInHex
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }
            // clicking "Go to exact color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColorInRgb,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                colorInputColorFlow.emit(exactColorInRgb)
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColorInRgb)
            }

            // indicator of not finished session
            data.proceedResult shouldNotBe null
        }


    @Test
    fun `when receiving any color from Color Input, then 'proceed' is not executed until Color Preview confirms that it has processed this color as well`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
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
                colorInputColorFlow.emit(color)
            }

            coVerify(exactly = 0) {
                colorProcessedConfirmationChannelForColorPreviewMock.send(color)
            }
            coVerify(exactly = 0) {
                proceed(colorMatcher = matchAny(), colorRoleMatcher = matchAny())
            }
            sut.viewModelScope.cancel() // SUT is suspended waiting for confirmation from Color Preview
        }

    /**
     * Ongoing data transaction should wait until [HomeViewModel.colorProcessedConfirmationChannelForColorPreview]
     * sends confirmation that the new color was processed by [ColorPreviewViewModel] before said
     * data transaction finishes.
     */
    @Test
    fun `when receiving any color from Color Input, then 'is data being updated' flag stays true until Color Preview confirms that it has processed this color as well`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            every { createColorData(color = any()) } returns mockk()
            createSut(
                colorProcessedConfirmationChannelForColorPreview = colorProcessedConfirmationChannelForColorPreviewReal,
            )

            val color = mockk<Color>()
            run emitExactColorFromColorInput@{
                colorInputColorFlow.emit(color)
            }
            sut.flowOfIsDataBeingUpdated.value shouldBe true // data transaction has started and is ongoing
            // colorProcessedConfirmationChannelForColorPreviewReal.send(color) wasn't called yet

            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(color)
            }
            sut.flowOfIsDataBeingUpdated.value shouldBe false // data transaction has finished
        }

    @Test
    fun `given there is a not-null color in Color Input, when 'proceed' action is invoked, then 'proceed' is executed`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            coVerify {
                proceed(colorMatcher = matchAny(), colorRoleMatcher = matchAny())
            }
        }

    @Test // ANCHOR:Label=1
    fun `given there is a not-null color in Color Input, when 'proceed' action is invoked, then 'proceedResult' is updated`() {
        mockStoresWithEmptyFlows()
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
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
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            colorInputSubmitAction.invoke(
                colorInput = mockk(),
                validationResult = ColorInputValidationResult.Valid(color = mockk()),
            )

            coVerify {
                proceed(colorMatcher = matchAny(), colorRoleMatcher = matchAny())
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
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
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
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
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
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
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
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
     * 2. there's some color in [ColorInputColorStore]
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
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorInputEventFlow = MutableSharedFlow<ColorInputEvent>()
            every { colorInputEventStore.eventFlow } returns colorInputEventFlow
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
    fun `when receiving a 'ColorSelected' event from Color Details, 'set color and proceed' action is invoked, thus new color is sent to color input mediator`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event = ColorDetailsEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            colorDetailsEventFlow.emit(event)

            coVerify {
                colorInputMediator.send(color = Color.Hex(0x123456), from = null)
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then 'proceed' method is invoked`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            coEvery {
                colorInputMediator.send(color = any(), from = any())
            } coAnswers  {
                val sentColor = firstArg<Color>()
                colorInputColorFlow.emit(sentColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(sentColor)
            }
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
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
                colorDetailsEventFlow.emit(event)
            }

            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }

            coVerify {
                proceed(colorMatcher = match(exactColor), colorRoleMatcher = match(ColorRole.Exact))
            }
        }

    @Test
    fun `when receiving a 'ColorSelected' event from Color Details, then 'proceedResult' is not cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow(value = mockk<Color>())
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
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
                colorDetailsEventFlow.emit(event)
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
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorFromColorInput@{
                colorInputColorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val event = ColorDetailsEvent.DataFetched(
                    domainDetails = mockk(relaxed = true),
                )
                colorDetailsEventFlow.emit(event)
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
            mockStoresWithEmptyFlows()
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event = ColorDetailsEvent.ColorSelected(
                color = Color.Hex(0x123456),
                colorRole = ColorRole.Exact,
            )
            colorDetailsEventFlow.emit(event)

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
     *  nothing breaks: [HomeViewModel.proceed] is invoked for both color X and then for color Y.
     */
    @Test
    fun `when receiving two same 'ColorSelected' events from Color Details rapidly, and then receiving different 'ColorSelected' event, then 'proceed' action is invoked normally`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
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
                colorDetailsEventFlow.emit(event)
            }

            // clicking "Go to exact color" twice in a quick succession
            run emitExactColorSelectedEvents@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                launch {
                    colorDetailsEventFlow.emit(event) // 1st time
                    colorDetailsEventFlow.emit(event) // 2nd time
                }
            }
            run emitExactColorFromColorInput@{
                colorInputColorFlow.emit(exactColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns exactColor
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }
            // clicking "Go back to initial color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = initialColor,
                    colorRole = ColorRole.Initial,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitInitialColorFromColorInput@{
                colorInputColorFlow.emit(initialColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(initialColor)
            }
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }

            coVerifyOrder {
                proceed(colorMatcher = match(exactColor), colorRoleMatcher = match(ColorRole.Exact))
                proceed(colorMatcher = match(initialColor), colorRoleMatcher = match(ColorRole.Initial))
            }
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then 'color scheme selected swatch data' is set`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            every { colorInputColorStore.colorFlow } returns MutableStateFlow<Color?>(initialColor)
            val colorSchemeEventFlow = MutableSharedFlow<ColorSchemeEvent>()
            every { colorSchemeEventStore.eventFlow } returns colorSchemeEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event: ColorSchemeEvent.SwatchSelected = mockk(relaxed = true)
            colorSchemeEventFlow.emit(event)

            data.colorSchemeSelectedSwatchData shouldNotBe null // assuming initially value is 'null'
        }

    @Test
    fun `when receiving a 'SwatchSelected' event from Color Scheme, then command is sent to 'selected swatch color details'`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            every { colorInputColorStore.colorFlow } returns MutableStateFlow(value = mockk<Color>())
            val colorSchemeEventFlow = MutableSharedFlow<ColorSchemeEvent>()
            every { colorSchemeEventStore.eventFlow } returns colorSchemeEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = any()) } returns colorData
            createSut()
            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()

            val event: ColorSchemeEvent.SwatchSelected = mockk(relaxed = true)
            colorSchemeEventFlow.emit(event)

            coEvery {
                val commandStore = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsCommandStore
                    .shouldNotBeNull()
                commandStore.issue(command = any<ColorDetailsCommand.SetColorDetails>())
            }
        }

    @Test // ANCHOR:Label=2
    fun `when receiving 'null' color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitColorFromColorInput@{
                val newColor: Color? = null
                colorInputColorFlow.emit(newColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(newColor)
            }

            data.proceedResult shouldBe null
        }

    @Test
    fun `when receiving not-null color from Color Input after 'proceed' was invoked, then 'proceedResult' is cleared`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed.invoke()
            run emitNewColorFromColorInput@{
                colorInputColorFlow.emit(Color.Hex(0x1))
            }

            data.proceedResult shouldBe null
        }

    @Test
    fun `when 'proceed' is invoked and Color Input is cleared before 'DataFetched' event arrives, then no exception is thrown`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            run proceedWithInitialColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            run emitNewColorFromColorInput@{
                val newColor: Color? = null
                colorInputColorFlow.emit(newColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(newColor)
            }

            shouldNotThrowAny {
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                    every { exact } returns mockk {
                        every { color } returns mockk()
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. there's a color c0 in [ColorInputColorStore]. Exact color for it is c1.
     *
     * WHEN
     * 1. [HomeData.CanProceed.Yes.proceed] action is invoked.
     * It is a new Color Center session, so [sut] starts waiting for [ColorDetailsEvent.DataFetched]
     * to create a new [ColorCenterSession].
     * 2. [ColorDetailsEvent.DataFetched] for proceeded color c0 hasn't arrived yet, so new
     * [ColorCenterSession] is not created yet.
     * 3. new color c2 is emitted from [colorInputColorStore].
     * 4. [HomeData.CanProceed.Yes.proceed] action is invoked.
     * It is a new Color Center session, so [sut] starts waiting for [ColorDetailsEvent.DataFetched]
     * to create a new [ColorCenterSession].
     * 5. [ColorDetailsEvent.DataFetched] for proceeded color c2 arrives, and new
     * [ColorCenterSession] is created.
     * 6. [ColorDetailsEvent.DataFetched] for proceeded color c0 arrives, but due to last
     * "proceeded with" color was c2, it is ignored, and no new [ColorCenterSession] is created.
     * 7. new color c1 (see GIVEN) is emitted from [colorInputColorStore].
     * Due to it being an "exact" color for initial color c0, it belongs to the same [ColorCenterSession].
     * But current session is for color c2, thus this color is treated as a color from different session.
     *
     * THEN
     * [HomeData.proceedResult] is set to `null`.
     * If WHEN #6 led to creation of new [ColorCenterSession] with seed being c0, then it would've
     * been the current session. Thus c1 would've been considered belonging to that session,
     * and [HomeData.proceedResult] wouldn't have been set to `null`.
     */
    @Test
    fun `when 'proceed' is invoked and new color is emitted from Color Input and proceeded with before 'DataFetched' event arrives for initial color, then Color Center session is created for the latest proceeded color`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val exactColorForInitialColor = Color.Hex(0x1)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            run proceedWithInitialColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            val newColor = Color.Hex(0x2)
            run emitNewColorFromColorInput@{
                colorInputColorFlow.emit(newColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(newColor)
            }
            run proceedWithNewColor@{
                // we know from other tests that it would be 'CanProceed.Yes'
                data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            }
            run emitDataFetchedEventOfNewColor@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns newColor
                    every { exact } returns mockk {
                        every { color } returns Color.Hex(0x3)
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }
            run emitDataFetchedEventOfInitialColor@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                    every { exact } returns mockk {
                        every { color } returns exactColorForInitialColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColorForInitialColorFromColorInput@{
                colorInputColorFlow.emit(exactColorForInitialColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColorForInitialColor)
            }

            data.proceedResult shouldBe null
        }

    @Test
    fun `when 'proceed' is invoked for second time before 'DataFetched' event arrives, then no exception is thrown`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val currentColor = Color.Hex(0x0) // name 'color' conflicts with fields of DomainColorDetails
            val colorInputColorFlow = MutableStateFlow<Color?>(currentColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsColorFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsColorFlow
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
                colorDetailsColorFlow.emit(event)
            }
            // emit event for second invocation of 'proceed'
            shouldNotThrowAny {
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsColorFlow.emit(event)
            }
        }

    /**
     * Ongoing data transaction should wait until [HomeViewModel.colorCenterViewModelFlow] emits
     * new instance before said data transaction finishes.
     *
     * GIVEN
     * 1. [sut] is created.
     * 2. [SuspendGate] is closed to simulate a possible delay.
     *
     * WHEN
     * 1. 'proceed' is invoked. New Color Center session is started, thus new [ColorCenterComponents]
     * are created.
     * 2. [SuspendGate] is opened to allow [HomeViewModel.colorCenterViewModelFlow]
     * to process new [ColorCenterComponents] and emit new ViewModel instance.
     *
     * THEN
     * 1. [HomeViewModel.colorCenterViewModelFlow] emits new ViewModel instance.
     * 2. [HomeViewModel.flowOfIsDataBeingUpdated] emits `false` to indicate that data transaction has finished.
     */
    @Test
    fun `when 'proceed' is invoked, then 'is data being updated' is set back to false only after flow of ColorCenterViewModel emits new instance`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val currentColor = Color.Hex(0x0) // name 'color' conflicts with fields of DomainColorDetails
            val colorInputColorFlow = MutableStateFlow<Color?>(currentColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsColorFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsColorFlow
            every { createColorData(color = any()) } returns mockk()
            val gate = ClosableSuspendGate(closed = true)
            createSut(
                gateForCollectColorCenterComponent = gate,
            )

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            // no need to emit ColorDetailsEvent.DataFetched: we're not interested in building ColorCenterSession in this test
            colorCenterComponentsStore.components shouldNotBe null // new components were created
            sut.flowOfIsDataBeingUpdated.value shouldBe true // data transaction is still running
            gate.open() // allow flow of ColorCenterViewModel to emit new instance

            sut.flowOfIsDataBeingUpdated.value shouldBe false // data transaction has finished
        }

    /**
     * Ongoing data transaction should wait until [HomeViewModel.collectColorCenterComponents]
     * collects new components before said data transaction finishes.
     *
     * GIVEN
     * 1. [sut] is created.
     * 2. [SuspendGate] is closed to simulate a possible delay.
     *
     * WHEN
     * 1. 'proceed' is invoked. New Color Center session is started, thus new [ColorCenterComponents]
     * are created.
     * 2. [SuspendGate] is opened to allow [HomeViewModel.collectColorCenterComponents]
     * to process new [ColorCenterComponents] and start collecting values from new Command/Event Stores.
     *
     * THEN
     * [HomeViewModel.flowOfIsDataBeingUpdated] emits `false` to indicate that data transaction has finished.
     */
    @Test
    fun `when 'proceed' is invoked, then 'is data being updated' is set back to false only after 'collectColorCenterComponents()' collects new components`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val currentColor = Color.Hex(0x0) // name 'color' conflicts with fields of DomainColorDetails
            val colorInputColorFlow = MutableStateFlow<Color?>(currentColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsColorFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsColorFlow
            every { createColorData(color = any()) } returns mockk()
            val gate = ClosableSuspendGate(closed = true)
            createSut(
                gateForFlowOfColorCenterViewModel = gate,
            )

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            // no need to emit ColorDetailsEvent.DataFetched: we're not interested in building ColorCenterSession in this test
            val components = colorCenterComponentsStore.components.shouldNotBeNull() // new components were created
            sut.colorCenterViewModelFlow.value shouldNotBe components.colorCenterViewModel // still old instance
            sut.flowOfIsDataBeingUpdated.value shouldBe true // data transaction is still running
            gate.open() // allow flow of ColorCenterViewModel to emit new instance

            sut.colorCenterViewModelFlow.value shouldBe components.colorCenterViewModel // already new instance
            sut.flowOfIsDataBeingUpdated.value shouldBe false // data transaction has finished
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
            } returns kotlin.run {
                val enabled = ResumeFromLastSearchedColorOnStartup(enabled = true)
                MutableStateFlow(enabled)
            }
            val lastSearchedColor: Color = Color.Hex(0x1A803F)
            coEvery { lastSearchedColorRepository.getLastSearchedColor() } returns lastSearchedColor
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            val colorData: ProceedResult.Success.ColorData = mockk()
            every { createColorData(color = lastSearchedColor) } returns colorData
            coEvery {
                colorInputMediator.send(color = any(), from = any())
            } coAnswers  {
                val sentColor = firstArg<Color>()
                colorInputColorFlow.emit(sentColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(sentColor)
            }

            createSut()

            val proceedResultAsSuccess =
                data.proceedResult.shouldBeInstanceOf<ProceedResult.Success>()
            proceedResultAsSuccess.colorData shouldBe colorData
        }

    @Test
    fun `only 'seed' color of a Color Center session is persisted as a last searched color`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val initialColor = Color.Hex(0x0)
            val colorInputColorFlow = MutableStateFlow<Color?>(initialColor)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val colorDetailsEventFlow = MutableSharedFlow<ColorDetailsEvent>()
            every { colorDetailsEventStore.eventFlow } returns colorDetailsEventFlow
            every { createColorData(color = any()) } returns mockk()
            createSut()

            // we know from other tests that it would be 'CanProceed.Yes'
            data.canProceed.shouldBeInstanceOf<CanProceed.Yes>().proceed()
            val exactColor = Color.Hex(0x123456)
            run emitDataFetchedEvent@{
                val domainDetails: DomainColorDetails = mockk(relaxed = true) {
                    every { color } returns initialColor
                    every { exact } returns mockk {
                        every { color } returns exactColor
                    }
                }
                val event = ColorDetailsEvent.DataFetched(domainDetails)
                colorDetailsEventFlow.emit(event)
            }

            // clicking "Go to exact color"
            run emitColorSelectedEvent@{
                val event = ColorDetailsEvent.ColorSelected(
                    color = exactColor,
                    colorRole = ColorRole.Exact,
                )
                colorDetailsEventFlow.emit(event)
            }
            run emitExactColor@{
                colorInputColorFlow.emit(exactColor)
                colorProcessedConfirmationChannelForColorPreviewReal.send(exactColor)
            }
            run emitDataFetchedEvent@{
                val event = ColorDetailsEvent.DataFetched(
                    domainDetails = mockk(relaxed = true),
                )
                colorDetailsEventFlow.emit(event)
            }

            coVerify(exactly = 1) {
                lastSearchedColorRepository.setLastSearchedColor(color = initialColor)
            }
        }

    @Test
    fun `invoking 'randomize color' sends new randomized color to color input mediator`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val randomColor: Color.Hex = mockk()
            every { getPredictableRandomColor() } returns randomColor
            val featureValue = DomainAutoProceedWithRandomizedColors(enabled = false)
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors } returns MutableStateFlow(featureValue)
            createSut()

            data.randomizeColor()

            coVerify(exactly = 1) {
                colorInputMediator.send(color = randomColor, from = null)
            }
        }

    @Test
    fun `invoking 'randomize color' proceeds with it if 'auto proceed with randomized colors' feature is enabled`() =
        runTest(testDispatcher) {
            mockStoresWithEmptyFlows()
            val colorInputColorFlow = MutableStateFlow<Color?>(null)
            every { colorInputColorStore.colorFlow } returns colorInputColorFlow
            val randomColor: Color.Hex = mockk()
            every { getPredictableRandomColor() } returns randomColor
            val featureValue = DomainAutoProceedWithRandomizedColors(enabled = true)
            every { userPreferencesRepository.flowOfAutoProceedWithRandomizedColors } returns MutableStateFlow(featureValue)
            every { createColorData(color = any()) } returns mockk()
            createSut()

            data.randomizeColor()
            run emitColorFromColorInput@{
                colorInputColorFlow.emit(randomColor)
            }
            run emitConfirmationFromColorPreview@{
                colorProcessedConfirmationChannelForColorPreviewReal.send(randomColor)
            }

            coVerify(exactly = 1) {
                proceed(colorMatcher = match(randomColor), colorRoleMatcher = match(null))
            }
        }

    fun createSut(
        colorProcessedConfirmationChannelForColorPreview: Channel<Color?> = colorProcessedConfirmationChannelForColorPreviewReal,
        gateForFlowOfColorCenterViewModel: SuspendGate = OpenSuspendGate,
        gateForCollectColorCenterComponent: SuspendGate = OpenSuspendGate,
        gateForDataUpdateGuard: SuspendGate = OpenSuspendGate,
    ) =
        HomeViewModel(
            colorInputMediatorFactory = { _ -> colorInputMediator },
            colorInputGroupViewModelFactory = colorInputGroupViewModelFactory,
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
            colorProcessedConfirmationChannelForColorPreview = colorProcessedConfirmationChannelForColorPreview,
            colorPreviewViewModelFactory = { _, _, _ -> mockk(relaxed = true) },
            colorCenterComponentsStoreFactory = colorCenterComponentsStoreFactory,
            gates = SuspendGates(
                gateForFlowOfColorCenterViewModel = gateForFlowOfColorCenterViewModel,
                gateForCollectColorCenterComponent = gateForCollectColorCenterComponent,
                gateForDataUpdateGuard = gateForDataUpdateGuard,
            ),
            createColorData = createColorData,
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
        every { colorInputColorStore.colorFlow } returns MutableStateFlow(null)
        every { colorInputEventStore.eventFlow } returns emptyFlow()
        every { colorDetailsEventStore.eventFlow } returns MutableSharedFlow()
        every { colorSchemeEventStore.eventFlow } returns emptyFlow()
    }

    /**
     * Verifies that [HomeViewModel.proceed] was invoked with the specified parameters.
     * Use inside [coVerify] block.
     */
    suspend inline fun MockKVerificationScope.proceed(
        colorMatcher: MyMatcher<Color> = matchAny(),
        colorRoleMatcher: MyMatcher<ColorRole?> = matchAny(),
    ) {
        kotlin.run verifyColorDetailsCommandIssued@{
            // and(matcher, matcher) is inconvenient to use
            val expectedCommand = match<ColorDetailsCommand> { command ->
                if (command !is ColorDetailsCommand.FetchData) return@match false
                colorMatcher.match(command.color) && colorRoleMatcher.match(command.colorRole)
            }
            colorDetailsCommandStore.issue(command = expectedCommand)
        }
        kotlin.run verifyColorSchemeCommandIssued@{
            // and(matcher, matcher) is inconvenient to use
            val expectedCommand = match<ColorSchemeCommand> { command ->
                if (command !is ColorSchemeCommand.FetchData) return@match false
                colorMatcher.match(command.color)
            }
            colorSchemeCommandStore.issue(command = expectedCommand)
        }
    }

    fun interface MyMatcher<in T> {
        fun match(actual: T): Boolean
    }

    object MyMatchers {
        fun <T> matchAny() = MyMatcher<T> { true }
        fun <T> match(expected: T) = MyMatcher<T> { actual -> actual == expected }
    }
}