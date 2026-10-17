package io.github.mmolosay.thecolor.demo.grams

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Moment
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.demo.input.newSubmission
import io.github.mmolosay.thecolor.demo.input.shown
import io.github.mmolosay.thecolor.demo.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.textfield.text
import io.github.mmolosay.thecolor.demo.textfield.withText
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class GramsInputFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<GramsInputState, GramsInputOutcome>,
    @Assisted private val environment: GramsInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val moment: Moment<GramsInputState>
        get() = store.moment

    val moments: Flow<Moment<GramsInputState>>
        get() = store.moments

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        val slice = store.slice(textFieldLens) { grams, outcome: Nothing? ->
            context(environment) { onTextField(grams, outcome) }
        }
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

// The transitions behind the actions, and the reducer for its Text Field. Only the feature instance
// calls them. They're outside the class so that they can't reach its lifetime or store: they can
// run more than once, so they must have no side effects

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

// keeps only the text it allows: digits, up to four, no leading zeros
context(environment: GramsInputEnvironment)
fun onTextField(
    grams: GramsInputState,
    outcome: Nothing?, // the field has no outcomes
): Change<GramsInputState, GramsInputOutcome> {
    val textField = context(environment.textField) {
        val text = grams.textField.text()
        val allowed = text.filter { it in '0'..'9' }.trimStart('0').take(4)
        grams.textField.withText(allowed)
    }
    val state = grams.copy(textField = textField)
    return Change(state)
}
