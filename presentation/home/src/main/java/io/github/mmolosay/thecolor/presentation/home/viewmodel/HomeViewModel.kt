package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterComponentsConsumerRegistry.ConsumerId
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ColorSchemeSelectedSwatchData
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.github.mmolosay.thecolor.utils.cache.CacheStore
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.receiveAllUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
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
 *
 * @param emissionGateForFlowOfColorCenterViewModel is used in unit tests to simulate a possible
 * delay when processing values from the upstream flow due to non-deterministic CPU scheduling.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    colorInputMediatorFactory: ColorInputMediator.Factory,
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorProcessedConfirmationChannelForColorPreview: Channel<Color?>,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    @Named("flowOfColorCenterViewModel") emissionGateForFlowOfColorCenterViewModel: SuspendGate,
    private val proceedExecutorFactory: ProceedExecutor.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val colorFactory: ColorFactory,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val dataUpdateGuard = DataUpdateGuard()
    val flowOfIsDataBeingUpdated: StateFlow<Boolean> = kotlin.run {
        // mapping StateFlow to StateFlow involves boilerplate 'stateIn()':
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2631
        fun value(numberOfOngoingUpdates: Int): Boolean =
            (numberOfOngoingUpdates > 0)
        val upstream = dataUpdateGuard.flowOfOngoingUpdates
        val initialValue = value(upstream.value)
        upstream
            .map(::value)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)
    }

    val cacheStore = CacheStore()

    private val _navEventFlow = MutableStateFlow<HomeNavEvent?>(null)
    val navEventFlow = _navEventFlow.asStateFlow()

    private val colorInputMediator: ColorInputMediator =
        colorInputMediatorFactory.create(
            colorInputColorStore = colorInputColorStore,
        )
    private val flowOfProcessedColorsFromColorInput =
        MutableStateFlow<Color?>(colorInputColorStore.colorFlow.value)

    val colorInputViewModel: ColorInputViewModel =
        colorInputViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorInputEventStore = colorInputEventStore,
            colorInputMediator = colorInputMediator,
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
        val componentsConsumedConfirmationChannel = kotlin.run {
            val consumerId = ConsumerId("colorCenterViewModelFlow")
            componentsConsumerRegistry.register(consumerId)
        }
        colorCenterComponentsStore.componentsFlow
            .transformLatest { components ->
                emissionGateForFlowOfColorCenterViewModel.awaitOpen()
                withContext(NonCancellable) {
                    emit(components?.colorCenterViewModel)
                    componentsConsumedConfirmationChannel.send(components)
                }
            }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
    }

    /*
     * Having this as 'StateFlow' rather than as a simple variable solves race condition of
     * read-write from/into the variable. It also allows suspending the read operation until
     * the write has happened, so that not-null value can be read after suspension.
     */
    private var proceedExecutorFlow = MutableStateFlow<ProceedExecutor?>(null)
    private val ccSessionStore = ColorCenterSessionStore()
    private var jobWithProceed: Job? = null
    private val colorInputOrchestrator = ColorInputOrchestrator()

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
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
        colorInputOrchestrator.mutex.withLock {
            dataUpdateGuard.withCounter {
                try {
                    if (!color.doesBelongToOngoingSession()) {
                        _dataFlow.update {
                            it.copy(
                                canProceed = CanProceed(colorFromColorInput = color),
                                proceedResult = null, // 'proceed' wasn't invoked for new color yet
                            )
                        }
                        onColorCenterSessionEnded()
                    }
                } finally {
                    colorInputOrchestrator.onColorProcessed(color)
                    flowOfProcessedColorsFromColorInput.emit(color)
                    colorProcessedConfirmationChannelForColorPreview.receiveAllUntil(color)
                }
            }
        }
    }

    private fun collectEventsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputEventStore.eventFlow
                .collect(::onEventFromColorInput)
        }

    private fun onEventFromColorInput(event: ColorInputEvent) {
        when (event) {
            is ColorInputEvent.Submit -> {
                val hasProceeded = onColorInputSubmitted(event.colorInputState)
                event.onConsumed(wasAccepted = hasProceeded)
            }
        }
    }

    private fun onColorInputSubmitted(
        colorInputState: ColorInputState,
    ): Boolean {
        if (colorInputState is ColorInputState.Valid) {
            viewModelScope.launch(defaultDispatcher) {
                dataUpdateGuard.withCounter {
                    val color = colorInputState.color
                    // even though new color from Color Input MAY belong to the ongoing session,
                    // it's not produced from the "seed" of the ongoing session, thus logically it's a new one
                    onColorCenterSessionStarted(color)
                    proceed(color = color, colorRole = null)
                    componentsConsumerRegistry.suspendUntilAllConsumed()
                }
            }.also { job ->
                job.setToJobWithProceed()
            }
            return true
        } else {
            _dataFlow.update {
                val result = HomeData.ProceedResult.InvalidSubmittedColor(
                    discard = ::clearProceedResult,
                )
                it.copy(proceedResult = result)
            }
            return false
        }
    }

    private fun collectColorCenterComponents() =
        viewModelScope.launch(defaultDispatcher) {
            colorCenterComponentsStore.componentsFlow.collectLatest { components ->
                // subscribe to new dependencies once new Color Center is created.
                if (components != null) {
                    coroutineScope {
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
                }
                if (components != null) {
                    proceedExecutorFlow.value = proceedExecutorFactory.create(
                        colorDetailsCommandStore = components.colorDetailsCommandStore,
                        colorSchemeCommandStore = components.colorSchemeCommandStore,
                    )
                } else {
                    // components == null
                    proceedExecutorFlow.value = null
                }
            }
        }

    private fun onEventFromColorDetailsOfColorCenter(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected ->
                viewModelScope.launch(defaultDispatcher) {
                    dataUpdateGuard.withCounter {
                        val color = event.color
                        sendColorToColorInput(color)
                        var wereNewComponentsCreated = false
                        if (!color.doesBelongToOngoingSession()) {
                            onColorCenterSessionStarted(color)
                            wereNewComponentsCreated = true
                        }
                        proceed(color = color, colorRole = event.colorRole)
                        if (wereNewComponentsCreated) {
                            componentsConsumerRegistry.suspendUntilAllConsumed()
                        }
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
                .flowOfResumeFromLastSearchedColorOnStartup()
                .first()
            val enabled = resumeFromLastSearchedColorOnStartup.enabled
            if (!enabled) return@launch
            val color = lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
            dataUpdateGuard.withCounter {
                sendColorToColorInput(color)
                // even though last searched color MAY belong to the ongoing session,
                // it's not produced from the "seed" of the ongoing session, thus logically it's a new one
                onColorCenterSessionStarted(color)
                proceed(color = color, colorRole = null)
                componentsConsumerRegistry.suspendUntilAllConsumed()
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /** Variation that takes current color of Color Input. */
    private fun proceed() {
        viewModelScope.launch(defaultDispatcher) {
            dataUpdateGuard.withCounter {
                val color = requireNotNull(colorInputColorStore.colorFlow.value)
                onColorCenterSessionEnded() // end current session (if any)
                onColorCenterSessionStarted(color)
                proceed(color = color, colorRole = null)
                componentsConsumerRegistry.suspendUntilAllConsumed()
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /**
     * Wraps execution of [ProceedExecutor] in accompanying, ViewModel-specific logic,
     * like updating exposed data.
     */
    private suspend fun proceed(
        color: Color,
        colorRole: ColorRole?,
    ) {
        kotlin.run invokeProceedExecutor@{
            val proceedExecutor = proceedExecutorFlow.filterNotNull().first()
            proceedExecutor.invoke(
                color = color,
                colorRole = colorRole,
            )
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
            val color = colorFactory.random()
            val shouldProceed = userPreferencesRepository
                .flowOfAutoProceedWithRandomizedColors()
                .first().enabled
            dataUpdateGuard.withCounter {
                sendColorToColorInput(color)
                if (shouldProceed) {
                    // even though new randomized color MAY belong to the ongoing session,
                    // it's not produced from the "seed" of the ongoing session, thus logically it's a new one
                    onColorCenterSessionStarted(color)
                    proceed(color = color, colorRole = null)
                    componentsConsumerRegistry.suspendUntilAllConsumed()
                }
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    private fun setGoToSettingsNavEvent() {
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        val event = HomeNavEvent.GoToSettings(
            onConsumed = ::clearNavEvent,
        )
        _navEventFlow.value = event
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

    private fun clearNavEvent() {
        _navEventFlow.value = null
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
            requestToGoToSettings = ::setGoToSettingsNavEvent,
        )
    }

    private fun CanProceed(colorFromColorInput: Color?): CanProceed {
        val hasColorInColorInput = (colorFromColorInput != null)
        return when (hasColorInColorInput) {
            true -> CanProceed.Yes(proceed = this::proceed)
            false -> CanProceed.No
        }
    }

    private fun onColorCenterSessionStarted(seed: Color) {
        // recreate Color Center ViewModel (and its sub-feature ViewModels) to reset their states
        colorCenterComponentsStore.createNewComponents()
        viewModelScope.launch(defaultDispatcher) {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
        viewModelScope.launch(defaultDispatcher, start = CoroutineStart.UNDISPATCHED) buildSession@{
            ccSessionStore.cancelAndClearSession()
            ccSessionStore.sessionState = SessionState.BeingBuilt(seed, coroutineContext.job)
            val components = requireNotNull(colorCenterComponentsStore.components)
            val event = components.colorDetailsEventStore.eventFlow
                .filterIsInstance<ColorDetailsEvent.DataFetched>()
                .first { it.domainDetails.color == seed }
            val relatedColors = setOf(event.domainDetails.exact.color)
            val newSession = ColorCenterSession(seed, relatedColors)
            val newSessionState = SessionState.Ongoing(newSession)
            ensureActive()
            ccSessionStore.sessionState = newSessionState
        }
    }

    private fun onColorCenterSessionEnded() {
        ccSessionStore.cancelAndClearSession()
        colorCenterComponentsStore.disposeComponents()
    }

    private suspend fun sendColorToColorInput(
        color: Color,
    ) {
        colorInputOrchestrator.mutex.withLock {
            val wouldColorFlowEmitThisColor = colorInputColorStore.wouldEmitIfSet(color)
            colorInputMediator.send(color = color, from = null)
            colorInputOrchestrator.onColorSentToColorInput(
                color = color,
                wouldColorFlowEmitThisColor = wouldColorFlowEmitThisColor,
            )
        }
        colorInputOrchestrator.suspendUntilAllSentColorsAreProcessed()
    }

    private fun Job.setToJobWithProceed() {
        jobWithProceed?.cancel()
        jobWithProceed = this
    }

    private fun Color?.doesBelongToOngoingSession(): Boolean {
        val color = this ?: return false
        val session = (ccSessionStore.sessionState as? SessionState.Ongoing)?.session ?: return false
        return with(doesColorBelongToSession) { color doesBelongTo session }
    }

    private suspend fun ColorCenterComponentsConsumerRegistry.suspendUntilAllConsumed() {
        val components = colorCenterComponentsStore.components
        this.suspendUntilAllConsumed(components)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object HomeViewModelDiModule {

    @Provides
    fun provideColorProcessedConfirmationChannelForColorPreview(): Channel<Color?> =
        Channel<Color?>(Channel.UNLIMITED)

    @Provides
    @Named("flowOfColorCenterViewModel")
    fun provideEmissionGateForFlowOfColorCenterViewModel(): SuspendGate =
        OpenSuspendGate
}

/**
 * Coordinates updates to and from [ColorInputMediator].
 * [HomeViewModel] both sends colors to mediator and collects them from it.
 * Both (emission and collection) must be coordinated with each other to avoid race condition.
 */
private class ColorInputOrchestrator {

    /**
     * Set of colors that were sent to [ColorInputMediator] from [HomeViewModel],
     * but not yet collected and processed in [HomeViewModel.onColorFromColorInput].
     */
    private val flowOfSentButNotYetProcessedColors = MutableStateFlow(emptySet<Color>())

    /*
     * 1. color is sent to ColorInputMediator
     * 2. it is reported via 'onColorSentToColorInput()'
     * 3. color from Color Input is received in 'HomeViewModel.onColorFromColorInput()'
     * Sometimes step 3 may perform quicker than step 2, thus 'onColorProcessedFromColorInput()'
     * is called before 'onColorSentToColorInput()'. Mutex helps to mitigate that.
     */
    val mutex = Mutex()

    @Synchronized
    fun onColorSentToColorInput(
        color: Color,
        wouldColorFlowEmitThisColor: Boolean,
    ) {
        // if color is not emitted after being sent, then color won't be processed,
        // and then it will stay in the set forever if we add it there
        if (wouldColorFlowEmitThisColor) {
            flowOfSentButNotYetProcessedColors.update { set ->
                set + color
            }
        }
    }

    @Synchronized
    fun onColorProcessed(color: Color?) {
        if (color == null) return // Set<Color> doesn't contain nulls, so nothing to remove
        flowOfSentButNotYetProcessedColors.update { set ->
            set - color
        }
    }

    suspend fun suspendUntilAllSentColorsAreProcessed() {
        val thereAreNoUnprocessedColors = kotlin.run {
            val list = flowOfSentButNotYetProcessedColors.value
            list.isEmpty()
        }
        if (thereAreNoUnprocessedColors) return // fast route without suspension
        flowOfSentButNotYetProcessedColors.first { it.isEmpty() }
    }
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
private class DataUpdateGuard {
    val flowOfOngoingUpdates = MutableStateFlow(0)

    inline fun withCounter(block: () -> Unit) {
        flowOfOngoingUpdates.update { it + 1 }
        try {
            block()
        } finally {
            flowOfOngoingUpdates.update { it - 1 }
            assert(flowOfOngoingUpdates.value >= 0)
        }
    }
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
                launch { consumedConfirmationChannel.receiveAllUntil(components) }
            }
        }
    }

    @JvmInline
    value class ConsumerId(val value: Any)
}

private class ColorCenterSessionStore {

    var sessionState: SessionState = SessionState.NoSession
        @Synchronized get
        @Synchronized set

    sealed interface SessionState {
        data object NoSession : SessionState
        data class BeingBuilt(val seed: Color, val job: Job) : SessionState
        data class Ongoing(val session: ColorCenterSession) : SessionState
    }

    @Synchronized
    fun cancelAndClearSession() {
        val sessionState = this.sessionState
        (sessionState as? SessionState.BeingBuilt)?.job?.cancel()
        this.sessionState = SessionState.NoSession
    }
}

/**
 * Simple wrapper that is used to be more literate in unit tests.
 * Instead of verifying that command was sent to [ColorDetailsCommandStore] as the result of
 * invoking [HomeViewModel.proceed], we can verify that [ProceedExecutor] was called.
 */
/* private but Dagger */
class ProceedExecutor @AssistedInject constructor(
    @Assisted private val colorDetailsCommandStore: ColorDetailsCommandStore,
    @Assisted private val colorSchemeCommandStore: ColorSchemeCommandStore,
) {

    suspend operator fun invoke(
        color: Color,
        colorRole: ColorRole?,
    ) {
        kotlin.run sendToColorDetails@{
            val command = ColorDetailsCommand.FetchData(color, colorRole)
            colorDetailsCommandStore.issue(command)
        }
        kotlin.run sendToColorScheme@{
            val command = ColorSchemeCommand.FetchData(color)
            colorSchemeCommandStore.issue(command)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            colorDetailsCommandStore: ColorDetailsCommandStore,
            colorSchemeCommandStore: ColorSchemeCommandStore,
        ): ProceedExecutor
    }
}

/** Creates instance of [HomeData.ProceedResult.Success.ColorData]. */
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