package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.color.GetStartupColorUseCase
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsAction
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ExecuteColorDetailsAction
import io.github.mmolosay.thecolor.presentation.details.viewmodel.asError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.colorOrNull
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededBy
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupHandle
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupStateFactory
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.group.colorState
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeAction
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeHandle
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ExecuteColorSchemeAction
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.asError
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.asReady
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.UpdateScope
import io.github.mmolosay.thecolor.utils.batch
import io.github.mmolosay.thecolor.utils.dropOnNull
import io.github.mmolosay.thecolor.utils.focus
import io.github.mmolosay.thecolor.utils.launchSuperseding
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
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
class HomeViewModel @Inject constructor(
    private val colorInputGroupStateFactory: ColorInputGroupStateFactory,
    private val colorInputGroupViewModelFactory: ColorInputGroupViewModel.Factory,
    private val colorPreviewDataFactory: ColorPreviewDataFactory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorComparator: ColorComparator,
    private val getStartupColor: GetStartupColorUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : ViewModel(
    viewModelScope = CoroutineScope(SupervisorJob() + defaultDispatcher),
) {

    private val _stateFlow = MutableStateFlow<HomeState?>(null)
    val stateFlow: StateFlow<HomeState?> = _stateFlow.asStateFlow()

    private val opRegistry = CoroutineRegistry<Operation>()
    private val ccSessionStore = ColorCenterSessionStore()
    private val seFactory = SideEffectFactory()

    private var colorInputGroupViewModel: ColorInputGroupViewModel? = null

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    init {
        initialize()
    }

    private fun initialize(): Job =
        viewModelScope.launch {
            val color = getStartupColor()
            val colorInputGroupViewModel = colorInputGroupViewModelFactory.create(
                coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
                initialState = colorInputGroupStateFactory.create(color),
                submitAction = ColorInputSubmitActionImpl(),
            ).also {
                colorInputGroupViewModel = it
            }
            _stateFlow.batch {
                val initial = HomeState(
                    home = HomeData(
                        canProceed = CanProceed(colorInputGroupViewModel.colorState.color),
                        proceedResult = null, // 'proceed' action wasn't invoked yet
                        sideEffects = persistentListOf(),
                    ),
                    colorInputGroupHandle = ColorInputGroupHandle(colorInputGroupViewModel),
                    colorPreview = colorPreviewDataFactory.create(color),
                    colorCenterHandles = null, // 'proceed' action wasn't invoked yet
                )
                update { it ?: initial }
                if (color != null) {
                    context(dropOnNull()) {
                        proceedWithLastSearchedColor(color)
                    }
                }
            }
            collectColorsFromColorInput(colorInputGroupViewModel)
        }

    context(updateScope: UpdateScope<HomeState>)
    private fun proceedWithLastSearchedColor(color: Color): Job {
        val components = createNewColorCenterComponents()
        consumeColorCenterComponents(components)
        onColorBecameCurrent(color)
        setProceedResult(color)
        return launchTransition(Operation.Transition.Proceed) {
            colorInputGroupViewModel
                .let { requireNotNull(it) }
                .setColor(color)
            val deferredDetails = CompletableDeferred<DomainColorDetails>()
            startColorCenterSession(seed = color, deferredDetails = deferredDetails)
            launchFetch(Operation.Fetch.ColorDetails) {
                val viewModel = components.colorDetailsViewModel
                viewModel.setSeedColor(color, deferredDetails)
            }
            launchFetch(Operation.Fetch.ColorScheme) {
                val viewModel = components.colorSchemeViewModel
                viewModel.fetchColorScheme(color)
            }
        }
    }

    private fun collectColorsFromColorInput(group: ColorInputGroupViewModel): Job =
        viewModelScope.launch {
            group.colorStateFlow
                .drop(1) // replayed value
                .filter { it.source != null } // set by 'Color Input' feature
                .collect(::onColorFromColorInput)
        }

    private fun onColorFromColorInput(colorState: ColorState) =
        launchTransition(Operation.Transition.ColorFromColorInput) {
            val color = colorState.color
            endColorCenterSession() // assuming any new color from Color Input is a new session
            updateState {
                onColorCenterSessionEnded()
                onColorBecameCurrent(color)
            }
        }

    fun execute(action: HomeAction): Job? =
        when (action) {
            is HomeAction.Proceed -> proceed()
            is HomeAction.RandomizeColor -> randomizeColor()
            is HomeAction.RequestToGoToSettings -> onRequestToGoToSettings()
            is HomeAction.OnProceedResultProcessed -> onProceedResultProcessed(action.result)
            is HomeAction.OnSideEffectProcessed -> onSideEffectProcessed(action.se)
            is HomeAction.ClearColorSchemeSelectedSwatch -> clearColorSchemeSelectedSwatch()
        }

    private fun proceed(): Job =
        launchTransition(Operation.Transition.Proceed) launch@{
            val color = colorInputGroupViewModel
                .let { requireNotNull(it) }
                .colorState.color ?: return@launch // invalid state
            colorInputGroupViewModel
                .let { requireNotNull(it) }
                .setColor(color)
            proceedWith(color)
        }

    private fun randomizeColor(): Job =
        launchTransition(Operation.Transition.Proceed) launch@{
            val color = getPredictableRandomColor()
            colorInputGroupViewModel
                .let { requireNotNull(it) }
                .setColor(color)
            val shouldProceed = userPreferencesRepository
                .flowOfAutoProceedWithRandomizedColors
                .filterReady().first()
                .getOrElse { DefaultUserPreferences.AutoProceedWithRandomizedColors }
                .enabled
            if (shouldProceed) {
                proceedWith(color)
            } else {
                endColorCenterSession()
                updateState {
                    onColorCenterSessionEnded()
                    onColorBecameCurrent(color)
                }
            }
        }

    private fun onRequestToGoToSettings(): Job =
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        viewModelScope.launch {
            val se = seFactory.goToSettings()
            updateData {
                it.copy(sideEffects = it.sideEffects.toPersistentList() + se)
            }
        }

    private fun onProceedResultProcessed(result: HomeData.ProceedResult): Job =
        viewModelScope.launch {
            updateData { current ->
                if (current.proceedResult != result) return@updateData current // stale invocation
                current.copy(proceedResult = null)
            }
        }

    private fun onSideEffectProcessed(se: SideEffect): Job =
        viewModelScope.launch {
            updateData {
                val newSideEffects = it.sideEffects.toPersistentList() - se
                it.copy(sideEffects = newSideEffects)
            }
        }

    private fun clearColorSchemeSelectedSwatch(): Job =
        viewModelScope.launch {
            // no ongoing session means there's no selected swatch to clear
            val components = colorCenterComponentsStore.components ?: return@launch
            components.selectedSwatchColorDetailsViewModel.clear()
        }

    context(coroutineScope: CoroutineScope)
    private suspend fun proceedWith(color: Color) {
        // doesn't update color in 'Color Input', should be done by the caller
        endColorCenterSession()
        val components = createNewColorCenterComponents()
        updateState {
            consumeColorCenterComponents(components)
            onColorBecameCurrent(color)
            setProceedResult(color)
        }
        val deferredDetails = CompletableDeferred<DomainColorDetails>()
        startColorCenterSession(seed = color, deferredDetails = deferredDetails)
        coroutineScope.launchFetch(Operation.Fetch.ColorDetails) {
            val viewModel = components.colorDetailsViewModel
            viewModel.setSeedColor(color, deferredDetails)
        }
        coroutineScope.launchFetch(Operation.Fetch.ColorScheme) {
            val viewModel = components.colorSchemeViewModel
            viewModel.fetchColorScheme(color)
        }
    }

    context(updateScope: UpdateScope<HomeState>)
    private fun setProceedResult(color: Color) {
        updateScope.update {
            val colorData = createColorData(color)
            val proceedResult = HomeData.ProceedResult.Success(
                colorData = colorData,
            )
            it.copy(
                home = it.home.copy(proceedResult = proceedResult),
            )
        }
    }

    private fun createNewColorCenterComponents(): ColorCenterComponents =
        colorCenterComponentsStore.createNewComponents()

    context(updateScope: UpdateScope<HomeState>)
    private fun consumeColorCenterComponents(components: ColorCenterComponents) =
        updateScope.update {
            val handles = ColorCenterHandles(
                colorCenter = ColorCenterHandle(components.colorCenterViewModel),
                colorDetails = ColorDetailsHandle(
                    stateFlow = components.colorDetailsViewModel.stateFlow,
                    execute = ColorCenterColorDetailsActionExecutor(components),
                ),
                colorScheme = ColorSchemeHandle(
                    stateFlow = components.colorSchemeViewModel.stateFlow,
                    execute = ColorSchemeActionExecutor(components),
                ),
                selectedSwatchDetails = ColorDetailsHandle(
                    stateFlow = components.selectedSwatchColorDetailsViewModel.stateFlow,
                    execute = SelectedSwatchColorDetailsActionExecutor(components),
                ),
            )
            it.copy(colorCenterHandles = handles)
        }

    context(coroutineScope: CoroutineScope)
    private suspend fun startColorCenterSession(
        seed: Color,
        deferredDetails: Deferred<DomainColorDetails>,
    ) {
        val startedBuilding = CompletableDeferred<Unit>()
        coroutineScope.launch {
            val sessionBuilding = ccSessionStore.startBuilding(seed, coroutineContext.job)
            startedBuilding.complete(Unit)
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
        // suspend inline until the state of session store is updated to 'BeingBuilt'
        startedBuilding.await()

        coroutineScope.launch {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    private suspend fun endColorCenterSession() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
    }

    context(updateScope: UpdateScope<HomeState>)
    private fun onColorCenterSessionEnded() =
        updateScope.update {
            it.copy(
                home = it.home.copy(proceedResult = null),
                colorCenterHandles = null,
            )
        }

    context(updateScope: UpdateScope<HomeState>)
    private fun onColorBecameCurrent(color: Color?) {
        updateScope.update {
            it.copy(
                home = it.home.copy(canProceed = CanProceed(color)),
                colorPreview = colorPreviewDataFactory.create(color),
            )
        }
    }

    private fun CanProceed(currentColor: Color?): Boolean =
        (currentColor != null)

    private inline fun updateState(block: UpdateScope<HomeState>.() -> Unit) =
        _stateFlow.batch {
            with(dropOnNull(), block)
        }

    private fun updateData(transform: (HomeData) -> HomeData) =
        updateState {
            this.focus(HomeStateLenses.home).update(transform)
        }

    private inner class ColorInputSubmitActionImpl : ColorInputSubmitAction {
        override fun invoke(
            colorInput: ColorInput,
            validationResult: ColorInputValidationResult,
        ): Boolean {
            when (validationResult) {
                is ColorInputValidationResult.Valid -> {
                    launchTransition(Operation.Transition.Proceed) {
                        val color = validationResult.color
                        colorInputGroupViewModel
                            .let { requireNotNull(it) }
                            .setColor(color)
                        proceedWith(color)
                    }
                    return true
                }
                is ColorInputValidationResult.Invalid -> {
                    viewModelScope.launch {
                        updateData {
                            val result = HomeData.ProceedResult.InvalidSubmittedColor
                            it.copy(proceedResult = result)
                        }
                    }
                    return false
                }
            }
        }
    }

    private inner class ColorCenterColorDetailsActionExecutor(
        private val components: ColorCenterComponents,
    ) : ExecuteColorDetailsAction {

        private val viewModel: ColorDetailsViewModel
            get() = components.colorDetailsViewModel

        override operator fun invoke(action: ColorDetailsAction): Job? =
            when (action) {
                is ColorDetailsAction.SelectColor -> {
                    launchTransition(Operation.Transition.Proceed) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        val color = viewModel.stateFlow.value.colorOrNull(action.role)
                            ?: return@launch // stale invocation
                        ccSessionStore.sessionState.mustBeOngoing()
                        colorInputGroupViewModel
                            .let { requireNotNull(it) }
                            .setColor(color)
                        updateState {
                            onColorBecameCurrent(color)
                            // assuming any color selected belongs to ongoing session
                            setProceedResult(color)
                        }
                        launchFetch(Operation.Fetch.ColorDetails) {
                            val viewModel = components.colorDetailsViewModel
                            viewModel.selectColor(action.role)
                        }
                        launchFetch(Operation.Fetch.ColorScheme) {
                            val viewModel = components.colorSchemeViewModel
                            viewModel.fetchColorScheme(color)
                        }
                    }
                }
                is ColorDetailsAction.RetryOnError -> {
                    val origin = viewModel.stateFlow.value.asError()?.error?.origin
                        ?: return null // stale invocation
                    when (origin) {
                        is ColorDetailsError.Origin.SetSeedColor ->
                            launchTransition(Operation.Transition.Proceed) launch@{
                                if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                                // the session was canceled when the seed fetch failed, so build a new one
                                val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                startColorCenterSession(
                                    seed = origin.color,
                                    deferredDetails = deferredDetails,
                                )
                                launchFetch(Operation.Fetch.ColorDetails) {
                                    val viewModel = components.colorDetailsViewModel
                                    viewModel.setSeedColor(origin.color, deferredDetails)
                                }
                            }
                        is ColorDetailsError.Origin.SelectColor ->
                            viewModelScope.launchFetch(Operation.Fetch.ColorDetails) launch@{
                                if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                                val viewModel = components.colorDetailsViewModel
                                // the session is still ongoing, only the fetch failed
                                viewModel.selectColor(origin.role)
                            }
                    }
                }
            }
    }

    private inner class SelectedSwatchColorDetailsActionExecutor(
        private val components: ColorCenterComponents,
    ) : ExecuteColorDetailsAction {

        private val viewModel: ColorDetailsViewModel
            get() = components.selectedSwatchColorDetailsViewModel

        override operator fun invoke(action: ColorDetailsAction): Job? =
            when (action) {
                is ColorDetailsAction.SelectColor -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        viewModel.selectColor(action.role)
                    }
                }
                is ColorDetailsAction.RetryOnError -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        val origin = viewModel.stateFlow.value.asError()?.error?.origin
                            ?: return@launch // stale invocation
                        when (origin) {
                            // the swatch's seed comes from 'setSeedDetails', which cannot fail, so this origin is not reachable here
                            is ColorDetailsError.Origin.SetSeedColor -> {
                                viewModel.setSeedColor(origin.color)
                            }
                            is ColorDetailsError.Origin.SelectColor -> {
                                viewModel.selectColor(origin.role)
                            }
                        }
                    }
                }
            }
    }

    private inner class ColorSchemeActionExecutor(
        private val components: ColorCenterComponents,
    ) : ExecuteColorSchemeAction {

        private val viewModel: ColorSchemeViewModel
            get() = components.colorSchemeViewModel

        override fun invoke(action: ColorSchemeAction): Job? =
            when (action) {
                is ColorSchemeAction.SelectSwatch -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        val swatchDetails = viewModel.stateFlow.value.asReady()
                            ?.domainColorScheme?.swatchDetails?.getOrNull(action.swatchIndex)
                            ?: return@launch // stale invocation
                        val swatchViewModel = components.selectedSwatchColorDetailsViewModel
                        // set the data first, so that the handle is published already populated
                        swatchViewModel.setSeedDetails(swatchDetails)
                    }
                }
                is ColorSchemeAction.SelectMode -> {
                    viewModel.selectMode(action.mode)
                    null
                }
                is ColorSchemeAction.SelectSwatchCount -> {
                    viewModel.selectSwatchCount(action.count)
                    null
                }
                is ColorSchemeAction.ApplyChanges -> {
                    viewModelScope.launchFetch(Operation.Fetch.ColorScheme) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        val ready =
                            viewModel.stateFlow.value.asReady() ?: return@launch // stale invocation
                        if (!ready.data.hasChangesToApply) return@launch // nothing to apply
                        viewModel.fetchColorScheme(ready.request.seed)
                    }
                }
                is ColorSchemeAction.RetryOnError -> {
                    viewModelScope.launchFetch(Operation.Fetch.ColorScheme) launch@{
                        if (colorCenterComponentsStore.components !== components) return@launch // stale instance
                        val error =
                            viewModel.stateFlow.value.asError() ?: return@launch // stale invocation
                        viewModel.fetchColorScheme(error.request.seed)
                    }
                }
            }
    }

    /**
     * Launches an [Operation.Transition] on [viewModelScope].
     *
     * [block] may write [HomeState] as many times as there are states worth publishing;
     * every commit must be reachable in bounded time. It must not await a network call,
     * a child ViewModel, or an [Operation.Fetch] — see [Operation.Transition].
     *
     * Unbounded follow-up work is started with [launchFetch] on the receiver [CoroutineScope], making it
     * a child of this operation: a superseding transition cancels it, and it keeps this operation's
     * [Job] alive without delaying any commit.
     */
    private fun launchTransition(
        value: Operation.Transition,
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        viewModelScope.launchSuperseding(
            registry = opRegistry,
            value = value,
            predicate = { it.value.isSupersededBy(value) },
            block = block,
        )

    /**
     * Launches an [Operation.Fetch] as a child of the receiver — normally the [Operation.Transition]
     * that started it, so a superseding transition cancels this fetch along with it.
     *
     * [block] may run for an unbounded time and must not write [HomeState].
     */
    private fun CoroutineScope.launchFetch(
        value: Operation.Fetch,
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        launchSuperseding(
            registry = opRegistry,
            value = value,
            predicate = { it.value.isSupersededBy(value) },
            block = block,
        )
}

/**
 * A kind of work that [HomeViewModel] runs in a coroutine tracked by the [HomeViewModel.opRegistry].
 * Two operations of the same kind never run at the same time;
 * which kinds supersede which is defined by the `isSupersededBy*` functions below.
 */
private sealed interface Operation {

    /** Brings 'Home' to a new state. Writes [HomeState]. */
    sealed interface Transition : Operation {
        data object Proceed : Transition
        data object ColorFromColorInput : Transition
    }

    /** Fills in data the current state is waiting for. Never writes [HomeState], */
    sealed interface Fetch : Operation {
        data object ColorDetails : Fetch
        data object ColorScheme : Fetch
        data object SwatchColorDetails : Fetch
    }

    companion object {

        fun Operation.isSupersededBy(new: Operation): Boolean =
            when (new) {
                is Transition -> true // a new state invalidates anything ongoing
                is Fetch -> (new::class == this::class) // a fetch invalidates only its own kind
            }
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