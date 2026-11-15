package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.domain.color.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.color.GetStartupColorUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterFeature
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorFeature
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.utils.SideEffectIdFactory
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeStateLenses as StateLenses

@HiltViewModel
class HomeViewModel @Inject constructor(
    coreFactory: HomeCore.Factory,
    colorEditorFactory: ColorEditorFeature.Factory,
    inputGroupFactory: ColorInputGroupFeature.Factory,
    colorCenterFactory: ColorCenterFeature.Factory,
    colorDetailsFactory: ColorDetailsFeature.Factory,
    private val getStartupColor: GetStartupColorUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : ViewModel(
    viewModelScope = CoroutineScope(SupervisorJob() + defaultDispatcher),
) {

    private val core: HomeCore = coreFactory.create()
    private val store = Store(initialState = core.initialState())
    private val seFactory = SideEffectFactory()

    private val colorEditor: ColorEditorFeature = colorEditorFactory.create(
        core = core.inputEditorCore,
        store = store.slice(StateLenses.inputEditor) { state, outcome: ColorEditorOutcome? ->
            with(core) { state.onInputEditor(outcome) }
        },
        scope = viewModelScope.child(),
    )

    private val colorInputGroup: ColorInputGroupFeature = inputGroupFactory.create(
        core = core.inputGroupCore,
        store = store.slice(StateLenses.inputGroup) { state -> Change(state) },
        scope = viewModelScope.child(),
    )

    private val colorCenter: ColorCenterFeature = colorCenterFactory.create(
        core = core.colorCenterCore,
        store = store.slice(StateLenses.colorCenter) { state, outcome: ColorCenterOutcome? ->
            with(core) { state.onColorCenter(outcome) }
        },
        scope = viewModelScope.child(),
    )

    private val selectedSwatch: ColorDetailsFeature = colorDetailsFactory.create(
        core = core.selectedSwatchCore,
        store = store.slice(StateLenses.selectedSwatch) { state, _: ColorDetailsOutcome? ->
            Change(state)
        },
        scope = viewModelScope.child(),
    )

    // TODO: temporary
    val stateFlow: StateFlow<HomeViewState?> = store.mapState { state ->
        with(core) {
            if (!state.isInitialized()) return@mapState null
            HomeViewState(
                home = state.data(),
                colorEditor = colorEditor,
                colorInputGroup = colorInputGroup,
                colorPreview = state.colorPreview(),
                colorCenter = colorCenter,
                selectedSwatch = selectedSwatch,
            )
        }
    }

    init {
        serveStartup()
        serveSave()
        collectAutoProceed()
    }

    private fun serveStartup() {
        viewModelScope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedStartup() } },
        ) { request ->
            val color = getStartupColor()
            store.update { state ->
                with(core) { state.completeStartup(color, request) }
            }
        }
    }

    private fun serveSave() {
        viewModelScope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedSave() } },
        ) { save ->
            lastSearchedColorRepository.setLastSearchedColor(save.color)
            store.update { state ->
                with(core) { state.completeSave(save) }
            }
        }
    }

    private fun collectAutoProceed() {
        viewModelScope.launch {
            userPreferencesRepository.flowOfAutoProceedWithRandomizedColors
                .filterReady()
                .collect { preference ->
                    val enabled = preference
                        .getOrElse { DefaultUserPreferences.AutoProceedWithRandomizedColors }
                        .enabled
                    store.update { state ->
                        with(core) { state.applyAutoProceed(enabled) }
                    }
                }
        }
    }

    // TODO: temporary: one method per action comes with Home finished
    fun execute(action: HomeAction): Job? {
        when (action) {
            is HomeAction.Proceed -> store.update { state ->
                with(core) { state.proceed() }
            }
            is HomeAction.RandomizeColor -> {
                val color = getPredictableRandomColor()
                store.update { state ->
                    with(core) { state.randomize(color) }
                }
            }
            is HomeAction.RequestToGoToSettings -> {
                val se = seFactory.goToSettings()
                store.update { state ->
                    with(core) { state.addSideEffect(se) }
                }
            }
            is HomeAction.OnSideEffectProcessed -> store.update { state ->
                with(core) { state.ackSideEffect(action.se) }
            }
            is HomeAction.ClearColorSchemeSelectedSwatch -> store.update { state ->
                with(core) { state.closeSwatch() }
            }
        }
        return null
    }
}

private class SideEffectFactory {

    private val idFactory = SideEffectIdFactory()

    fun goToSettings(): SideEffect.GoToSettings =
        SideEffect.GoToSettings(
            id = idFactory.get(),
        )
}