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
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventHandler
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel.CoroutineRegistryRules.trackAsProceed
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputSource
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupDataFactory
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEventHandler
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.BatchScope
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.batch
import io.github.mmolosay.thecolor.utils.trackThisAsSingleActive
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorComparator: ColorComparator,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val store = Store(initialData())
    val dataFlow: StateFlow<HomeData> = store.flow

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)
    private val seFactory = SideEffectFactory()

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
            store = Store<ColorPreviewData?>(null),
        )

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    private val _colorCenterViewModelFlow = MutableStateFlow<ColorCenterViewModel?>(null)
    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> = _colorCenterViewModelFlow.asStateFlow()

    private val _colorSchemeSwatchDetailsViewModelFlow = MutableStateFlow<ColorDetailsViewModel?>(null)
    val colorSchemeSwatchDetailsViewModelFlow = _colorSchemeSwatchDetailsViewModelFlow.asStateFlow()

    private val opRegistry = CoroutineRegistry<Operation>()
    private val ccSessionStore = ColorCenterSessionStore()

    init {
        maybeProceedWithLastSearchedColor()
        collectColorsFromColorInput()
    }

    private fun maybeProceedWithLastSearchedColor() {
        viewModelScope.launch(defaultDispatcher) {
            opRegistry.trackAsProceed {
                store.batch {
                    val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
                        .flowOfResumeFromLastSearchedColorOnStartup
                        .filterReady()
                        .first().getOrElse { DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup }
                    val enabled = resumeFromLastSearchedColorOnStartup.enabled
                    if (!enabled) return@launch
                    val color = lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
                    createAndConsumeNewColorCenterComponents()
                    val deferredDetails = CompletableDeferred<DomainColorDetails>()
                    startColorCenterSession(
                        seed = color,
                        deferredDetails = deferredDetails,
                    )
                    colorInputMediator.set(color)
                    onColorBecameCurrent(color)
                    proceed(color) { colorDetails, colorScheme ->
                        coroutineScope {
                            launch { colorDetails.setSeedColor(color, deferredDetails) }
                            launch { colorScheme.fetchColorScheme(color) }
                        }
                    }
                }
            }
        }
    }

    private fun collectColorsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputMediator.colorStateFlow
                .drop(1) // replayed value
                .filter { it.source is ColorInputSource }
                .conflate()
                .collect(::onColorFromColorInput)
        }

    private suspend fun onColorFromColorInput(colorState: ColorInputMediator.ColorState) {
        store.batch {
            val color = colorState.color
            endColorCenterSession() // assuming any new color from Color Input is a new session
            onColorBecameCurrent(color)
        }
    }

    fun execute(action: HomeAction): Job =
        viewModelScope.launch(exclusiveLane) {
            when (action) {
                is HomeAction.Proceed -> proceed()
                is HomeAction.RandomizeColor -> randomizeColor()
                is HomeAction.RequestToGoToSettings -> onRequestToGoToSettings()
                is HomeAction.ClearProceedResult -> clearProceedResult()
                is HomeAction.OnSideEffectProcessed -> onSideEffectProcessed(action.se)
                is HomeAction.ClearColorSchemeSelectedSwatch -> clearColorSchemeSelectedSwatch()
            }
        }

    context(coroutineScope: CoroutineScope)
    private suspend fun proceed() {
        val color = colorInputMediator.colorState.color ?: return // invalid state
        opRegistry.trackAsProceed {
            withContext(defaultDispatcher) {
                store.batch {
                    endColorCenterSession()
                    createAndConsumeNewColorCenterComponents()
                    val deferredDetails = CompletableDeferred<DomainColorDetails>()
                    startColorCenterSession(
                        seed = color,
                        deferredDetails = deferredDetails,
                    )
                    colorInputMediator.set(color)
                    onColorBecameCurrent(color)
                    proceed(color) { colorDetails, colorScheme ->
                        coroutineScope {
                            launch { colorDetails.setSeedColor(color, deferredDetails) }
                            launch { colorScheme.fetchColorScheme(color) }
                        }
                    }
                }
            }
        }
    }

    context(batchScope: BatchScope<HomeData>)
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
            batchScope.update {
                it.copy(proceedResult = proceedResult)
            }
        }
    }

    context(coroutineScope: CoroutineScope)
    private suspend fun randomizeColor() {
        opRegistry.trackAsProceed {
            withContext(defaultDispatcher) {
                store.batch {
                    colorInputMediator.withLock { editor ->
                        val color = getPredictableRandomColor()
                        endColorCenterSession()
                        editor.set(color) // continue to hold mediator lock until the execution flow is finished
                        onColorBecameCurrent(color)
                        val shouldProceed = userPreferencesRepository
                            .flowOfAutoProceedWithRandomizedColors
                            .filterReady()
                            .first()
                            .getOrElse { DefaultUserPreferences.AutoProceedWithRandomizedColors }
                            .enabled
                        if (shouldProceed) {
                            createAndConsumeNewColorCenterComponents()
                            val deferredDetails = CompletableDeferred<DomainColorDetails>()
                            startColorCenterSession(
                                seed = color,
                                deferredDetails = deferredDetails,
                            )
                            proceed(color) { colorDetails, colorScheme ->
                                coroutineScope {
                                    launch { colorDetails.setSeedColor(color, deferredDetails) }
                                    launch { colorScheme.fetchColorScheme(color) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun onRequestToGoToSettings() {
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        val se = seFactory.goToSettings()
        store.update {
            it.copy(sideEffects = it.sideEffects + se)
        }
    }

    private suspend fun clearProceedResult() {
        store.update {
            it.copy(proceedResult = null)
        }
    }

    private suspend fun onSideEffectProcessed(se: SideEffect) {
        store.update {
            val newSideEffects = it.sideEffects - se
            it.copy(sideEffects = newSideEffects)
        }
    }

    private fun clearColorSchemeSelectedSwatch() {
        _colorSchemeSwatchDetailsViewModelFlow.value = null
    }

    private fun initialData(): HomeData =
        HomeData(
            canProceed = CanProceed(colorFromColorInput = colorInputMediator.colorState.color),
            proceedResult = null, // 'proceed' action wasn't invoked yet
            sideEffects = emptyList(),
        )

    private fun CanProceed(colorFromColorInput: Color?): Boolean {
        val hasColorInColorInput = (colorFromColorInput != null)
        return hasColorInColorInput
    }

    private suspend fun createAndConsumeNewColorCenterComponents() {
        colorCenterComponentsStore.createNewComponents(
            colorDetailsEventHandler = ColorCenterColorDetailsEventHandlerImpl(),
            colorSchemeEventHandler = ColorSchemeEventHandlerImpl(),
            selectedSwatchColorDetailsEventHandler = SelectedSwatchColorDetailsEventHandlerImpl(),
        )
        val newComponents = colorCenterComponentsStore.components
        _colorCenterViewModelFlow.emit(newComponents?.colorCenterViewModel)
    }

    context(coroutineScope: CoroutineScope)
    private suspend fun startColorCenterSession(
        seed: Color,
        deferredDetails: Deferred<DomainColorDetails>,
    ) {
        val job = Job(parent = currentCoroutineContext().job)
        // start building a new session inline suspending to guarantee that
        // by the time this function returns the session store has a new 'building' state
        val sessionBuilding = ccSessionStore.startBuilding(seed, job)
        @Suppress("CoroutineContextWithJob") // job is created correctly with the parent specified
        coroutineScope.launch(job) {
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

        coroutineScope.launch {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    context(batchScope: BatchScope<HomeData>)
    private suspend fun endColorCenterSession() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
        batchScope.update {
            it.copy(proceedResult = null)
        }
    }

    context(batchScope: BatchScope<HomeData>)
    private suspend fun onColorBecameCurrent(color: Color?) {
        batchScope.update {
            val canProceed = CanProceed(colorFromColorInput = color)
            it.copy(canProceed = canProceed)
        }
        colorPreviewViewModel.setColor(color)
    }

    private inner class ColorInputSubmitActionImpl : ColorInputSubmitAction {
        override fun invoke(
            colorInput: ColorInput,
            validationResult: ColorInputValidationResult,
        ): Boolean {
            when (validationResult) {
                is ColorInputValidationResult.Valid -> {
                    viewModelScope.launch(exclusiveLane) {
                        // TODO: merge with proceed() ?
                        opRegistry.trackAsProceed {
                            withContext(defaultDispatcher) {
                                store.batch {
                                    val color = validationResult.color
                                    createAndConsumeNewColorCenterComponents()
                                    val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                    startColorCenterSession(
                                        seed = color,
                                        deferredDetails = deferredDetails,
                                    )
                                    proceed(color) { colorDetails, colorScheme ->
                                        coroutineScope {
                                            launch { colorDetails.setSeedColor(color, deferredDetails) }
                                            launch { colorScheme.fetchColorScheme(color) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return true
                }
                is ColorInputValidationResult.Invalid -> {
                    viewModelScope.launch(exclusiveLane) {
                        store.update {
                            val result = HomeData.ProceedResult.InvalidSubmittedColor
                            it.copy(proceedResult = result)
                        }
                    }
                    return false
                }
            }
        }
    }

    private inner class ColorCenterColorDetailsEventHandlerImpl : ColorDetailsEventHandler {
        override suspend fun invoke(event: ColorDetailsEvent) {
            when (event) {
                is ColorDetailsEvent.ColorSelected ->
                    viewModelScope.launch(defaultDispatcher) {
                        opRegistry.trackAsProceed {
                            store.batch {
                                ccSessionStore.sessionState.mustBeOngoing()
                                val color = event.color
                                colorInputMediator.set(color)
                                onColorBecameCurrent(color)
                                // assuming any color selected belongs to ongoing session
                                proceed(color) { colorDetails, colorScheme ->
                                    coroutineScope {
                                        launch { colorDetails.selectColor(event.colorRole) }
                                        launch { colorScheme.fetchColorScheme(color) }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private inner class SelectedSwatchColorDetailsEventHandlerImpl : ColorDetailsEventHandler {
        override suspend fun invoke(event: ColorDetailsEvent) {
            when (event) {
                is ColorDetailsEvent.ColorSelected -> {
                    val viewModel = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsViewModel
                        ?: return
                    viewModel.selectColor(event.colorRole)
                }
            }
        }
    }

    private inner class ColorSchemeEventHandlerImpl : ColorSchemeEventHandler {
        override suspend fun invoke(event: ColorSchemeEvent) {
            when (event) {
                is ColorSchemeEvent.SwatchSelected -> {
                    val viewModel = colorCenterComponentsStore.components
                        ?.selectedSwatchColorDetailsViewModel
                        ?: return
                    viewModel.setSeedDetails(event.swatchColorDetails)
                    _colorSchemeSwatchDetailsViewModelFlow.value = viewModel
                }
            }
        }
    }

    private sealed interface Operation {
        data object Proceed : Operation
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
    }
}

private class SideEffectFactory {

    private val idFactory = SideEffectIdFactory()

    fun goToSettings(): SideEffect.GoToSettings =
        SideEffect.GoToSettings(
            id = idFactory.get(),
        )
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