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
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorHandle
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses
import io.github.mmolosay.thecolor.presentation.input.editor.asAtom
import io.github.mmolosay.thecolor.presentation.input.editor.toData
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
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
    inputGroupFactory: ColorInputGroupFeature.Factory,
    colorCenterFactory: ColorCenterFeature.Factory,
    colorDetailsFactory: ColorDetailsFeature.Factory,
    colorInputHexViewModelFactory: ColorInputHexViewModel.Factory,
    colorInputRgbViewModelFactory: ColorInputRgbViewModel.Factory,
    colorInputHsvViewModelFactory: ColorInputHsvViewModel.Factory,
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

    // temporary: the old input ViewModels, on the editor's slices
    private val colorEditorHandle: ColorEditorHandle = run {
        val inputEditor = store.slice(StateLenses.inputEditor) { state ->
            with(core) { state.onInputEditor() }
        }
        val hexSlice = inputEditor.slice(ColorEditorStateLenses.hex) { state ->
            with(core.inputEditorCore) { state.onHex() }
        }
        val hexViewModel = colorInputHexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(viewModelScope),
            atom = hexSlice.asAtom(),
            submitAction = ColorInputSubmitActionImpl(),
        )
        val rgbSlice = inputEditor.slice(ColorEditorStateLenses.rgb) { state ->
            with(core.inputEditorCore) { state.onRgb() }
        }
        val rgbViewModel = colorInputRgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(viewModelScope),
            atom = rgbSlice.asAtom(),
            submitAction = ColorInputSubmitActionImpl(),
        )
        val hsvSlice = inputEditor.slice(ColorEditorStateLenses.hsv) { state ->
            with(core.inputEditorCore) { state.onHsv() }
        }
        val hsvViewModel = colorInputHsvViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(viewModelScope),
            atom = hsvSlice.asAtom(),
        )
        val colorEditorHandle = ColorEditorHandle(
            dataFlow = inputEditor.mapState { state -> state.toData() },
            hex = ColorInputHexHandle(hexViewModel),
            rgb = ColorInputRgbHandle(rgbViewModel),
            hsv = ColorInputHsvHandle(hsvViewModel),
        )
        colorEditorHandle
    }

    // temporary
    val stateFlow: StateFlow<HomeViewState?> = store.mapState { state ->
        with(core) {
            if (!state.isInitialized()) return@mapState null
            HomeViewState(
                home = state.data(),
                colorEditorHandle = colorEditorHandle,
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
            is HomeAction.OnProceedResultProcessed -> store.update { state ->
                with(core) { state.acknowledgeRejection() }
            }
            is HomeAction.OnSideEffectProcessed -> store.update { state ->
                with(core) { state.acknowledgeSideEffect(action.se) }
            }
            is HomeAction.ClearColorSchemeSelectedSwatch -> store.update { state ->
                with(core) { state.closeSwatch() }
            }
        }
        return null
    }

    // TODO: temporary
    private inner class ColorInputSubmitActionImpl : ColorInputSubmitAction {
        override fun invoke(
            colorInput: ColorInput,
            validationResult: ColorInputValidationResult,
        ): Boolean =
            when (validationResult) {
                is ColorInputValidationResult.Valid -> {
                    val color = validationResult.color
                    store.update { state ->
                        with(core) { state.submit(color) }
                    }
                    true
                }
                is ColorInputValidationResult.Invalid -> {
                    store.update { state ->
                        with(core) { state.rejectSubmission() }
                    }
                    false
                }
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