package io.github.mmolosay.thecolor.presentation.input.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.composition.Change
import io.github.mmolosay.thecolor.utils.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.utils.composition.Store
import io.github.mmolosay.thecolor.utils.composition.slice
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateLenses as StateLenses

class ColorInputRgbFeature @AssistedInject constructor(
    @Assisted private val core: ColorInputRgbCore,
    @Assisted private val store: Store<ColorInputRgbState, ColorInputRgbOutcome>,
    @Assisted private val scope: CoroutineScope,
    textFieldFactory: TextFieldFeature.Factory,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    val viewData: StateFlow<ColorInputRgbViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val r: TextFieldFeature = textFieldFactory.create(
        core = core.rCore,
        store = store.slice(StateLenses.r) { state -> Change(state) },
        scope = scope.child(),
    )
    val g: TextFieldFeature = textFieldFactory.create(
        core = core.gCore,
        store = store.slice(StateLenses.g) { state -> Change(state) },
        scope = scope.child(),
    )
    val b: TextFieldFeature = textFieldFactory.create(
        core = core.bCore,
        store = store.slice(StateLenses.b) { state -> Change(state) },
        scope = scope.child(),
    )

    init {
        collectSmartBackspace()
    }

    private fun collectSmartBackspace() {
        scope.launch {
            userPreferencesRepository.flowOfSmartBackspace
                .readyValues(DefaultUserPreferences.SmartBackspace)
                .collect { preference ->
                    store.update { state ->
                        with(core) { state.applySmartBackspace(preference.enabled) }
                    }
                }
        }
    }

    fun submit() {
        store.update { state ->
            with(core) { state.submit() }
        }
    }

    fun ackAnswer(submission: ColorInputSubmission.Answered) {
        store.update { state ->
            with(core) { state.ackAnswer(submission) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorInputRgbCore,
            store: Store<ColorInputRgbState, ColorInputRgbOutcome>,
            scope: CoroutineScope,
        ): ColorInputRgbFeature
    }
}