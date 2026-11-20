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
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterFeature
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorFeature
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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

    val viewData: StateFlow<HomeViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val colorEditor: ColorEditorFeature = colorEditorFactory.create(
        core = core.inputEditorCore,
        store = store.slice(StateLenses.inputEditor) { state, outcome: ColorEditorOutcome? ->
            with(core) { state.onInputEditor(outcome) }
        },
        scope = viewModelScope.child(),
    )

    val colorInputGroup: ColorInputGroupFeature = inputGroupFactory.create(
        core = core.inputGroupCore,
        store = store.slice(StateLenses.inputGroup) { state -> Change(state) },
        scope = viewModelScope.child(),
    )

    val colorCenter: ColorCenterFeature = colorCenterFactory.create(
        core = core.colorCenterCore,
        store = store.slice(StateLenses.colorCenter) { state, outcome: ColorCenterOutcome? ->
            with(core) { state.onColorCenter(outcome) }
        },
        scope = viewModelScope.child(),
    )

    val selectedSwatch: ColorDetailsFeature = colorDetailsFactory.create(
        core = core.selectedSwatchCore,
        store = store.slice(StateLenses.selectedSwatch) { state, _: ColorDetailsOutcome? ->
            Change(state)
        },
        scope = viewModelScope.child(),
    )

    init {
        serveStartup()
        serveLastSearchedColorSave()
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

    private fun serveLastSearchedColorSave() {
        viewModelScope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedSaveRequest() } },
        ) { request ->
            lastSearchedColorRepository.setLastSearchedColor(request.color)
            store.update { state ->
                with(core) { state.completeSave(request) }
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

    fun proceed() {
        store.update { state ->
            with(core) { state.proceed() }
        }
    }

    fun randomize() {
        val color = getPredictableRandomColor()
        store.update { state ->
            with(core) { state.randomize(color) }
        }
    }

    fun deselectSwatch() {
        store.update { state ->
            with(core) { state.deselectSwatch() }
        }
    }
}