package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.shown
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class GramsInputFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<GramsInputState, GramsInputOutcome>,
    @Assisted private val environment: GramsInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: StateFlow<GramsInputViewData> =
        store.mapState { grams -> context(environment) { grams.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // the field has no outcomes, and its environment keeps the rule for its text
        val slice = store.slice(textFieldLens) { grams, _: Nothing? ->
            context(environment) { onTextField(grams) }
        }
        textFieldFactory.create(
            scope = scope.child(),
            store = slice,
            environment = environment.textField,
        )
    }

    fun submit() {
        store.update { grams ->
            context(environment) { grams.submit() }
        }
    }

    fun answerShown(submission: Submission) {
        store.update { grams ->
            context(environment) { grams.answerShown(submission) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<GramsInputState, GramsInputOutcome>,
            environment: GramsInputEnvironment,
        ): GramsInputFeature
    }
}

private val textFieldLens = Lens<GramsInputState, TextFieldState>(
    get = { grams -> grams.textField },
    set = { grams, textField -> grams.copy(textField = textField) },
)

// The reducer for its Text Field, which runs only when the field's own edit changes its text
context(_: GramsInputEnvironment)
fun onTextField(grams: GramsInputState): Change<GramsInputState, GramsInputOutcome> {
    val state = grams.withTextChanged()
    return Change(state)
}

// The transitions behind the actions. Only the feature instance calls them. They're outside the
// class so that they can't reach its scope or store: they can run more than once, so they must
// have no side effects

context(_: GramsInputEnvironment)
fun GramsInputState.submit(): Change<GramsInputState, GramsInputOutcome> {
    val state = this.copy(submission = Submission(answer = null))
    return Change(state, outcome = GramsInputOutcome.Submitted)
}

context(_: GramsInputEnvironment)
fun GramsInputState.answerShown(
    submission: Submission,
): Change<GramsInputState, GramsInputOutcome> {
    val state = this.copy(submission = this.submission.shown(submission))
    return Change(state)
}
