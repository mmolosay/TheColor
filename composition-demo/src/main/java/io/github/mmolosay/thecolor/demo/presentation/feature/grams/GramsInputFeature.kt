package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
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
        // nothing to decide: the field has no outcomes, its environment keeps the rule for its text,
        // and a submission made on older text is no longer pending
        val slice = store.slice(textFieldLens) { grams -> Change(grams) }
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

// The transitions behind the actions. Only the feature instance calls them. They're outside the
// class so that they can't reach its scope or store: they can run more than once, so they must
// have no side effects

// for the text as it is now: a change to it withdraws the submission
context(_: GramsInputEnvironment)
fun GramsInputState.submit(): Change<GramsInputState, GramsInputOutcome> {
    val submission = Submission(answer = null, version = this.version())
    val state = this.copy(submission = submission)
    return Change(state, outcome = GramsInputOutcome.Submitted)
}

context(_: GramsInputEnvironment)
fun GramsInputState.answerShown(
    submission: Submission,
): Change<GramsInputState, GramsInputOutcome> {
    val state = this.copy(submission = this.submission.shown(submission))
    return Change(state)
}
