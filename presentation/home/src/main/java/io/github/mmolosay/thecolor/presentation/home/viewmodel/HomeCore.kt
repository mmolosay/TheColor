package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.domain.color.isSameAs
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterCore
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import javax.inject.Inject

class HomeCore @AssistedInject constructor(
    inputEditorFactory: ColorEditorCore.Factory,
    inputGroupFactory: ColorInputGroupCore.Factory,
    colorCenterFactory: ColorCenterCore.Factory,
    colorDetailsFactory: ColorDetailsCore.Factory,
    private val colorComparator: ColorComparator,
    private val colorPreviewDataFactory: ColorPreviewDataFactory,
    private val createSessionColor: CreateSessionColorUseCase,
) {

    val inputEditorCore: ColorEditorCore = inputEditorFactory.create()
    val inputGroupCore: ColorInputGroupCore = inputGroupFactory.create()
    val colorCenterCore: ColorCenterCore = colorCenterFactory.create()
    val selectedSwatchCore: ColorDetailsCore = colorDetailsFactory.create()

    fun initialState(): HomeState {
        val inputEditor = inputEditorCore.initialState(color = null)
        val editorVersion = with(inputEditorCore) { inputEditor.version() }
        return HomeState(
            inputEditor = inputEditor,
            inputGroup = inputGroupCore.initialState(),
            colorCenter = colorCenterCore.initialState(),
            selectedSwatch = selectedSwatchCore.initialState(),
            startupColorRequest = HomeState.StartupColorRequest(editorVersion = editorVersion),
            autoProceed = null,
            lastSearchedColorSaveRequest = null,
        )
    }

    // region Getters

    fun HomeState.isInitialized(): Boolean {
        val isInputGroupInit = with(inputGroupCore) { inputGroup.isInitialized() }
        val isStartupColorInit = (this.awaitedStartup() == null)
        val isAutoProceedInit = (this.autoProceed != null)
        return (isInputGroupInit && isStartupColorInit && isAutoProceedInit)
    }

    fun HomeState.awaitedStartup(): HomeState.StartupColorRequest? {
        val request = this.startupColorRequest ?: return null
        val editorVersion = with(inputEditorCore) { inputEditor.version() }
        return request.takeIf { it.editorVersion == editorVersion }
    }

    fun HomeState.awaitedSaveRequest(): HomeState.LastSearchedColorSaveRequest? =
        this.lastSearchedColorSaveRequest

    fun HomeState.canProceed(): Boolean =
        (this.inputEditor.color != null)

    fun HomeState.toViewData(): HomeViewData? {
        if (!this.isInitialized()) return null
        val sessionColor = with(colorCenterCore) { colorCenter.sessionColor() }
        return HomeViewData(
            canProceed = this.canProceed(),
            sessionColor = sessionColor?.let { createSessionColor(it) },
            colorPreview = colorPreviewDataFactory.create(this.inputEditor.color)
        )
    }

    // endregion

    // region Setters

    private fun HomeState.withSession(color: Color): HomeState =
        this.copy(
            inputEditor = with(inputEditorCore) { inputEditor.withColor(color) },
            colorCenter = with(colorCenterCore) { colorCenter.withSession(color) },
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
            lastSearchedColorSaveRequest = HomeState.LastSearchedColorSaveRequest(color),
        )

    private fun HomeState.withoutSession(): HomeState =
        this.copy(
            colorCenter = with(colorCenterCore) { colorCenter.withoutSession() },
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
        )

    private fun HomeState.withoutStaleSession(): HomeState {
        val sessionColor = with(colorCenterCore) { colorCenter.sessionColor() } ?: return this
        if (context(colorComparator) { inputEditor.color isSameAs sessionColor }) return this
        return this.withoutSession()
    }

    // endregion

    // region Transitions

    fun HomeState.proceed(): Change<HomeState, Nothing> {
        val color = this.inputEditor.color ?: return Change(this)
        val state = this.withSession(color)
        return Change(state)
    }

    fun HomeState.randomize(color: Color): Change<HomeState, Nothing> {
        if (this.autoProceed == true) {
            return Change(this.withSession(color))
        }
        val edited = this.copy(
            inputEditor = with(inputEditorCore) { inputEditor.withColor(color) },
        )
        val state = edited.withoutStaleSession()
        return Change(state)
    }

    fun HomeState.deselectSwatch(): Change<HomeState, Nothing> {
        val state = this.copy(
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
        )
        return Change(state)
    }

    fun HomeState.applyAutoProceed(enabled: Boolean): Change<HomeState, Nothing> {
        val state = this.copy(autoProceed = enabled)
        return Change(state)
    }

    fun HomeState.completeStartup(
        color: Color?,
        expected: HomeState.StartupColorRequest,
    ): Change<HomeState, Nothing> {
        if (this.awaitedStartup() != expected) return Change(this)
        val completed = this.copy(startupColorRequest = null)
        val state = if (color != null) completed.withSession(color) else completed
        return Change(state)
    }

    fun HomeState.completeSave(
        expected: HomeState.LastSearchedColorSaveRequest,
    ): Change<HomeState, Nothing> {
        if (this.awaitedSaveRequest() != expected) return Change(this)
        val state = this.copy(lastSearchedColorSaveRequest = null)
        return Change(state)
    }

    fun HomeState.onInputEditor(outcome: ColorEditorOutcome?): Change<HomeState, Nothing> {
        var state = this.withoutStaleSession()
        return when (outcome) {
            null -> Change(state)
            is ColorEditorOutcome.ColorSubmitted -> {
                val inputEditor = with(inputEditorCore) {
                    state.inputEditor.withSubmissionAccepted()
                }
                state = state
                    .copy(inputEditor = inputEditor)
                    .withSession(outcome.color)
                Change(state)
            }
        }
    }

    fun HomeState.onColorCenter(outcome: ColorCenterOutcome?): Change<HomeState, Nothing> =
        when (outcome) {
            null -> Change(this)
            is ColorCenterOutcome.SessionColorSelected -> {
                val editor = with(inputEditorCore) { inputEditor.withColor(outcome.color) }
                val state = this.copy(inputEditor = editor)
                Change(state)
            }
            is ColorCenterOutcome.SwatchSelected -> {
                val swatch = with(selectedSwatchCore) { selectedSwatch.withSeedDetails(outcome.details) }
                val state = this.copy(selectedSwatch = swatch)
                Change(state)
            }
        }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): HomeCore
    }
}

class CreateSessionColorUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        HomeViewData.SessionColor(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}