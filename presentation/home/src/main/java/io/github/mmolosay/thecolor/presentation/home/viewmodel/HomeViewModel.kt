package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.ImmediateEventRelay
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsConsumerRegistry.ConsumerId
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ColorSchemeSelectedSwatchData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModelDiModule.ChannelForColorPreview
import io.github.mmolosay.thecolor.presentation.input.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.receiveAllUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * A [ViewModel] for 'Home' View.
 * Composed of sub-feature ViewModels of nested Views.
 *
 * It creates objects that are shared between sub-feature ViewModels via assisted injection and
 * factories.
 *
 * @param colorProcessedConfirmationChannelForColorPreview is passed to [ColorPreviewViewModel]
 * and is used to get notified when it has processed new color emitted from the color flow.
 * See [ColorPreviewViewModel] for details.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    colorInputMediatorFactory: ColorInputMediator.Factory,
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    @ChannelForColorPreview private val colorProcessedConfirmationChannelForColorPreview: Channel<Color?>,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val gates: SuspendGates,
    private val createColorData: CreateColorDataUseCase,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val dataUpdateGuard = DataUpdateGuard(gate = gates.gateForDataUpdateGuard)
    val flowOfIsDataBeingUpdated: StateFlow<Boolean> =
        dataUpdateGuard.flowOfIsDataBeingUpdated

    private val navEventRelay = ImmediateEventRelay<HomeNavEvent>()
    val navEventFlow = navEventRelay.eventFlow

    private val colorInputMediator: ColorInputMediator =
        colorInputMediatorFactory.create(
            colorInputColorStore = colorInputColorStore,
        )
    private val flowOfProcessedColorsFromColorInput =
        MutableStateFlow<Color?>(colorInputColorStore.colorFlow.value)

    val colorInputViewModel: ColorInputViewModel =
        colorInputViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            eventStore = colorInputEventStore,
            mediator = colorInputMediator,
            submitAction = ColorInputSubmitActionImpl(),
        )

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorFlow = flowOfProcessedColorsFromColorInput,
            colorProcessedConfirmationChannel = colorProcessedConfirmationChannelForColorPreview,
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )
    private val componentsConsumerRegistry = ColorCenterComponentsConsumerRegistry()

    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> = kotlin.run {
        val consumedComponents = mutableListOf<ColorCenterComponents?>()
        val consumedComponentsMutex = Mutex()
        val componentsConsumedConfirmationChannel = kotlin.run {
            val consumerId = ConsumerId("colorCenterViewModelFlow")
            componentsConsumerRegistry.register(consumerId)
        }
        colorCenterComponentsStore.componentsFlow
            .transformLatest { components ->
                gates.gateForFlowOfColorCenterViewModel.awaitOpen()
                consumedComponentsMutex.withLock {
                    consumedComponents += components
                }
                emit(components?.colorCenterViewModel)
            }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
            .also { flow ->
                viewModelScope.launch(defaultDispatcher) {
                    flow.collect {
                        consumedComponentsMutex.withLock {
                            consumedComponents.forEach { components ->
                                componentsConsumedConfirmationChannel.send(components)
                            }
                            consumedComponents.clear()
                        }
                    }
                }
            }
    }

    private val ccSessionStore = ColorCenterSessionStore()
    private var jobWithProceed: Job? = null

    init {
        collectColorsFromColorInput()
        collectColorCenterComponents()
        maybeProceedWithLastSearchedColor()
    }

    private fun collectColorsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputColorStore.colorFlow
                .drop(1) // replayed value
                .collect(::onColorFromColorInput)
        }

    private suspend fun onColorFromColorInput(color: Color?) {
        dataUpdateGuard.withCounter {
            _dataFlow.update {
                it.copy(canProceed = CanProceed(colorFromColorInput = color))
            }
            if (color == null || !color.doesBelongToCurrentSession()) {
                clearProceedResult() // 'proceed' wasn't invoked for new color yet
                onColorCenterSessionEnded()
            }
            kotlin.run emitForColorPreviewWithConfirmationOfReceive@{
                flowOfProcessedColorsFromColorInput.emit(color)
                colorProcessedConfirmationChannelForColorPreview.receiveAllUntil(color)
            }
        }
    }

    private fun collectColorCenterComponents() =
        viewModelScope.launch(defaultDispatcher) {
            val componentsConsumedConfirmationChannel = kotlin.run {
                val consumerId = ConsumerId("collectColorCenterComponents()")
                componentsConsumerRegistry.register(consumerId)
            }
            colorCenterComponentsStore.componentsFlow.collectLatest { components ->
                coroutineScope {
                    if (components != null) {
                        launch {
                            components.colorDetailsEventStore.eventFlow
                                .collect(::onEventFromColorDetailsOfColorCenter)
                        }
                        launch {
                            components.colorSchemeEventStore.eventFlow
                                .collect(::onEventFromColorScheme)
                        }
                        launch {
                            components.selectedSwatchColorDetailsEventStore.eventFlow
                                .collect(::onEventFromColorDetailsOfSelectedSwatch)
                        }
                    }
                    launch {
                        gates.gateForCollectColorCenterComponent.awaitOpen()
                        componentsConsumedConfirmationChannel.send(components)
                    }
                }
            }
        }

    private fun onEventFromColorDetailsOfColorCenter(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected ->
                viewModelScope.launch(defaultDispatcher) {
                    dataUpdateGuard.withCounter {
                        val color = event.color
                        colorInputMediator.send(color)
                        // assuming any color selected belongs to ongoing session
                        proceed(color = color, colorRole = event.colorRole)
                    }
                }.also { job ->
                    job.setToJobWithProceed()
                }
            is ColorDetailsEvent.DataFetched ->
                doNothing() // ignore, handled in onColorCenterSessionStarted()
        }
    }

    private fun onEventFromColorScheme(event: ColorSchemeEvent) {
        viewModelScope.launch(defaultDispatcher) {
            when (event) {
                is ColorSchemeEvent.SwatchSelected -> {
                    val command = ColorDetailsCommand.SetColorDetails(
                        domainDetails = event.swatchColorDetails,
                    )
                    val commandStore = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsCommandStore
                        ?: return@launch
                    commandStore.issue(command)
                    val selectedSwatchColorDetailsViewModel = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsViewModel
                        ?: return@launch
                    _dataFlow.update {
                        val data = ColorSchemeSelectedSwatchData(
                            colorDetailsViewModel = selectedSwatchColorDetailsViewModel,
                            discard = ::clearColorSchemeSwatchSelectedData,
                        )
                        it.copy(colorSchemeSelectedSwatchData = data)
                    }
                }
            }
        }
    }

    private suspend fun onEventFromColorDetailsOfSelectedSwatch(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected -> {
                val commandStore = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsCommandStore
                    ?: return
                val command = ColorDetailsCommand.FetchData(
                    color = event.color,
                    colorRole = event.colorRole,
                )
                commandStore.issue(command)
            }
            else -> doNothing()
        }
    }

    private fun maybeProceedWithLastSearchedColor() {
        viewModelScope.launch(defaultDispatcher) {
            val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
                .flowOfResumeFromLastSearchedColorOnStartup
                .filterNotNull().first()
            val enabled = resumeFromLastSearchedColorOnStartup.enabled
            if (!enabled) return@launch
            val color = lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
            dataUpdateGuard.withCounter {
                proceedInNewColorCenterSession(color, colorRole = null)
                colorInputMediator.send(color)
                componentsConsumerRegistry.suspendUntilAllConsumed()
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /** Variation that takes the current color of Color Input. */
    private fun proceed() {
        viewModelScope.launch(defaultDispatcher) {
            dataUpdateGuard.withCounter {
                val color = requireNotNull(colorInputColorStore.colorFlow.value)
                onColorCenterSessionEnded() // end current session (if any)
                proceedInNewColorCenterSession(color, colorRole = null)
                componentsConsumerRegistry.suspendUntilAllConsumed()
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /**
     * Invokes [proceed] action and starts a new Color Center session, which also means
     * new [ColorCenterComponents] are created.
     * You may also want to call [ColorCenterComponentsConsumerRegistry.suspendUntilAllConsumed].
     */
    private fun CoroutineScope.proceedInNewColorCenterSession(
        color: Color,
        colorRole: ColorRole?,
    ) {
        colorCenterComponentsStore.createNewComponents()
        onColorCenterSessionStarted(color)
        proceed(color, colorRole)
    }

    private fun CoroutineScope.proceed(
        color: Color,
        colorRole: ColorRole?,
    ) {
        val components = requireNotNull(colorCenterComponentsStore.components)
        launch issueCommandToColorDetails@{
            val command = ColorDetailsCommand.FetchData(color, colorRole)
            components.colorDetailsCommandStore.issue(command)
        }
        launch issueCommandToColorScheme@{
            val command = ColorSchemeCommand.FetchData(color)
            components.colorSchemeCommandStore.issue(command)
        }
        kotlin.run updateData@{
            val colorData = createColorData(color)
            val proceedResult = HomeData.ProceedResult.Success(
                colorData = colorData,
            )
            _dataFlow.update {
                it.copy(proceedResult = proceedResult)
            }
        }
    }

    private fun randomizeColor() {
        viewModelScope.launch(defaultDispatcher) {
            val color = getPredictableRandomColor()
            val shouldProceed = userPreferencesRepository
                .flowOfAutoProceedWithRandomizedColors
                .filterNotNull().first()
                .enabled
            if (shouldProceed) {
                dataUpdateGuard.withCounter {
                    proceedInNewColorCenterSession(color, colorRole = null)
                    colorInputMediator.send(color)
                    componentsConsumerRegistry.suspendUntilAllConsumed()
                }
            } else {
                colorInputMediator.send(color)
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    private fun sendGoToSettingsNavEvent() {
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        viewModelScope.launch(Dispatchers.Main.immediate) {
            val event = HomeNavEvent.GoToSettings
            navEventRelay.send(event)
        }
    }

    private fun clearProceedResult() {
        _dataFlow.update {
            it.copy(proceedResult = null)
        }
    }

    private fun clearColorSchemeSwatchSelectedData() {
        _dataFlow.update {
            it.copy(colorSchemeSelectedSwatchData = null)
        }
    }

    private fun initialData(): HomeData {
        val canProceed = kotlin.run {
            val color = colorInputColorStore.colorFlow.value
            CanProceed(colorFromColorInput = color)
        }
        return HomeData(
            canProceed = canProceed,
            proceedResult = null, // 'proceed' action wasn't invoked yet
            randomizeColor = ::randomizeColor,
            colorSchemeSelectedSwatchData = null, // no selected swatch initially
            requestToGoToSettings = ::sendGoToSettingsNavEvent,
        )
    }

    private fun CanProceed(colorFromColorInput: Color?): CanProceed {
        val hasColorInColorInput = (colorFromColorInput != null)
        return when (hasColorInColorInput) {
            true -> CanProceed.Yes(proceed = this::proceed)
            false -> CanProceed.No
        }
    }

    private fun CoroutineScope.onColorCenterSessionStarted(seed: Color) {
        val components = requireNotNull(colorCenterComponentsStore.components)
        ccSessionStore.set(
            SessionState.BeingBuilt(seed, coroutineContext.job)
        )
        launch(defaultDispatcher) {
            val event = components.colorDetailsEventStore.eventFlow
                .filterIsInstance<ColorDetailsEvent.DataFetched>()
                .first { it.domainDetails.color == seed }
            val relatedColors = setOf(event.domainDetails.exact.color)
            ensureActive()
            ccSessionStore.set(
                SessionState.Ongoing(session = ColorCenterSession(seed, relatedColors))
            )
        }
        launch(defaultDispatcher) {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    private fun onColorCenterSessionEnded() {
        ccSessionStore.set(SessionState.NoSession)
        colorCenterComponentsStore.disposeComponents()
    }

    // private extension for HomeViewModel, which always sends a color with null 'from'
    private fun ColorInputMediator.send(color: Color?) {
        this.send(color = color, from = null)
    }

    private fun Job.setToJobWithProceed() {
        jobWithProceed?.cancel()
        jobWithProceed = this
    }

    private suspend fun ColorCenterComponentsConsumerRegistry.suspendUntilAllConsumed() {
        val components = colorCenterComponentsStore.components
        this.suspendUntilAllConsumed(components)
    }

    private fun Color.doesBelongToCurrentSession(): Boolean {
        val color = this
        val sessionState = ccSessionStore.sessionState
        return when (sessionState) {
            is SessionState.NoSession -> false // no session -> nothing to belong to
            is SessionState.BeingBuilt -> (sessionState.seed == color) // started this session
            is SessionState.Ongoing -> with(doesColorBelongToSession) { color doesBelongTo sessionState.session }
        }
    }

    private inner class ColorInputSubmitActionImpl : ColorInputSubmitAction {
        override fun invoke(
            colorInput: ColorInput,
            validationResult: ColorInputValidationResult,
        ): Boolean {
            when (validationResult) {
                is ColorInputValidationResult.Valid -> {
                    viewModelScope.launch(defaultDispatcher) {
                        dataUpdateGuard.withCounter {
                            val color = validationResult.color
                            proceedInNewColorCenterSession(color, colorRole = null)
                            componentsConsumerRegistry.suspendUntilAllConsumed()
                        }
                    }.also { job ->
                        job.setToJobWithProceed()
                    }
                    return true
                }
                is ColorInputValidationResult.Invalid -> {
                    _dataFlow.update {
                        val result = HomeData.ProceedResult.InvalidSubmittedColor(
                            discard = ::clearProceedResult,
                        )
                        it.copy(proceedResult = result)
                    }
                    return false
                }
            }
        }
    }

    /** A collection of [SuspendGate]s for [HomeViewModel]. */
    data class SuspendGates(
        val gateForFlowOfColorCenterViewModel: SuspendGate,
        val gateForCollectColorCenterComponent: SuspendGate,
        val gateForDataUpdateGuard: SuspendGate,
    )
}

@Module
@InstallIn(ViewModelComponent::class)
internal object HomeViewModelDiModule {

    @Provides
    @ChannelForColorPreview
    fun provideColorProcessedConfirmationChannelForColorPreview(): Channel<Color?> =
        Channel<Color?>(Channel.UNLIMITED)

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ChannelForColorPreview

    @Provides
    fun provideSuspendGates(): HomeViewModel.SuspendGates =
        HomeViewModel.SuspendGates(
            gateForFlowOfColorCenterViewModel = OpenSuspendGate,
            gateForCollectColorCenterComponent = OpenSuspendGate,
            gateForDataUpdateGuard = OpenSuspendGate,
        )
}

/**
 * Tracks the number of ongoing data updates using reference counting technique.
 *
 * [HomeViewModel] may update its data multiple times during the same factual data transaction.
 * Having a counter of ongoing updates that data consumer (View) takes into account ensures that
 * consumer won't collect unstable, inconsistent data that is about to change, because data transaction
 * is still running.
 *
 * Ensures correct behaviour in concurrent execution when multiple data updates are
 * running in parallel.
 * Finishing one won't falsely signal that all are done (as it would've been with a simple boolean).
 */
private class DataUpdateGuard(
    private val gate: SuspendGate,
) {

    private var numberOfOngoingUpdates = 0
    private val mutexForNumberOfOngoingUpdates = Mutex()
    val flowOfIsDataBeingUpdated = MutableStateFlow<Boolean>(value = isDataBeingUpdated())

    suspend inline fun withCounter(block: () -> Unit) {
        updateNumberOfOngoingUpdates { it + 1 }
        try {
            block()
        } finally {
            updateNumberOfOngoingUpdates { it - 1 }
            assert(numberOfOngoingUpdates >= 0)
        }
    }

    private suspend fun updateNumberOfOngoingUpdates(newNumber: (Int) -> Int) {
        gate.awaitOpen()
        mutexForNumberOfOngoingUpdates.withLock {
            numberOfOngoingUpdates = newNumber(numberOfOngoingUpdates)
            flowOfIsDataBeingUpdated.update { isDataBeingUpdated() }
        }
    }

    private fun isDataBeingUpdated() =
        numberOfOngoingUpdates > 0
}

/**
 * Coordinates confirmation from multiple consumers that a shared [ColorCenterComponents] instance
 * has been received and processed.
 *
 * This registry is used to ensure that all [register]ed consumers have consumed the latest components
 * before proceeding. It provides a method [suspendUntilAllConsumed] to suspend until
 * every consumer has acknowledged the most recent components.
 * This avoids relying on flow emission timing (that may get delayed due to non-deterministic
 * CPU scheduling) to guarantee that all consumers have processed the latest components before
 * [DataUpdateGuard] finishes ongoing data transaction.
 *
 * This "await confirmation from consumer" approach, rather than simple
 * `flowOfMappedComponents.first { ... }`, avoids making a transitive assumption about the
 * implementation of consumers (the fact that they employ / depend on components).
 */
private class ColorCenterComponentsConsumerRegistry {

    private val mapOfConsumersToChannels =
        mutableMapOf<ConsumerId, ReceiveChannel<ColorCenterComponents?>>()

    fun register(consumer: ConsumerId): SendChannel<ColorCenterComponents?> {
        require(mapOfConsumersToChannels[consumer] == null) { "Consumer is already registered" }
        val consumedConfirmationChannel = Channel<ColorCenterComponents?>(Channel.CONFLATED)
        mapOfConsumersToChannels[consumer] = consumedConfirmationChannel
        return consumedConfirmationChannel
    }

    suspend fun suspendUntilAllConsumed(components: ColorCenterComponents?) {
        coroutineScope {
            mapOfConsumersToChannels.values.forEach { consumedConfirmationChannel ->
                launch {
                    consumedConfirmationChannel.receiveAllUntil(components)
                }
            }
        }
    }

    @JvmInline
    value class ConsumerId(val value: Any)
}

private class ColorCenterSessionStore {

    private val _flowOfSessionState = MutableStateFlow<SessionState>(SessionState.NoSession)
    val flowOfSessionState = _flowOfSessionState.asStateFlow()

    fun set(state: SessionState) {
        _flowOfSessionState.update { currentState ->
            if (currentState is SessionState.BeingBuilt) {
                currentState.job.cancel()
            }
            return@update state
        }
    }

    sealed interface SessionState {
        data object NoSession : SessionState
        data class BeingBuilt(val seed: Color, val job: Job) : SessionState
        data class Ongoing(val session: ColorCenterSession) : SessionState
    }
}

private val ColorCenterSessionStore.sessionState: SessionState
    get() = this.flowOfSessionState.value

/**
 * Creates an instance of [HomeData.ProceedResult.Success.ColorData].
 * It is a part of the internal [HomeViewModel] implementation, but is extracted into an injectable
 * component to enable mocking in unit tests.
 */
/* private but Dagger */
@Singleton
class CreateColorDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        HomeData.ProceedResult.Success.ColorData(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}