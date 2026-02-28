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
import io.github.mmolosay.thecolor.domain.usecase.ColorComparator
import io.github.mmolosay.thecolor.domain.usecase.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ColorSchemeSelectedSwatchData
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.utils.MutableConsumableStore
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.github.mmolosay.thecolor.utils.asConsumableStore
import io.github.mmolosay.thecolor.utils.doNothing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference
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
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val colorInputMediator: ColorInputMediator,
    colorInputGroupViewModelFactory: ColorInputGroupViewModel.Factory,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val gates: SuspendGates,
    private val createColorData: CreateColorDataUseCase,
    private val colorComparator: ColorComparator,
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

    private val _navEventStore = MutableConsumableStore<HomeNavEvent>()
    val navEventStore = _navEventStore.asConsumableStore()

    val colorInputGroupViewModel: ColorInputGroupViewModel =
        colorInputGroupViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            mediator = colorInputMediator,
            submitAction = ColorInputSubmitActionImpl(),
        )

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    private val _colorCenterViewModelFlow = MutableStateFlow<ColorCenterViewModel?>(null)
    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> = _colorCenterViewModelFlow.asStateFlow()

    private val ccSessionStore = ColorCenterSessionStore()
    private val jobWithProceed = AtomicReference<Job?>(null)
    private val jobWithComponentsCollection = AtomicReference<Job?>(null)

    init {
        collectColorsFromColorInput()
        maybeProceedWithLastSearchedColor()
    }

    private fun collectColorsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputMediator.colorStateFlow
                .drop(1) // replayed value
                .collect(::onColorFromColorInput)
        }

    private suspend fun onColorFromColorInput(colorState: ColorInputMediator.ColorState) {
        val color = colorState.color
        dataUpdateGuard.withCounter {
            _dataFlow.update {
                val canProceed = CanProceed(colorFromColorInput = color)
                it.copy(canProceed = canProceed)
            }
            if (color == null || !color.doesBelongToCurrentSession()) {
                clearProceedResult() // 'proceed' wasn't invoked for new color yet
                onColorCenterSessionEnded()
            }
            colorPreviewViewModel.setColor(color)
        }
    }

    private fun onEventFromColorDetailsOfColorCenter(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected ->
                viewModelScope.launch(defaultDispatcher) {
                    dataUpdateGuard.withCounter {
                        val color = event.color
                        colorInputMediator.set(color)
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
                colorInputMediator.set(color)
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /** Variation that takes the current color of Color Input. */
    private fun proceed() {
        viewModelScope.launch(defaultDispatcher) {
            dataUpdateGuard.withCounter {
                val color = requireNotNull(colorInputMediator.colorStateFlow.value.color)
                onColorCenterSessionEnded() // end current session (if any)
                proceedInNewColorCenterSession(color, colorRole = null)
            }
        }.also { job ->
            job.setToJobWithProceed()
        }
    }

    /**
     * Invokes [proceed] action and starts a new Color Center session, which also means
     * new [ColorCenterComponents] are created.
     */
    private suspend fun CoroutineScope.proceedInNewColorCenterSession(
        color: Color,
        colorRole: ColorRole?,
    ) {
        colorCenterComponentsStore.createNewComponents()
        run consumeNewComponents@{
            val newComponents = colorCenterComponentsStore.components
            _colorCenterViewModelFlow.emit(newComponents?.colorCenterViewModel)
            // collect components' flows in a standalone coroutine to decouple it from the 'jobWithProceed'
            viewModelScope.launch(defaultDispatcher) {
                if (newComponents != null) {
                    launch {
                        newComponents.colorDetailsEventStore.eventFlow
                            .collect(::onEventFromColorDetailsOfColorCenter)
                    }
                    launch {
                        newComponents.colorSchemeEventStore.eventFlow
                            .collect(::onEventFromColorScheme)
                    }
                    launch {
                        newComponents.selectedSwatchColorDetailsEventStore.eventFlow
                            .collect(::onEventFromColorDetailsOfSelectedSwatch)
                    }
                }
            }.also { job ->
                jobWithComponentsCollection.getAndSet(job)?.cancel()
            }
        }
        onColorCenterSessionStarted(color)
        proceed(color, colorRole)
    }

    private suspend fun proceed(
        color: Color,
        colorRole: ColorRole?,
    ) {
        val components = requireNotNull(colorCenterComponentsStore.components)
        coroutineScope {
            launch issueCommandToColorDetails@{
                val command = ColorDetailsCommand.FetchData(color, colorRole)
                components.colorDetailsCommandStore.issue(command)
            }
            launch issueCommandToColorScheme@{
                val command = ColorSchemeCommand.FetchData(color)
                components.colorSchemeCommandStore.issue(command)
            }
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
                    colorInputMediator.set(color)
                }
            } else {
                colorInputMediator.set(color)
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
        val event = HomeNavEvent.GoToSettings
        _navEventStore.publish(event)
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
            val color = colorInputMediator.colorStateFlow.value.color
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
        launch(defaultDispatcher, start = CoroutineStart.UNDISPATCHED) {
            ccSessionStore.startBuilding(seed).run {
                val event = components.colorDetailsEventStore.eventFlow
                    .filterIsInstance<ColorDetailsEvent.DataFetched>()
                    .first { it.domainDetails.color == seed }
                val relatedColors = setOf(event.domainDetails.exact.color)
                val session = ColorCenterSession(seed, relatedColors)
                ensureActive()
                complete(session)
            }
        }
        launch(defaultDispatcher) {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    private suspend fun onColorCenterSessionEnded() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
        jobWithComponentsCollection.getAndSet(null)?.cancel()
    }

    // private extension for HomeViewModel, which always sets a color with null 'source'
    private fun ColorInputMediator.set(color: Color?) {
        this.set(color = color, source = null)
    }

    private fun Job.setToJobWithProceed() {
        jobWithProceed.getAndSet(this)?.cancel()
    }

    private fun Color.doesBelongToCurrentSession(): Boolean {
        val color = this
        val sessionState = ccSessionStore.sessionState
        return when (sessionState) {
            is SessionState.NoSession -> false // no session -> nothing to belong to
            is SessionState.BeingBuilt -> with(colorComparator) { color isSameAs sessionState.seed } // started this session
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
        val gateForDataUpdateGuard: SuspendGate,
    )
}

@Module
@InstallIn(ViewModelComponent::class)
internal object HomeViewModelDiModule {

    @Provides
    fun provideSuspendGates(): HomeViewModel.SuspendGates =
        HomeViewModel.SuspendGates(
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

/* 'internal' for testing */
internal class ColorCenterSessionStore {

    private val _flowOfSessionState = MutableStateFlow<SessionState>(SessionState.NoSession)
    val flowOfSessionState = _flowOfSessionState.asStateFlow()

    private val updateStateMutex = Mutex()

    suspend fun clear() =
        updateStateMutex.withLock {
            _flowOfSessionState.update { currentState ->
                currentState.cancelIfBuilding()
                return@update SessionState.NoSession
            }
        }

    suspend fun startBuilding(seed: Color): SessionBuildingScope =
        updateStateMutex.withLock {
            val newState = SessionState.BeingBuilt(seed, currentCoroutineContext().job)
            _flowOfSessionState.update { currentState ->
                currentState.cancelIfBuilding()
                return@update newState
            }
            return SessionBuildingScopeImpl(origin = newState)
        }

    private fun SessionState.cancelIfBuilding() {
        if (this is SessionState.BeingBuilt) {
            this.job.cancel()
        }
    }

    interface SessionBuildingScope {
        suspend fun complete(session: ColorCenterSession)
    }

    private inner class SessionBuildingScopeImpl(
        private val origin: SessionState.BeingBuilt,
    ) : SessionBuildingScope {

        override suspend fun complete(session: ColorCenterSession) =
            updateStateMutex.withLock {
                _flowOfSessionState.update { currentState ->
                    if (currentState != origin) {
                        error("Cannot complete building session in the stale scope")
                    }
                    SessionState.Ongoing(session)
                }
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