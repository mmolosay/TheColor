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
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject

class HomeCore @AssistedInject constructor(
    inputEditorFactory: ColorEditorCore.Factory,
    inputGroupFactory: ColorInputGroupCore.Factory,
    colorCenterFactory: ColorCenterCore.Factory,
    colorDetailsFactory: ColorDetailsCore.Factory,
    private val colorComparator: ColorComparator,
    private val colorPreviewDataFactory: ColorPreviewDataFactory,
    private val createColorData: CreateColorDataUseCase,
) {

    val inputEditorCore: ColorEditorCore = inputEditorFactory.create()
    val inputGroupCore: ColorInputGroupCore = inputGroupFactory.create()
    val colorCenterCore: ColorCenterCore = colorCenterFactory.create()
    val selectedSwatchCore: ColorDetailsCore = colorDetailsFactory.create()

    fun initialState(): HomeState {
        val inputEditor = inputEditorCore.initialState(color = null)
        return HomeState(
            inputEditor = inputEditor,
            inputGroup = inputGroupCore.initialState(),
            colorCenter = colorCenterCore.initialState(),
            selectedSwatch = selectedSwatchCore.initialState(),
            startupColorRequest = HomeState.StartupColorRequest(editorVersion = inputEditor.version),
            autoProceed = null,
            lastSearchedColorSave = null,
            submissionRejected = false,
            sideEffects = persistentListOf(),
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
        return request.takeIf { it.editorVersion == this.inputEditor.version }
    }

    fun HomeState.awaitedSave(): HomeState.LastSearchedColorSave? =
        this.lastSearchedColorSave

    fun HomeState.canProceed(): Boolean =
        (this.inputEditor.color != null)

    fun HomeState.colorPreview(): ColorPreviewData =
        colorPreviewDataFactory.create(this.inputEditor.color)

    // TODO: Core probably shouldn't reference 'HomeData'
    private fun HomeState.proceedResult(): HomeData.ProceedResult? {
        if (this.submissionRejected) return HomeData.ProceedResult.InvalidSubmittedColor
        val color = with(colorCenterCore) { colorCenter.sessionColor() } ?: return null
        val colorData = createColorData(color)
        return HomeData.ProceedResult.Success(colorData)
    }

    // TODO: temporary, today's 'HomeData'
    fun HomeState.data(): HomeData =
        HomeData(
            canProceed = this.canProceed(),
            proceedResult = this.proceedResult(),
            sideEffects = this.sideEffects,
        )

    // endregion

    // region Setters

    private fun HomeState.withSession(color: Color): HomeState =
        this.copy(
            inputEditor = with(inputEditorCore) { inputEditor.withColor(color) },
            colorCenter = with(colorCenterCore) { colorCenter.withSession(color) },
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
            lastSearchedColorSave = HomeState.LastSearchedColorSave(color),
            submissionRejected = false,
        )

    private fun HomeState.withoutSession(): HomeState =
        this.copy(
            colorCenter = with(colorCenterCore) { colorCenter.withoutSession() },
            selectedSwatch = with(selectedSwatchCore) { selectedSwatch.withoutSession() },
        )

    // TODO: refine name
    private fun HomeState.withAgreement(): HomeState {
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
        val state = edited.withAgreement()
        return Change(state)
    }

    fun HomeState.closeSwatch(): Change<HomeState, Nothing> {
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
        expected: HomeState.LastSearchedColorSave,
    ): Change<HomeState, Nothing> {
        if (this.awaitedSave() != expected) return Change(this)
        val state = this.copy(lastSearchedColorSave = null)
        return Change(state)
    }

    // TODO: temporary, until the inputs submit through outcomes
    fun HomeState.submit(color: Color): Change<HomeState, Nothing> =
        Change(this.withSession(color))

    fun HomeState.rejectSubmission(): Change<HomeState, Nothing> =
        Change(this.copy(submissionRejected = true))

    fun HomeState.ackRejection(): Change<HomeState, Nothing> =
        Change(this.copy(submissionRejected = false))

    // TODO: temporary, until going to Settings is state of its own
    fun HomeState.addSideEffect(se: HomeData.SideEffect): Change<HomeState, Nothing> =
        Change(this.copy(sideEffects = (this.sideEffects.toPersistentList() + se)))

    fun HomeState.ackSideEffect(se: HomeData.SideEffect): Change<HomeState, Nothing> =
        Change(this.copy(sideEffects = (this.sideEffects.toPersistentList() - se)))

    fun HomeState.onInputEditor(outcome: ColorEditorOutcome?): Change<HomeState, Nothing> {
        var state = this.withAgreement()
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