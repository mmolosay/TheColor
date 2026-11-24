package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.color.GetPredictableRandomColorUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterFeature
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorFeature
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.utils.composition.Change
import io.github.mmolosay.thecolor.utils.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.utils.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.utils.composition.Store
import io.github.mmolosay.thecolor.utils.composition.slice
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeStateLenses as StateLenses

@HiltViewModel
class HomeViewModel @Inject constructor(
    coreFactory: HomeCore.Factory,
    colorEditorFactory: ColorEditorFeature.Factory,
    colorInputGroupFactory: ColorInputGroupFeature.Factory,
    colorCenterFactory: ColorCenterFeature.Factory,
    colorDetailsFactory: ColorDetailsFeature.Factory,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
    private val getPredictableRandomColor: GetPredictableRandomColorUseCase,
) : ViewModel() {

    private val core: HomeCore = coreFactory.create()
    private val store = Store(initialState = core.initialState())

    val viewData: StateFlow<HomeViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val colorEditor: ColorEditorFeature = colorEditorFactory.create(
        core = core.colorEditorCore,
        store = store.slice(StateLenses.colorEditor) { state, outcome: ColorEditorOutcome? ->
            with(core) { state.onColorEditor(outcome) }
        },
        scope = viewModelScope.child(),
    )

    val colorInputGroup: ColorInputGroupFeature = colorInputGroupFactory.create(
        core = core.colorInputGroupCore,
        store = store.slice(StateLenses.colorInputGroup) { state -> Change(state) },
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
        collectAutoProceed()
        serveLastSearchedColorSave()
    }

    private fun serveStartup() {
        val request = with(core) { store.value.awaitedStartup() } ?: return
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            val color = run {
                val resume = userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup
                    .readyValues(DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup)
                    .first()
                if (!resume.enabled) return@run null
                val lastSearched = lastSearchedColorRepository.flowOfLastSearchedColor
                    .filterReady().first()
                return@run lastSearched.result.getOrElse { null }
            }
            store.update { state ->
                with(core) { state.completeStartup(color, request) }
            }
        }
    }

    private fun collectAutoProceed() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            userPreferencesRepository.flowOfAutoProceedWithRandomizedColors
                .readyValues(DefaultUserPreferences.AutoProceedWithRandomizedColors)
                .collect { preference ->
                    store.update { state ->
                        with(core) { state.applyAutoProceed(preference.enabled) }
                    }
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

    fun deselectSwatch(expected: SwatchSession) {
        store.update { state ->
            with(core) { state.deselectSwatch(expected) }
        }
    }
}