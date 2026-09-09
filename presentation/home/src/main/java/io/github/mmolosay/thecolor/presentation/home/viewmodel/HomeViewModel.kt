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
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventHandler
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededBy
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputSource
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupDataFactory
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEventHandler
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.UpdateScope
import io.github.mmolosay.thecolor.utils.batch
import io.github.mmolosay.thecolor.utils.dropOnNull
import io.github.mmolosay.thecolor.utils.focus
import io.github.mmolosay.thecolor.utils.launchSuperseding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
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
    private val colorInputMediator: ColorInputMediator,
    colorInputGroupDataFactory: ColorInputGroupDataFactory,
    colorInputGroupViewModelFactory: ColorInputGroupViewModel.Factory,
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

    private val store = Store<HomeState?>(null)
    val stateFlow: StateFlow<HomeState?> = store.flow

    private val opRegistry = CoroutineRegistry<Operation>()
    private val ccSessionStore = ColorCenterSessionStore()
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
            store.batch {
                val initial = HomeState(
                    home = HomeData(
                        canProceed = CanProceed(colorInputMediator.colorState.color),
                        proceedResult = null, // 'proceed' action wasn't invoked yet
                        sideEffects = emptyList(),
                    ),
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
            collectColorsFromColorInput()
        }

    context(updateScope: UpdateScope<HomeState>)
    private fun proceedWithLastSearchedColor(color: Color): Job {
        val components = createNewColorCenterComponents()
        consumeColorCenterComponents(components)
        onColorBecameCurrent(color)
        setProceedResult(color)
        return launchTransition(Operation.Transition.Proceed) {
            colorInputMediator.set(color)
            val deferredDetails = CompletableDeferred<DomainColorDetails>()
            startColorCenterSession(seed = color, deferredDetails = deferredDetails)
            launchFetch(Operation.Fetch.ColorDetails) {
                val viewModel = components.colorCenterViewModel.colorDetailsViewModel
                viewModel.setSeedColor(color, deferredDetails)
            }
            launchFetch(Operation.Fetch.ColorScheme) {
                val viewModel = components.colorCenterViewModel.colorSchemeViewModel
                viewModel.fetchColorScheme(color)
            }
        }
    }

    private fun collectColorsFromColorInput(): Job =
        viewModelScope.launch {
            colorInputMediator.colorStateFlow
                .drop(1) // replayed value
                .filter { it.source is ColorInputSource }
                .collect(::onColorFromColorInput)
        }

    private fun onColorFromColorInput(colorState: ColorInputMediator.ColorState) =
        launchTransition(Operation.Transition.ColorFromColorInput) {
            val color = colorState.color
            endColorCenterSession() // assuming any new color from Color Input is a new session
            updateState {
                onColorCenterSessionEnded()
                onColorBecameCurrent(color)
            }
        }

    fun execute(action: HomeAction): Job =
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
            val color = colorInputMediator.colorState.color ?: return@launch // invalid state
            colorInputMediator.set(color)
            proceedWith(color)
        }

    private fun randomizeColor(): Job =
        launchTransition(Operation.Transition.Proceed) launch@{
            val color: Color
            // take the lock before producing the color, so no mediator update lands between the two
            colorInputMediator.withLock { editor ->
                color = getPredictableRandomColor()
                editor.set(color)
            }
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
                it.copy(sideEffects = it.sideEffects + se)
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
                val newSideEffects = it.sideEffects - se
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
        // doesn't update 'colorInputMediator', it should be done by the caller
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
            val viewModel = components.colorCenterViewModel.colorDetailsViewModel
            viewModel.setSeedColor(color, deferredDetails)
        }
        coroutineScope.launchFetch(Operation.Fetch.ColorScheme) {
            val viewModel = components.colorCenterViewModel.colorSchemeViewModel
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
        colorCenterComponentsStore.createNewComponents(
            colorDetailsEventHandler = ColorCenterColorDetailsEventHandlerImpl(),
            colorSchemeEventHandler = ColorSchemeEventHandlerImpl(),
            selectedSwatchColorDetailsEventHandler = SelectedSwatchColorDetailsEventHandlerImpl(),
        )

    context(updateScope: UpdateScope<HomeState>)
    private fun consumeColorCenterComponents(components: ColorCenterComponents) =
        updateScope.update {
            val handles = ColorCenterHandles(
                colorCenter = ColorCenterHandle(components.colorCenterViewModel),
                selectedSwatchDetails = ColorDetailsHandle(components.selectedSwatchColorDetailsViewModel),
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

    // TODO: before, Store.transaction() was used, and it held a write Mutex, making every transaction() exclusive for its whole duration. Wrap in mutex.withLock()?
    private suspend inline fun updateState(block: UpdateScope<HomeState>.() -> Unit) =
        store.batch {
            with(dropOnNull(), block)
        }

    private suspend fun updateData(transform: (HomeData) -> HomeData) =
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
                        colorInputMediator.set(color)
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

    private inner class ColorCenterColorDetailsEventHandlerImpl : ColorDetailsEventHandler {
        override fun invoke(event: ColorDetailsEvent) {
            when (event) {
                is ColorDetailsEvent.SelectColorAction ->
                    launchTransition(Operation.Transition.Proceed) launch@{
                        val components = colorCenterComponentsStore.components ?: return@launch
                        val color = event.color
                        ccSessionStore.sessionState.mustBeOngoing()
                        colorInputMediator.set(color)
                        updateState {
                            onColorBecameCurrent(color)
                            // assuming any color selected belongs to ongoing session
                            setProceedResult(color)
                        }
                        launchFetch(Operation.Fetch.ColorDetails) {
                            val viewModel = components.colorCenterViewModel.colorDetailsViewModel
                            viewModel.selectColor(event.colorRole)
                        }
                        launchFetch(Operation.Fetch.ColorScheme) {
                            val viewModel = components.colorCenterViewModel.colorSchemeViewModel
                            viewModel.fetchColorScheme(color)
                        }
                    }
                is ColorDetailsEvent.RetryOnErrorAction ->
                    when (val origin = event.error.origin) {
                        is ColorDetailsError.Origin.SetSeedColor ->
                            launchTransition(Operation.Transition.Proceed) launch@{
                                val components = colorCenterComponentsStore.components ?: return@launch
                                // the session was canceled when the seed fetch failed, so build a new one
                                val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                startColorCenterSession(
                                    seed = origin.color,
                                    deferredDetails = deferredDetails,
                                )
                                launchFetch(Operation.Fetch.ColorDetails) {
                                    val viewModel =
                                        components.colorCenterViewModel.colorDetailsViewModel
                                    viewModel.setSeedColor(origin.color, deferredDetails)
                                }
                            }
                        is ColorDetailsError.Origin.SelectColor ->
                            viewModelScope.launchFetch(Operation.Fetch.ColorDetails) launch@{
                                val viewModel = viewModel() ?: return@launch
                                // the session is still ongoing, only the fetch failed
                                viewModel.selectColor(origin.role)
                            }
                    }
            }
        }

        private fun viewModel(): ColorDetailsViewModel? =
            colorCenterComponentsStore.components?.colorCenterViewModel?.colorDetailsViewModel
    }

    private inner class SelectedSwatchColorDetailsEventHandlerImpl : ColorDetailsEventHandler {
        override fun invoke(event: ColorDetailsEvent) {
            when (event) {
                is ColorDetailsEvent.SelectColorAction -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) {
                        viewModel()?.selectColor(event.colorRole)
                    }
                }
                is ColorDetailsEvent.RetryOnErrorAction -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) launch@{
                        val viewModel = viewModel() ?: return@launch
                        when (val origin = event.error.origin) {
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

        private fun viewModel(): ColorDetailsViewModel? =
            colorCenterComponentsStore.components?.selectedSwatchColorDetailsViewModel
    }

    private inner class ColorSchemeEventHandlerImpl : ColorSchemeEventHandler {
        override fun invoke(event: ColorSchemeEvent) {
            when (event) {
                is ColorSchemeEvent.SelectSwatchAction -> {
                    viewModelScope.launchFetch(Operation.Fetch.SwatchColorDetails) launch@{
                        val viewModel = colorCenterComponentsStore.components
                            ?.selectedSwatchColorDetailsViewModel
                            ?: return@launch
                        // set the data first, so that the handle is published already populated
                        viewModel.setSeedDetails(event.swatchColorDetails)
                    }
                }
                is ColorSchemeEvent.ApplyChangesAction -> {
                    viewModelScope.launchFetch(Operation.Fetch.ColorScheme) {
                        viewModel()?.fetchColorScheme(event.seed)
                    }
                }
                is ColorSchemeEvent.RetryOnErrorAction -> {
                    viewModelScope.launchFetch(Operation.Fetch.ColorScheme) {
                        viewModel()?.fetchColorScheme(event.seed)
                    }
                }
            }
        }

        private fun viewModel(): ColorSchemeViewModel? =
            colorCenterComponentsStore.components?.colorCenterViewModel?.colorSchemeViewModel
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