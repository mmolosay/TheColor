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
import io.github.mmolosay.thecolor.utils.cache.CacheStore
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.receiveAllUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * A [ViewModel] for 'Home' View.
 * Composed of sub-feature ViewModels of nested Views.
 *
 * It creates objects that are shared between sub-feature ViewModels via assisted injection and
 * factories.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    colorInputMediatorFactory: ColorInputMediator.Factory,
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorProcessedConfirmationChannelForColorPreview: Channel<Color?>,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val proceedExecutorFactory: ProceedExecutor.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorCenterSessionBuilder: ColorCenterSessionBuilder,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val colorFactory: ColorFactory,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val dataUpdateGuard = DataUpdateGuard() // TODO: not all places use it atm
    val flowOfIsDataBeingUpdated: StateFlow<Boolean> = kotlin.run {
        // mapping StateFlow to StateFlow involves boilerplate 'stateIn()':
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2631
        fun value(numberOfOngoingUpdates: Int): Boolean =
            (numberOfOngoingUpdates != 0)
        val upstream = dataUpdateGuard.flowOfOngoingUpdates
        val initialValue = value(upstream.value)
        upstream
            .map(::value)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue)
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
            colorProcessedConfirmation = colorProcessedConfirmationChannelForColorPreview,
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> =
        colorCenterComponentsStore.componentsFlow
            .map { it?.colorCenterViewModel }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = null)

    /*
     * Having this as 'StateFlow' rather than as a simple variable solves race condition of
     * read-write from/into the variable. It also allows suspending the read operation until
     * the write has happened, so that not-null value can be read after suspension.
     */
    private var proceedExecutorFlow = MutableStateFlow<ProceedExecutor?>(null)
    private var colorCenterSession: ColorCenterSession? = null
    private var createNewColorSessionJob: Job? = null
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
                    val belongsToOngoingSession = kotlin.run {
                        val session = colorCenterSession
                        if (session == null || color == null) return@run false
                        with(doesColorBelongToSession) { color doesBelongTo session }
                    }
                    if (!belongsToOngoingSession) {
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
                proceed(
                    color = colorInputState.color,
                    colorRole = null,
                    isNewColorCenterSession = true,
                )
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
            colorCenterComponentsStore.componentsFlow.collect { components ->
                /*
                 * Subscribe to new dependencies once new Color Center is created.
                 * Launch collection coroutines from Color Center coroutine scope,
                 * so when ColorCenterViewModel is disposed of and its coroutine scope is cancelled,
                 * so is the collection job on old instances of Command/Event stores.
                */
                if (components != null) {
                    components.colorCenterCoroutineScope.launch(defaultDispatcher) {
                        launch {
                            val eventStore = components.colorDetailsEventStore
                            eventStore.eventFlow.collect(::onEventFromColorDetailsOfColorCenter)
                        }
                        launch {
                            val eventStore = components.colorSchemeEventStore
                            eventStore.eventFlow.collect(::onEventFromColorScheme)
                        }
                        launch {
                            val eventStore = components.selectedSwatchColorDetailsEventStore
                            eventStore.eventFlow.collect(::onEventFromColorDetailsOfSelectedSwatch)
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

    private suspend fun onEventFromColorDetailsOfColorCenter(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected -> {
                sendColorToColorInput(color = event.color)
                proceed(
                    color = event.color,
                    colorRole = event.colorRole,
                    isNewColorCenterSession = false, // atm all colors from this event are part of the ongoing session
                )
            }
            is ColorDetailsEvent.DataFetched ->
                doNothing() // ignore, handled in onColorCenterSessionStarted()
        }
    }

    private suspend fun onEventFromColorScheme(event: ColorSchemeEvent) {
        when (event) {
            is ColorSchemeEvent.SwatchSelected -> {
                val command = ColorDetailsCommand.SetColorDetails(
                    domainDetails = event.swatchColorDetails,
                )
                val commandStore = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsCommandStore
                    ?: return
                commandStore.issue(command)

                val selectedSwatchColorDetailsViewModel = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsViewModel
                    ?: return
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
            val lastSearchedColor =
                lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
            dataUpdateGuard.withCounter {
                sendColorToColorInput(color = lastSearchedColor)
                proceed(
                    color = lastSearchedColor,
                    colorRole = null,
                    isNewColorCenterSession = true,
                )
            }
        }
    }

    /** Variation that takes current color of Color Input. */
    private fun proceed() {
        viewModelScope.launch(defaultDispatcher) {
            onColorCenterSessionEnded() // end current session (if any)
            val color = requireNotNull(colorInputColorStore.colorFlow.value)
            proceed(
                color = color,
                colorRole = null, // standalone color (without a role)
                isNewColorCenterSession = true, // color from Color Input, thus new session
            )
        }
    }

    /**
     * Wraps execution of [ProceedExecutor] in accompanying, ViewModel-specific logic, like managing
     * color center session and updating exposed data.
     */
    private suspend fun proceed(
        color: Color,
        colorRole: ColorRole?,
        isNewColorCenterSession: Boolean,
    ) {
        colorInputOrchestrator.suspendUntilAllSentColorsAreProcessed()
        if (isNewColorCenterSession) {
            onColorCenterSessionStarted(color)
        }
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
                .first()
                .enabled
            dataUpdateGuard.withCounter {
                sendColorToColorInput(color)
                if (shouldProceed) {
                    proceed(
                        color = color,
                        colorRole = null,
                        isNewColorCenterSession = true,
                    )
                }
            }
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
            val components = requireNotNull(colorCenterComponentsStore.components)
            val event = components.colorDetailsEventStore.eventFlow
                .filterIsInstance<ColorDetailsEvent.DataFetched>()
                .first { it.domainDetails.color == seed }
            val relatedColors = setOf(event.domainDetails.exact.color)
            // TODO: get rid of builder?
            val newSession = colorCenterSessionBuilder
                .seed(seed)
                .relatedColors(relatedColors)
                .build()
            ensureActive()
            colorCenterSession = newSession
        }.also {
            createNewColorSessionJob?.cancel()
            createNewColorSessionJob = it
        }
    }

    private fun onColorCenterSessionEnded() {
        createNewColorSessionJob?.cancel()
        createNewColorSessionJob = null
        colorCenterSession = null
        colorCenterSessionBuilder.clear()
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
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object HomeViewModelDiModule {

    @Provides
    fun provideColorProcessedConfirmationChannelForColorPreview(): Channel<Color?> =
        Channel<Color?>(Channel.UNLIMITED)
}

/**
 * Coordinates updates to and from [ColorInputMediator].
 * [HomeViewModel] both sends colors to mediator and collects them from it.
 * Both (emission and collection) must be coordinated with each other to avoid race condition.
 */
private class ColorInputOrchestrator {

    /**
     * List of colors that were sent to [ColorInputMediator] from [HomeViewModel],
     * but not yet collected and processed in [HomeViewModel.onColorFromColorInput].
     */
    private val flowOfSentButNotYetProcessedColors = MutableStateFlow(emptyList<Color>())

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
        // and then it will stay in the list forever if we add it there
        if (wouldColorFlowEmitThisColor) {
            flowOfSentButNotYetProcessedColors.update { list ->
                list + color
            }
        }
    }

    @Synchronized
    fun onColorProcessed(color: Color?) {
        if (color == null) return // List<Color> doesn't contain nulls, so nothing to remove
        flowOfSentButNotYetProcessedColors.update { list ->
            list.toMutableList().also {
                it.asReversed().remove(color) // remove latest entry
            }
        }
    }

    suspend fun suspendUntilAllSentColorsAreProcessed() {
        val thereAreNoUnprocessedColors = kotlin.run {
            val list = flowOfSentButNotYetProcessedColors.value
            list.isEmpty()
        }
        if (thereAreNoUnprocessedColors) return // fast route
        flowOfSentButNotYetProcessedColors.first { it.isEmpty() }
        return // explicit return to have a place for breakpoint after the suspension
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
        }
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