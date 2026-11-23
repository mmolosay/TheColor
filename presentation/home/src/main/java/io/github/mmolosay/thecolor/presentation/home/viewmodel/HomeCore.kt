package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.domain.color.isSameAs
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterCore
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

class HomeCore @AssistedInject constructor(
    colorEditorFactory: ColorEditorCore.Factory,
    colorInputGroupFactory: ColorInputGroupCore.Factory,
    colorCenterFactory: ColorCenterCore.Factory,
    colorDetailsFactory: ColorDetailsCore.Factory,
    private val colorComparator: ColorComparator,
    private val colorPreviewDataFactory: ColorPreviewDataFactory,
    private val createSessionColor: CreateSessionColorUseCase,
) {

    val colorEditorCore: ColorEditorCore = colorEditorFactory.create()
    val colorInputGroupCore: ColorInputGroupCore = colorInputGroupFactory.create()
    val colorCenterCore: ColorCenterCore = colorCenterFactory.create()
    val selectedSwatchCore: ColorDetailsCore = colorDetailsFactory.create()

    fun initialState(): HomeState {
        val colorEditor = colorEditorCore.initialState(color = null)
        val editorVersion = with(colorEditorCore) { colorEditor.version() }
        return HomeState(
            colorEditor = colorEditor,
            colorInputGroup = colorInputGroupCore.initialState(),
            colorCenter = colorCenterCore.initialState(),
            selectedSwatch = selectedSwatchCore.initialState(),
            swatchSession = null,
            startupColorRequest = HomeState.StartupColorRequest(colorEditorVersion = editorVersion),
            autoProceed = null,
            lastSearchedColorSaveRequest = null,
        )
    }

    // region Getters

    fun HomeState.isInitialized(): Boolean {
        val isColorInputGroupInit = with(colorInputGroupCore) { colorInputGroup.isInitialized() }
        val isStartupColorInit = (this.awaitedStartup() == null)
        val isAutoProceedInit = (this.autoProceed != null)
        return (isColorInputGroupInit && isStartupColorInit && isAutoProceedInit)
    }

    fun HomeState.awaitedStartup(): HomeState.StartupColorRequest? {
        val request = this.startupColorRequest ?: return null
        val editorVersion = with(colorEditorCore) { colorEditor.version() }
        return request.takeIf { it.colorEditorVersion == editorVersion }
    }

    fun HomeState.awaitedSaveRequest(): HomeState.LastSearchedColorSaveRequest? =
        this.lastSearchedColorSaveRequest

    fun HomeState.canProceed(): Boolean =
        (this.colorEditor.color != null)

    fun HomeState.toViewData(): HomeViewData? {
        if (!this.isInitialized()) return null
        val sessionColor = with(colorCenterCore) { colorCenter.sessionColor() }
        return HomeViewData(
            canProceed = this.canProceed(),
            sessionColor = sessionColor?.let { createSessionColor(it) },
            colorPreview = colorPreviewDataFactory.create(this.colorEditor.color),
            swatchSession = this.swatchSession,
        )
    }

    // endregion

    // region Setters

    private fun HomeState.withSession(color: Color): HomeState =
        this
            .withoutSwatchSession()
            .copy(
                colorEditor = with(colorEditorCore) { colorEditor.withColor(color) },
                colorCenter = with(colorCenterCore) { colorCenter.withSession(color) },
                lastSearchedColorSaveRequest = HomeState.LastSearchedColorSaveRequest(color),
            )

    private fun HomeState.withoutSession(): HomeState =
        this
            .withoutSwatchSession()
            .copy(
                colorCenter = with(colorCenterCore) { colorCenter.withoutSession() },
            )

    private fun HomeState.withoutStaleSession(): HomeState {
        val sessionColor = with(colorCenterCore) { colorCenter.sessionColor() } ?: return this
        if (context(colorComparator) { colorEditor.color isSameAs sessionColor }) return this
        return this.withoutSession()
    }

    private fun HomeState.withSwatchSession(details: DomainColorDetails): HomeState =
        this.copy(
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withSeedDetails(details) },
            swatchSession = SwatchSession(),
        )

    private fun HomeState.withoutSwatchSession(): HomeState =
        this.copy(
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
            swatchSession = null,
        )

    // endregion

    // region Transitions

    fun HomeState.proceed(): Change<HomeState, Nothing> {
        val color = this.colorEditor.color ?: return Change(this)
        val state = this.withSession(color)
        return Change(state)
    }

    fun HomeState.randomize(color: Color): Change<HomeState, Nothing> {
        if (this.autoProceed == true) {
            return Change(this.withSession(color))
        }
        val edited = this.copy(
            colorEditor = with(colorEditorCore) { colorEditor.withColor(color) },
        )
        val state = edited.withoutStaleSession()
        return Change(state)
    }

    fun HomeState.deselectSwatch(expected: SwatchSession): Change<HomeState, Nothing> {
        if (this.swatchSession != expected) return Change(this)
        val state = this.withoutSwatchSession()
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

    fun HomeState.onColorEditor(outcome: ColorEditorOutcome?): Change<HomeState, Nothing> {
        var state = this.withoutStaleSession()
        return when (outcome) {
            null -> Change(state)
            is ColorEditorOutcome.ColorSubmitted -> {
                val editor = with(colorEditorCore) {
                    state.colorEditor.withSubmissionAccepted()
                }
                state = state
                    .copy(colorEditor = editor)
                    .withSession(outcome.color)
                Change(state)
            }
        }
    }

    fun HomeState.onColorCenter(outcome: ColorCenterOutcome?): Change<HomeState, Nothing> =
        when (outcome) {
            null -> Change(this)
            is ColorCenterOutcome.SessionColorSelected -> {
                val editor = with(colorEditorCore) { colorEditor.withColor(outcome.color) }
                val state = this.copy(colorEditor = editor)
                Change(state)
            }
            is ColorCenterOutcome.SwatchSelected -> {
                val state = this.withSwatchSession(outcome.details)
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