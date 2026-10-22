package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.composition.ViewDataSource
import io.github.mmolosay.thecolor.composition.viewData
import io.github.mmolosay.thecolor.demo.presentation.feature.input.newSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.shown
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.CoroutineScope

class GramsInputFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<GramsInputState, GramsInputOutcome>,
    @Assisted private val environment: GramsInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: ViewDataSource<GramsInputViewData> =
        store.viewData { grams -> context(environment) { grams.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // nothing to decide: the field has no outcomes, and its environment keeps the rule for its text
        val slice = store.slice(textFieldLens) { grams, _: Nothing? -> Change(grams) }
        textFieldFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.textField,
        )
    }

    fun submit() {
        store.update { grams ->
            context(environment) { grams.submit() }
        }
    }

    fun answerShown(token: Token) {
        store.update { grams ->
            context(environment) { grams.answerShown(token) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
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
// class so that they can't reach its lifetime or store: they can run more than once, so they must
// have no side effects

context(_: GramsInputEnvironment)
fun GramsInputState.submit(): Change<GramsInputState, GramsInputOutcome> {
    val state = this.copy(submission = newSubmission())
    return Change(state, outcome = GramsInputOutcome.Submitted)
}

context(_: GramsInputEnvironment)
fun GramsInputState.answerShown(token: Token): Change<GramsInputState, GramsInputOutcome> {
    val state = this.copy(submission = this.submission.shown(token))
    return Change(state)
}
