package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputStateLenses as StateLenses

class GramsInputFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<GramsInputState, GramsInputOutcome>,
    @Assisted private val core: GramsInputCore,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: StateFlow<GramsInputViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // nothing to decide: the field has no outcomes, its core keeps the rule for its text, and a
        // submission made on older text is no longer pending
        val slice = store.slice(StateLenses.textField) { state -> Change(state) }
        textFieldFactory.create(
            scope = scope.child(),
            store = slice,
            core = core.textFieldCore,
        )
    }

    fun answerShown(submission: Submission) {
        store.update { state -> with(core) { state.answerShown(submission) } }
    }

    fun submit() {
        store.update { state -> with(core) { state.submit() } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<GramsInputState, GramsInputOutcome>,
            core: GramsInputCore,
        ): GramsInputFeature
    }
}
