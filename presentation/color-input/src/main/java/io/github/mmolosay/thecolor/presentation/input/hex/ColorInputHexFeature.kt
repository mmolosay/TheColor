package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateLenses as StateLenses

class ColorInputHexFeature @AssistedInject constructor(
    @Assisted private val core: ColorInputHexCore,
    @Assisted private val store: Store<ColorInputHexState, ColorInputHexOutcome>,
    @Assisted scope: CoroutineScope,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: StateFlow<ColorInputHexViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    val textField: TextFieldFeature = textFieldFactory.create(
        core = core.textFieldCore,
        store = store.slice(StateLenses.textField) { state -> Change(state) },
        scope = scope.child(),
    )

    fun ackAnswer(submission: ColorInputSubmission.Answered) {
        store.update { state ->
            with(core) { state.ackAnswer(submission) }
        }
    }

    fun submit() {
        store.update { state ->
            with(core) { state.submit() }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorInputHexCore,
            store: Store<ColorInputHexState, ColorInputHexOutcome>,
            scope: CoroutineScope,
        ): ColorInputHexFeature
    }
}