package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorRole
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
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.firstPronto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    colorInputMediatorFactory: ColorInputMediator.Factory,
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val proceedExecutorFactory: ProceedExecutor.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorCenterSessionBuilder: ColorCenterSessionBuilder,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val _navEventFlow = MutableStateFlow<HomeNavEvent?>(null)
    val navEventFlow = _navEventFlow.asStateFlow()

    private val colorInputMediator: ColorInputMediator =
        colorInputMediatorFactory.create(
            colorInputColorStore = colorInputColorStore,
        )

    val colorInputViewModel: ColorInputViewModel =
        colorInputViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorInputEventStore = colorInputEventStore,
            colorInputMediator = colorInputMediator,
        )

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorFlow = colorInputColorStore.colorFlow,
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> =
        colorCenterComponentsStore.componentsFlow
            .map { it?.colorCenterViewModel }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    /*
     * Having this as 'StateFlow' rather than as a simple variable solves race condition of
     * read-write from/into the variable. It also allows suspending the read operation until
     * the write has happened, so that not-null value can be read after suspension.
     */
    private var proceedExecutorFlow = MutableStateFlow<ProceedExecutor?>(null)
    private var colorCenterSession: ColorCenterSession? = null
    private var orchestrator = Orchestrator()
    private var dataFetchedEventProcessor: DataFetchedEventProcessor? = initialDataFetchedEventProcessor()

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
        collectColorCenterComponents()
        proceedWithLastSearchedColor()
    }

    private fun collectColorsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputColorStore.colorFlow
                .drop(1) // replayed value
                .collect(::onColorFromColorInput)
        }

    private suspend fun onColorFromColorInput(color: Color?) {
        orchestrator.colorInputMutex.withLock {
            try {
                val session = colorCenterSession
                if (session != null && color != null &&
                    with(doesColorBelongToSession) { color doesBelongTo session }
                ) {
                    return // ignore re-emitted color or colors that are part of ongoing session
                }

                _dataFlow.update {
                    it.copy(
                        canProceed = CanProceed(colorFromColorInput = color),
                        proceedResult = null, // 'proceed' wasn't invoked for new color yet
                    )
                }
                onColorCenterSessionEnded()
            } finally {
                orchestrator.onColorProcessedFromColorInput(color)
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
                    val coroutineScope = components.colorCenterCoroutineScope
                    coroutineScope.launch(defaultDispatcher) {
                        val eventStore = components.colorDetailsEventStore
                        eventStore.eventFlow.collect(::onEventFromColorDetailsOfColorCenter)
                    }
                    coroutineScope.launch(defaultDispatcher) {
                        val eventStore = components.colorSchemeEventStore
                        eventStore.eventFlow.collect(::onEventFromColorScheme)
                    }
                    coroutineScope.launch(defaultDispatcher) {
                        val eventStore = components.selectedSwatchColorDetailsEventStore
                        eventStore.eventFlow.collect(::onEventFromColorDetailsOfSelectedSwatch)
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
            is ColorDetailsEvent.DataFetched -> {
                dataFetchedEventProcessor?.process(event)
            }
            is ColorDetailsEvent.ColorSelected -> {
                sendColorToColorInput(color = event.color)
                proceed(
                    color = event.color,
                    colorRole = event.colorRole,
                    isNewColorCenterSession = false, // atm all colors from this event are part of the ongoing session
                )
            }
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
                commandStore.issue(command) // TODO: this piece of code is hard to test; refactor HomeViewModel?

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

    private fun proceedWithLastSearchedColor() {
        viewModelScope.launch(defaultDispatcher) {
            val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
                .flowOfResumeFromLastSearchedColorOnStartup()
                .first()
            val enabled = resumeFromLastSearchedColorOnStartup.enabled
            if (!enabled) return@launch
            val lastSearchedColor =
                lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
            sendColorToColorInput(color = lastSearchedColor)
            proceed(
                color = lastSearchedColor,
                colorRole = null,
                isNewColorCenterSession = true,
            )
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
        orchestrator.suspendUntilProceedIsAllowed()
        if (isNewColorCenterSession) {
            onColorCenterSessionStarted(color)
        }
        kotlin.run invokeProceedExecutor@{
            val proceed = proceedExecutorFlow
                .filterNotNull()
                .firstPronto()
            proceed(
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

    private fun setGoToSettingsNavEvent() {
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
            colorSchemeSelectedSwatchData = null, // no selected swatch initially
            goToSettings = ::setGoToSettingsNavEvent,
        )
    }

    private fun CanProceed(colorFromColorInput: Color?): CanProceed {
        val hasColorInColorInput = (colorFromColorInput != null)
        return when (hasColorInColorInput) {
            true -> CanProceed.Yes(proceed = this::proceed)
            false -> CanProceed.No
        }
    }

    private fun initialDataFetchedEventProcessor(): DataFetchedEventProcessor? =
        null

    @Synchronized
    private fun onColorCenterSessionStarted(seed: Color) {
        colorCenterSessionBuilder.seed(seed)
        // recreate Color Center ViewModel (and its sub-feature ViewModels) to reset their states
        colorCenterComponentsStore.createNewComponents()
        kotlin.run setProcessor@{
            val currentProcessor = dataFetchedEventProcessor // capture in closure
            // implementation of a "Composite" design pattern
            dataFetchedEventProcessor = DataFetchedEventProcessor { event ->
                BuildColorCenterSession().process(event)
                dataFetchedEventProcessor = currentProcessor // restore previous value
            }
        }
        // only persist a seed of each new session
        viewModelScope.launch(defaultDispatcher) {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    @Synchronized
    private fun onColorCenterSessionEnded() {
        colorCenterSession = null
        colorCenterSessionBuilder.clear()
        dataFetchedEventProcessor = initialDataFetchedEventProcessor()
        colorCenterComponentsStore.disposeComponents()
    }

    private suspend fun sendColorToColorInput(
        color: Color,
    ) {
        orchestrator.colorInputMutex.withLock {
            withContext(uiDataUpdateDispatcher) {
                colorInputMediator.send(color = color, from = null)
            }
            orchestrator.onColorSentToColorInput(color)
        }
    }

    /**
     * A [DataFetchedEventProcessor] that creates a [ColorCenterSession]
     * and sets it into a [colorCenterSession] field.
     */
    private fun BuildColorCenterSession() =
        DataFetchedEventProcessor { event ->
            val details = event.domainDetails
            val relatedColors = setOf(details.exact.color)
            colorCenterSession = colorCenterSessionBuilder
                .relatedColors(relatedColors)
                .build()
        }
}

/**
 * Coordinates different parts of [HomeViewModel] so that they are executed in controlled
 * manner. Components will wait for the ones they depend on to be executed first.
 */
private class Orchestrator {

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
    val colorInputMutex = Mutex()

    @Synchronized
    fun onColorSentToColorInput(color: Color) {
        flowOfSentButNotYetProcessedColors.update { list ->
            list + color
        }
    }

    @Synchronized
    fun onColorProcessedFromColorInput(color: Color?) {
        flowOfSentButNotYetProcessedColors.update { list ->
            list.toMutableList().also {
                it.asReversed().remove(color) // remove latest entry
            }
        }
    }

    suspend fun suspendUntilProceedIsAllowed() {
        suspendUntilAllSentColorsAreProcessed()
    }

    private suspend fun suspendUntilAllSentColorsAreProcessed() {
        val list = flowOfSentButNotYetProcessedColors.value
        val thereAreNoUnprocessedColors = (list.isEmpty())
        if (thereAreNoUnprocessedColors) return
        flowOfSentButNotYetProcessedColors.first { it.isEmpty() }
        return // explicit return to have a place for breakpoint after the suspension
    }
}

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

/**
 * Specifies the way of processing (handling, reacting to) a [ColorDetailsEvent.DataFetched] event.
 * It is an implementation of a "Strategy" design pattern.
 * See [HomeViewModel.dataFetchedEventProcessor].
 */
private fun interface DataFetchedEventProcessor {
    fun process(event: ColorDetailsEvent.DataFetched)
}