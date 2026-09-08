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
import io.github.mmolosay.thecolor.presentation.center.ColorCenterDataFactory
import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventHandler
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
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
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEvent
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEventHandler
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.CoroutineRegistry
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.Store
import io.github.mmolosay.thecolor.utils.launchSuperseding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
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
    private val colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    private val colorCenterDataFactory: ColorCenterDataFactory,
    colorCenterComponentsStoreFactory: ColorCenterComponentsStore.Factory,
    private val createColorData: CreateColorDataUseCase,
    private val colorComparator: ColorComparator,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val store = Store<HomeState>(HomeState.Initializing)
    val stateFlow: StateFlow<HomeState> = store.flow

    private val treeStore = store.focus(HomeStateLenses.tree)
    private val dataStore = treeStore.focus(HomeTreeDataLenses.home)

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

    @Volatile
    private var colorPreviewViewModel: ColorPreviewViewModel? = null

    private val colorCenterComponentsStore: ColorCenterComponentsStore =
        colorCenterComponentsStoreFactory.create(
            viewModelScope = viewModelScope,
        )

    init {
        viewModelScope.launch(defaultDispatcher) {
            initialize()
            collectColorsFromColorInput()
        }
    }

    context(coroutineScope: CoroutineScope)
    private suspend fun initialize() {
        val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
            .flowOfResumeFromLastSearchedColorOnStartup
            .filterReady().first()
            .getOrElse { DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup }
        val color = if (resumeFromLastSearchedColorOnStartup.enabled) {
            lastSearchedColorRepository.getLastSearchedColor()
        } else null
        store.transaction {
            store.update {
                val tree = HomeTreeData(
                    home = HomeData(
                        canProceed = CanProceed(colorFromColorInput = colorInputMediator.colorState.color),
                        proceedResult = null, // 'proceed' action wasn't invoked yet
                        sideEffects = emptyList(),
                    ),
                    colorPreview = colorPreviewDataFactory.create(color),
                )
                HomeState.Ready(
                    tree = tree,
                    colorCenterHandles = null, // 'proceed' action wasn't invoked yet
                )
            }
            // from here on the state is 'Ready', thus the partial lenses are safe to write through
            this.colorPreviewViewModel = colorPreviewViewModelFactory.create(
                coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
                store = treeStore.focus(HomeTreeDataLenses.colorPreview),
            )

            if (!resumeFromLastSearchedColorOnStartup.enabled) return@transaction
            if (color == null) return@transaction
            createAndConsumeNewColorCenterComponents()
            val deferredDetails = CompletableDeferred<DomainColorDetails>()
            startColorCenterSession(
                seed = color,
                deferredDetails = deferredDetails,
            )
            colorInputMediator.set(color)
            onColorBecameCurrent(color)
            proceed(color) { colorDetails, colorScheme ->
                // 'coroutineScope' is a context parameter, so it isn't an implicit extension
                // receiver and has to be named explicitly
                coroutineScope.launchAsFetchColorDetails { colorDetails.setSeedColor(color, deferredDetails) }
                coroutineScope.launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
            }
        }
        // the new state is committed and observable now, so the waiting operations can be released
        initialized.open()
    }

    private fun collectColorsFromColorInput(): Job =
        launchUntracked {
            colorInputMediator.colorStateFlow
                .drop(1) // replayed value
                .filter { it.source is ColorInputSource }
                .conflate()
                .collect(::onColorFromColorInput)
        }

    private suspend fun onColorFromColorInput(colorState: ColorInputMediator.ColorState) {
        store.transaction {
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
            is HomeAction.ClearProceedResult -> clearProceedResult()
            is HomeAction.OnSideEffectProcessed -> onSideEffectProcessed(action.se)
            is HomeAction.ClearColorSchemeSelectedSwatch -> clearColorSchemeSelectedSwatch()
        }

    private fun proceed(): Job =
        launchAsProceed launch@{
            val color = colorInputMediator.colorState.color ?: return@launch // invalid state
            store.transaction {
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
                    launchAsFetchColorDetails { colorDetails.setSeedColor(color, deferredDetails) }
                    launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
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
            dataStore.update {
                it.copy(proceedResult = proceedResult)
            }
        }
    }

    private fun randomizeColor(): Job =
        launchAsProceed launch@{
            store.transaction {
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
                            launchAsFetchColorDetails { colorDetails.setSeedColor(color, deferredDetails) }
                            launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
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

    private fun clearProceedResult(): Job =
        launchUntracked {
            dataStore.update {
                it.copy(proceedResult = null)
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
            components.store.update {
                ColorCenterTreeDataLenses.selectedSwatchDetails.set(it, ColorDetailsState.Idle)
            }
        }

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
        val newComponents = requireNotNull(colorCenterComponentsStore.components)
        store.updateReady {
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

    private suspend fun endColorCenterSession() {
        ccSessionStore.clear()
        colorCenterComponentsStore.disposeComponents()
        store.updateReady {
            it.copy(
                tree = it.tree.copy(
                    home = it.tree.home.copy(proceedResult = null),
                ),
                colorCenterHandles = null,
            )
        }
    }

    private suspend fun onColorBecameCurrent(color: Color?) {
        dataStore.update {
            val canProceed = CanProceed(colorFromColorInput = color)
            it.copy(canProceed = canProceed)
        }
        // TODO: doesn't look like a write to the same store under the ongoing transaction, but it is.
        //       replace ColorPreviewViewModel.setColor() with a colorPreviewStore.update() ?
        colorPreviewViewModel
            .let { requireNotNull(it) }
            .setColor(color)
    }

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
                                launchAsFetchColorDetails { colorDetails.setSeedColor(color, deferredDetails) }
                                launchAsFetchColorScheme { colorScheme.fetchColorScheme(color) }
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

    private fun launchAsProceed(
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        viewModelScope.launchSuperseding(
            context = defaultDispatcher,
            registry = opRegistry,
            value = Operation.Proceed,
            predicate = { it.value.isSupersededByProceed() },
            block = {
                initialized.awaitOpen()
                block()
            },
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

    /** Establishes a new 'Color Center' session. Supersedes every other operation. */
    data object Proceed : Operation

    /** Writes into the 'Color Details' of an ongoing session. */
    data object FetchColorDetails : Operation

    /** Writes into the 'Color Scheme' of an ongoing session. */
    data object FetchColorScheme : Operation

    /** Writes into the 'Color Details' of a swatch selected in the 'Color Scheme'. */
    data object UpdateSwatchColorDetails : Operation

    companion object {

        fun Operation.isSupersededByProceed(): Boolean =
            when (this) {
                // a new session invalidates every piece of work that belongs to the previous one
                is Proceed,
                is FetchColorDetails,
                is FetchColorScheme,
                is UpdateSwatchColorDetails, ->
                    true
            }

        fun Operation.isSupersededByFetchColorDetails(): Boolean =
            when (this) {
                is FetchColorDetails ->
                    true
                is Proceed,
                is FetchColorScheme,
                is UpdateSwatchColorDetails, ->
                    false
            }

        fun Operation.isSupersededByFetchColorScheme(): Boolean =
            when (this) {
                is FetchColorScheme ->
                    true
                is Proceed,
                is FetchColorDetails,
                is UpdateSwatchColorDetails, ->
                    false
            }

        fun Operation.isSupersededByUpdateSwatchColorDetails(): Boolean =
            when (this) {
                is UpdateSwatchColorDetails ->
                    true
                is Proceed,
                is FetchColorDetails,
                is FetchColorScheme, ->
                    false
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

private suspend fun Store<HomeState>.updateReady(
    transform: (HomeState.Ready) -> HomeState.Ready,
) {
    this.update {
        when (it) {
            is HomeState.Initializing -> error("HomeState must be Ready")
            is HomeState.Ready -> transform(it)
        }
    }
}