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
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededByColorFromColorInput
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededByFetchColorDetails
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededByFetchColorScheme
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededByProceed
import io.github.mmolosay.thecolor.presentation.home.viewmodel.Operation.Companion.isSupersededByUpdateSwatchColorDetails
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
import io.github.mmolosay.thecolor.utils.BatchUpdateScope
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.UpdateScope
import io.github.mmolosay.thecolor.utils.appendUnlessCancelled
import io.github.mmolosay.thecolor.utils.batch
import io.github.mmolosay.thecolor.utils.focus
import io.github.mmolosay.thecolor.utils.launchSuperseding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
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
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _store = Store<HomeState?>(null)
    private val initializedLens = Lens<HomeState?, HomeState>(
        get = { s -> requireNotNull(s) },
        set = { _, v -> v },
    )
    private val store: Store<HomeState> = _store.focus(initializedLens)
    private val dataStore: Store<HomeData> = store.focus(
        Lens(
            get = { s -> s.home },
            set = { s, v -> s.copy(home = v) },
        )
    )

    val stateFlow: StateFlow<HomeState?> = _store.flow

    private val initialized = ClosableSuspendGate(closed = true)
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
        collectColorsFromColorInput()
    }

    private fun initialize(): Job =
        viewModelScope.launch(defaultDispatcher) {
            val updatesScope = BatchUpdateScope<HomeState?>()
            run {
                val initial = initialHomeState()
                updatesScope.update { initial }
            }
            run {
                updatesScope
                    .focus(initializedLens)
                    .appendUnlessCancelled { maybeProceedWithLastSearchedColor() }
            }
            _store.update {
                updatesScope.apply(it)
            }
            initialized.open()
        }

    private suspend fun initialHomeState(): HomeState {
        val color = getStartupColor()
        return HomeState(
            home = HomeData(
                canProceed = CanProceed(colorFromColorInput = colorInputMediator.colorState.color),
                proceedResult = null, // 'proceed' action wasn't invoked yet
                sideEffects = emptyList(),
            ),
            colorPreview = colorPreviewDataFactory.create(color),
            colorCenterHandles = null, // 'proceed' action wasn't invoked yet
        )
    }

    context(
        coroutineScope: CoroutineScope,
        updateScope: UpdateScope<HomeState>,
    )
    private fun maybeProceedWithLastSearchedColor(): Job =
        coroutineScope.launchAsInitialProceed launch@{
            val color = getStartupColor() ?: return@launch
            createAndConsumeNewColorCenterComponents()
            val deferredDetails = CompletableDeferred<DomainColorDetails>()
            // this Job is joined before 'initialized' opens
            context(coroutineScope) {
                startColorCenterSession(
                    seed = color,
                    deferredDetails = deferredDetails,
                )
            }
            colorInputMediator.set(color)
            onColorBecameCurrent(color)
            proceed(color) { colorDetails, colorScheme ->
                // launch lasting operations as children of outer coroutine to complete this coroutine quicker
                coroutineScope.launchAsFetchColorDetails {
                    colorDetails.setSeedColor(color, deferredDetails)
                }
                coroutineScope.launchAsFetchColorScheme {
                    colorScheme.fetchColorScheme(color)
                }
            }
        }

    private fun collectColorsFromColorInput(): Job =
        launchUntracked {
            colorInputMediator.colorStateFlow
                .drop(1) // replayed value
                .filter { it.source is ColorInputSource }
                .collect(::onColorFromColorInput)
        }

    private fun onColorFromColorInput(colorState: ColorInputMediator.ColorState) =
        launchAsColorFromColorInput {
            store.batch {
                val color = colorState.color
                endColorCenterSession() // assuming any new color from Color Input is a new session
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
        launchAsProceed launch@{
            val color = colorInputMediator.colorState.color ?: return@launch // invalid state
            store.transaction {
                store.batch { // TODO: remove outer 'transaction' when color input collector is tracked
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
                        launchAsFetchColorDetails {
                            colorDetails.setSeedColor(
                                color,
                                deferredDetails
                            )
                        }
                        launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
                    }
                }
            }
        }

    context(updateScope: UpdateScope<HomeState>)
    private fun proceed(
        color: Color,
        colorCenterAction: (ColorDetailsViewModel, ColorSchemeViewModel) -> Unit,
    ) {
        run executeColorCenterAction@{
            val components = requireNotNull(colorCenterComponentsStore.components)
            val colorDetails = components.colorCenterViewModel.colorDetailsViewModel
            val colorScheme = components.colorCenterViewModel.colorSchemeViewModel
            colorCenterAction(colorDetails, colorScheme)
        }
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

    private fun randomizeColor(): Job =
        launchAsProceed launch@{
            store.transaction {
                store.batch { // TODO: remove outer 'transaction' when color input collector is tracked
                    colorInputMediator.withLock { editor ->
                        val color = getPredictableRandomColor()
                        endColorCenterSession()
                        editor.set(color) // continue to hold mediator lock until the execution flow is finished
                        onColorBecameCurrent(color)
                        val shouldProceed = userPreferencesRepository
                            .flowOfAutoProceedWithRandomizedColors
                            .filterReady().first()
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
                                launchAsFetchColorDetails {
                                    colorDetails.setSeedColor(
                                        color,
                                        deferredDetails
                                    )
                                }
                                launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
                            }
                        }
                    }
                }
            }
        }

    private fun onRequestToGoToSettings(): Job =
        /*
         * Right now there's no logic in ViewModel that accompanies navigating to Settings.
         * In a real app, here would've been a logic for accepting / denying UI's navigation request
         * depending on the business logic. Here may also be sending data to analytics or logging.
         */
        launchUntracked {
            val se = seFactory.goToSettings()
            dataStore.update {
                it.copy(sideEffects = it.sideEffects + se)
            }
        }

    private fun onProceedResultProcessed(result: HomeData.ProceedResult): Job =
        launchUntracked {
            dataStore.update { current ->
                if (current.proceedResult != result) return@update current // stale invocation
                current.copy(proceedResult = null)
            }
        }

    private fun onSideEffectProcessed(se: SideEffect): Job =
        launchUntracked {
            dataStore.update {
                val newSideEffects = it.sideEffects - se
                it.copy(sideEffects = newSideEffects)
            }
        }

    private fun clearColorSchemeSelectedSwatch(): Job =
        launchUntracked launch@{
            // no ongoing session means there's no selected swatch to clear
            val components = colorCenterComponentsStore.components ?: return@launch
            components.selectedSwatchColorDetailsViewModel.clear()
        }

    context(updateScope: UpdateScope<HomeState>)
    private fun createAndConsumeNewColorCenterComponents() {
        colorCenterComponentsStore.createNewComponents(
            colorDetailsEventHandler = ColorCenterColorDetailsEventHandlerImpl(),
            colorSchemeEventHandler = ColorSchemeEventHandlerImpl(),
            selectedSwatchColorDetailsEventHandler = SelectedSwatchColorDetailsEventHandlerImpl(),
        )
        val newComponents = requireNotNull(colorCenterComponentsStore.components)
        updateScope.update {
            val handles = ColorCenterHandles(
                colorCenter = ColorCenterHandle(newComponents.colorCenterViewModel),
                selectedSwatchDetails = ColorDetailsHandle(newComponents.selectedSwatchColorDetailsViewModel),
            )
            it.copy(colorCenterHandles = handles)
        }
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

    context(updateScope: UpdateScope<HomeState>)
    private suspend fun endColorCenterSession() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
        updateScope.update {
            it.copy(
                home = it.home.copy(proceedResult = null),
                colorCenterHandles = null,
            )
        }
    }

    context(updateScope: UpdateScope<HomeState>)
    private fun onColorBecameCurrent(color: Color?) {
        updateScope.update {
            it.withCurrentColor(color)
        }
    }

    private fun CanProceed(colorFromColorInput: Color?): Boolean {
        val hasColorInColorInput = (colorFromColorInput != null)
        return hasColorInColorInput
    }

    private fun HomeState.withCurrentColor(color: Color?): HomeState =
        this.copy(
            home = this.home.copy(canProceed = CanProceed(color)),
            colorPreview = colorPreviewDataFactory.create(color),
        )

    private inner class ColorInputSubmitActionImpl : ColorInputSubmitAction {
        override fun invoke(
            colorInput: ColorInput,
            validationResult: ColorInputValidationResult,
        ): Boolean {
            when (validationResult) {
                is ColorInputValidationResult.Valid -> {
                    // TODO: merge with proceed() ?
                    launchAsProceed {
                        store.transaction {
                            store.batch { // TODO: remove outer 'transaction' when color input collector is tracked
                                val color = validationResult.color
                                createAndConsumeNewColorCenterComponents()
                                val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                startColorCenterSession(
                                    seed = color,
                                    deferredDetails = deferredDetails,
                                )
                                colorInputMediator.set(color)
                                onColorBecameCurrent(color)
                                proceed(color) { colorDetails, colorScheme ->
                                    launchAsFetchColorDetails {
                                        colorDetails.setSeedColor(
                                            color,
                                            deferredDetails
                                        )
                                    }
                                    launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
                                }
                            }
                        }
                    }
                    return true
                }
                is ColorInputValidationResult.Invalid -> {
                    launchUntracked {
                        dataStore.update {
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
                    launchAsProceed {
                        store.transaction {
                            store.batch { // TODO: remove outer 'transaction' when color input collector is tracked
                                ccSessionStore.sessionState.mustBeOngoing()
                                val color = event.color
                                colorInputMediator.set(color)
                                onColorBecameCurrent(color)
                                // assuming any color selected belongs to ongoing session
                                proceed(color) { colorDetails, colorScheme ->
                                    launchAsFetchColorDetails { colorDetails.selectColor(event.colorRole) }
                                    launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
                                }
                            }
                        }
                    }
                is ColorDetailsEvent.RetryOnErrorAction ->
                    when (val origin = event.error.origin) {
                        is ColorDetailsError.Origin.SetSeedColor ->
                            launchAsProceed launch@{
                                val viewModel = viewModel() ?: return@launch
                                // the session was canceled when the seed fetch failed, so build a new one
                                val deferredDetails = CompletableDeferred<DomainColorDetails>()
                                startColorCenterSession(
                                    seed = origin.color,
                                    deferredDetails = deferredDetails,
                                )
                                viewModel.setSeedColor(origin.color, deferredDetails)
                            }
                        is ColorDetailsError.Origin.SelectColor ->
                            viewModelScope.launchAsFetchColorDetails launch@{
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
                    viewModelScope.launchAsUpdateSwatchColorDetails {
                        viewModel()?.selectColor(event.colorRole)
                    }
                }
                is ColorDetailsEvent.RetryOnErrorAction -> {
                    viewModelScope.launchAsUpdateSwatchColorDetails launch@{
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
                    viewModelScope.launchAsUpdateSwatchColorDetails launch@{
                        val viewModel = colorCenterComponentsStore.components
                            ?.selectedSwatchColorDetailsViewModel
                            ?: return@launch
                        // set the data first, so that the handle is published already populated
                        viewModel.setSeedDetails(event.swatchColorDetails)
                    }
                }
                is ColorSchemeEvent.ApplyChangesAction -> {
                    viewModelScope.launchAsFetchColorScheme {
                        viewModel()?.fetchColorScheme(event.seed)
                    }
                }
                is ColorSchemeEvent.RetryOnErrorAction -> {
                    viewModelScope.launchAsFetchColorScheme {
                        viewModel()?.fetchColorScheme(event.seed)
                    }
                }
            }
        }

        private fun viewModel(): ColorSchemeViewModel? =
            colorCenterComponentsStore.components?.colorCenterViewModel?.colorSchemeViewModel
    }

    private fun CoroutineScope.launchAsInitialProceed(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        this.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.Proceed,
            predicate = { it.value.isSupersededByProceed() },
            block = block, // no awaitInit()
        )

    private fun launchAsProceed(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        viewModelScope.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.Proceed,
            predicate = { it.value.isSupersededByProceed() },
            block = awaitInit(block),
        )

    private fun CoroutineScope.launchAsFetchColorDetails(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        this.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.FetchColorDetails,
            predicate = { it.value.isSupersededByFetchColorDetails() },
            block = awaitInit(block),
        )

    private fun CoroutineScope.launchAsFetchColorScheme(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        this.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.FetchColorScheme,
            predicate = { it.value.isSupersededByFetchColorScheme() },
            block = awaitInit(block),
        )

    private fun CoroutineScope.launchAsUpdateSwatchColorDetails(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        this.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.UpdateSwatchColorDetails,
            predicate = { it.value.isSupersededByUpdateSwatchColorDetails() },
            block = awaitInit(block),
        )

    private fun launchAsColorFromColorInput(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        viewModelScope.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.ColorFromColorInput,
            predicate = { it.value.isSupersededByColorFromColorInput() },
            block = awaitInit(block),
        )

    private fun launchUntracked(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        viewModelScope.launch(
            context = defaultDispatcher,
            block = awaitInit(block),
        )

    private fun awaitInit(
        block: suspend CoroutineScope.() -> Unit,
    ): suspend CoroutineScope.() -> Unit =
        {
            initialized.awaitOpen()
            block()
        }
}

/**
 * A kind of work that [HomeViewModel] runs in a coroutine tracked by the [HomeViewModel.opRegistry].
 * Two operations of the same kind never run at the same time;
 * which kinds supersede which is defined by the `isSupersededBy*` functions below.
 */
private sealed interface Operation {

    data object Proceed : Operation

    data object FetchColorDetails : Operation

    data object FetchColorScheme : Operation

    data object UpdateSwatchColorDetails : Operation

    data object ColorFromColorInput : Operation

    companion object {

        fun Operation.isSupersededByProceed(): Boolean =
            when (this) {
                // a new session invalidates every piece of work that belongs to the previous one
                is Proceed,
                is FetchColorDetails,
                is FetchColorScheme,
                is UpdateSwatchColorDetails,
                is ColorFromColorInput, ->
                    true
            }

        fun Operation.isSupersededByFetchColorDetails(): Boolean =
            when (this) {
                is FetchColorDetails ->
                    true
                is Proceed,
                is FetchColorScheme,
                is UpdateSwatchColorDetails,
                is ColorFromColorInput, ->
                    false
            }

        fun Operation.isSupersededByFetchColorScheme(): Boolean =
            when (this) {
                is FetchColorScheme ->
                    true
                is Proceed,
                is FetchColorDetails,
                is UpdateSwatchColorDetails,
                is ColorFromColorInput, ->
                    false
            }

        fun Operation.isSupersededByUpdateSwatchColorDetails(): Boolean =
            when (this) {
                is UpdateSwatchColorDetails ->
                    true
                is Proceed,
                is FetchColorDetails,
                is FetchColorScheme,
                is ColorFromColorInput, ->
                    false
            }

        fun Operation.isSupersededByColorFromColorInput(): Boolean =
            when (this) {
                is Proceed,
                is ColorFromColorInput,
                is FetchColorDetails,
                is FetchColorScheme,
                is UpdateSwatchColorDetails, -> true
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