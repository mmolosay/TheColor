package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Store
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ColorSchemeSelectedSwatchData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel.CoroutineRegistryRules.trackAsConsumeColorCenterComponents
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel.CoroutineRegistryRules.trackAsProceed
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupDataFactory
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.MutableConsumableStore
import io.github.mmolosay.thecolor.utils.OperationCounter
import io.github.mmolosay.thecolor.utils.asConsumableStore
import io.github.mmolosay.thecolor.utils.removeAndCancelAll
import io.github.mmolosay.thecolor.utils.trackThisAsSingleActive
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

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
    colorInputGroupDataFactory: ColorInputGroupDataFactory,
    colorInputGroupViewModelFactory: ColorInputGroupViewModel.Factory,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorComparator: ColorComparator,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    private val _workingDataFlow = MutableStateFlow(_dataFlow.value)
    val dataFlow = _dataFlow.asStateFlow()

    private val _effectStore = MutableConsumableStore<HomeEffect>()
    val effectStore = _effectStore.asConsumableStore()

    private val dataUpdateCounter = OperationCounter { counter, _ ->
        val areAllUpdatesFinished = (counter == 0)
        if (areAllUpdatesFinished) {
            _dataFlow.value = _workingDataFlow.value
        }
    }

    val colorInputGroupViewModel: ColorInputGroupViewModel = run {
        val data = colorInputGroupDataFactory.create()
        val store = Store(data)
        colorInputGroupViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            store = store,
            mediator = colorInputMediator,
            submitAction = ColorInputSubmitActionImpl(),
        )
    }

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            store = Store<ColorPreviewData?>(null)
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    private val _colorCenterViewModelFlow = MutableStateFlow<ColorCenterViewModel?>(null)
    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> = _colorCenterViewModelFlow.asStateFlow()

    private val opRegistry = CoroutineRegistry<Operation>()
    private val ccSessionStore = ColorCenterSessionStore()

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
        dataUpdateCounter.withCounter {
            _workingDataFlow.update {
                val canProceed = CanProceed(colorFromColorInput = color)
                it.copy(canProceed = canProceed)
            }
            if (color == null || !color.doesBelongToCurrentSession()) {
                clearProceedResult() // 'proceed' wasn't invoked for new color yet
                endColorCenterSession()
            }
            colorPreviewViewModel.setColor(color).join()
        }
    }

    private fun onEventFromColorDetailsOfColorCenter(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected ->
                viewModelScope.launch(defaultDispatcher) {
                    opRegistry.trackAsProceed {
                        ccSessionStore.sessionState.mustBeOngoing()
                        dataUpdateCounter.withCounter {
                            val color = event.color
                            colorInputMediator.set(color)
                            // assuming any color selected belongs to ongoing session
                            proceed(color) { colorDetails, colorScheme ->
                                colorDetails.selectColor(event.colorRole)
                                colorScheme.fetchColorScheme(color)
                            }
                        }
                    }
                }
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onEventFromColorScheme(event: ColorSchemeEvent) {
        viewModelScope.launch(defaultDispatcher) {
            when (event) {
                is ColorSchemeEvent.SwatchSelected -> {
                    val viewModel = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsViewModel
                        ?: return@launch
                    viewModel.setSeedDetails(event.swatchColorDetails)
                    val selectedSwatchColorDetailsViewModel = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsViewModel
                        ?: return@launch
                    _workingDataFlow.update {
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

    @Suppress("RedundantSuspendModifier")
    private suspend fun onEventFromColorDetailsOfSelectedSwatch(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected -> {
                val viewModel = colorCenterComponentsStore.components
                    ?.selectedSwatchColorDetailsViewModel
                    ?: return
                viewModel.selectColor(event.colorRole)
            }
        }
    }

    private fun maybeProceedWithLastSearchedColor() {
        viewModelScope.launch(defaultDispatcher) {
            opRegistry.trackAsProceed {
                val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
                    .flowOfResumeFromLastSearchedColorOnStartup
                    .filterReady()
                    .first().getOrElse { DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup }
                val enabled = resumeFromLastSearchedColorOnStartup.enabled
                if (!enabled) return@launch
                val color = lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
                dataUpdateCounter.withCounter {
                    createAndConsumeNewColorCenterComponents()
                    val deferredDetails = CompletableDeferred<DomainColorDetails>()
                    startColorCenterSession(
                        seed = color,
                        deferredDetails = deferredDetails,
                    )
                    proceed(color) { colorDetails, colorScheme ->
                        colorDetails.setSeedColor(color, deferredDetails)
                        colorScheme.fetchColorScheme(color)
                    }
                    colorInputMediator.set(color)
                }
            }
        }
    }

    /** Variation that takes the current color of Color Input. */
    private fun proceed() {
        viewModelScope.launch(defaultDispatcher) {
            opRegistry.trackAsProceed {
                dataUpdateCounter.withCounter {
                    endColorCenterSession() // end current session (if any)
                    val color = requireNotNull(colorInputMediator.colorState.color)
                    createAndConsumeNewColorCenterComponents()
                    val deferredDetails = CompletableDeferred<DomainColorDetails>()
                    startColorCenterSession(
                        seed = color,
                        deferredDetails = deferredDetails,
                    )
                    proceed(color) { colorDetails, colorScheme ->
                        colorDetails.setSeedColor(color, deferredDetails)
                        colorScheme.fetchColorScheme(color)
                    }
                }
            }
        }
    }

    private suspend fun proceed(
        color: Color,
        colorCenterAction: suspend (ColorDetailsViewModel, ColorSchemeViewModel) -> Unit,
    ) {
        run executeColorCenterAction@{
            val components = requireNotNull(colorCenterComponentsStore.components)
            val colorDetails = components.colorCenterViewModel.colorDetailsViewModel
            val colorScheme = components.colorCenterViewModel.colorSchemeViewModel
            colorCenterAction(colorDetails, colorScheme)
        }
        run updateData@{
            val colorData = createColorData(color)
            val proceedResult = HomeData.ProceedResult.Success(
                colorData = colorData,
            )
            _workingDataFlow.update {
                it.copy(proceedResult = proceedResult)
            }
        }
    }

    private fun randomizeColor() {
        viewModelScope.launch(defaultDispatcher) {
            opRegistry.trackAsProceed {
                val color = getPredictableRandomColor()
                colorInputMediator.withLock { editor ->
                    val shouldProceed = userPreferencesRepository
                        .flowOfAutoProceedWithRandomizedColors
                        .filterReady()
                        .first().getOrElse { DefaultUserPreferences.AutoProceedWithRandomizedColors }
                        .enabled
                    dataUpdateCounter.withCounter {
                        if (shouldProceed) {
                            createAndConsumeNewColorCenterComponents()
                            val deferredDetails = CompletableDeferred<DomainColorDetails>()
                            startColorCenterSession(
                                seed = color,
                                deferredDetails = deferredDetails,
                            )
                            proceed(color) { colorDetails, colorScheme ->
                                colorDetails.setSeedColor(color, deferredDetails)
                                colorScheme.fetchColorScheme(color)
                            }
                        }
                        editor.set(color)
                    }
                }
            }
        }
    }

    private fun onRequestToGoToSettings() {
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        val effect = HomeEffect.GoToSettings
        _effectStore.publish(effect)
    }

    private fun clearProceedResult() {
        _workingDataFlow.update {
            it.copy(proceedResult = null)
        }
    }

    private fun clearColorSchemeSwatchSelectedData() {
        _workingDataFlow.update {
            it.copy(colorSchemeSelectedSwatchData = null)
        }
    }

    private fun initialData(): HomeData {
        val canProceed = kotlin.run {
            val color = colorInputMediator.colorState.color
            CanProceed(colorFromColorInput = color)
        }
        return HomeData(
            canProceed = canProceed,
            proceedResult = null, // 'proceed' action wasn't invoked yet
            randomizeColor = ::randomizeColor,
            colorSchemeSelectedSwatchData = null, // no selected swatch initially
            requestToGoToSettings = ::onRequestToGoToSettings,
        )
    }

    private fun CanProceed(colorFromColorInput: Color?): CanProceed {
        val hasColorInColorInput = (colorFromColorInput != null)
        return when (hasColorInColorInput) {
            true -> CanProceed.Yes(proceed = this::proceed)
            false -> CanProceed.No
        }
    }

    private suspend fun createAndConsumeNewColorCenterComponents() {
        colorCenterComponentsStore.createNewComponents()
        val newComponents = colorCenterComponentsStore.components
        _colorCenterViewModelFlow.emit(newComponents?.colorCenterViewModel)
        viewModelScope.launch(defaultDispatcher) {
            opRegistry.trackAsConsumeColorCenterComponents {
                if (newComponents == null) return@launch
                launch(start = CoroutineStart.UNDISPATCHED) {
                    newComponents.colorCenterViewModel.colorDetailsViewModel.eventFlow
                        .collect(::onEventFromColorDetailsOfColorCenter)
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    newComponents.colorCenterViewModel.colorSchemeViewModel.eventFlow
                        .collect(::onEventFromColorScheme)
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    newComponents.selectedSwatchColorDetailsViewModel.eventFlow
                        .collect(::onEventFromColorDetailsOfSelectedSwatch)
                }
            }
        }
    }

    private suspend fun CoroutineScope.startColorCenterSession(
        seed: Color,
        deferredDetails: Deferred<DomainColorDetails>,
    ) {
        val deferredSessionBuildingScope = CompletableDeferred<ColorCenterSessionStore.SessionBuildingScope>()
        val sessionBuildingJob = launch {
            val sessionBuilding = deferredSessionBuildingScope.await()
            val session = run {
                val seedDetails = runCatching { deferredDetails.await() }.getOrElse {
                    sessionBuilding.cancel() // will also cancel this coroutine
                    return@launch
                }
                with(colorComparator) { require(seed isSameAs seedDetails.color) }
                val relatedColors = setOf(seedDetails.exact.color)
                ColorCenterSession(seed, relatedColors)
            }
            sessionBuilding.complete(session)
        }
        // start building a new session inline suspending to guarantee that
        // by the time this function returns the session store has a new 'building' state
        val sessionBuilding = ccSessionStore.startBuilding(seed, sessionBuildingJob)
        deferredSessionBuildingScope.complete(sessionBuilding)

        launch {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    private suspend fun endColorCenterSession() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
        opRegistry.removeAndCancelAll { it.value is Operation.ConsumeColorCenterComponents }
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
                        opRegistry.trackAsProceed {
                            dataUpdateCounter.withCounter {
                                val color = validationResult.color
                                createAndConsumeNewColorCenterComponents()
                                val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                startColorCenterSession(
                                    seed = color,
                                    deferredDetails = deferredDetails,
                                )
                                proceed(color) { colorDetails, colorScheme ->
                                    colorDetails.setSeedColor(color, deferredDetails)
                                    colorScheme.fetchColorScheme(color)
                                }
                            }
                        }
                    }
                    return true
                }
                is ColorInputValidationResult.Invalid -> {
                    _workingDataFlow.update {
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

    private sealed interface Operation {
        data object Proceed : Operation
        data object ConsumeColorCenterComponents : Operation
    }

    private object CoroutineRegistryRules {

        context(coroutineScope: CoroutineScope)
        suspend inline fun CoroutineRegistry<Operation>.trackAsProceed(
            block: () -> Unit,
        ): Unit =
            this.trackThisAsSingleActive(
                predicate = { it.value is Operation.Proceed },
                value = Operation.Proceed,
                block = block,
            )

        context(coroutineScope: CoroutineScope)
        suspend inline fun CoroutineRegistry<Operation>.trackAsConsumeColorCenterComponents(
            block: () -> Unit,
        ): Unit =
            this.trackThisAsSingleActive(
                predicate = { it.value is Operation.ConsumeColorCenterComponents },
                value = Operation.ConsumeColorCenterComponents,
                block = block,
            )
    }
}

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