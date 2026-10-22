package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

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

class OuncesInputFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<OuncesInputState, OuncesInputOutcome>,
    @Assisted private val environment: OuncesInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
) {

    val viewData: ViewDataSource<OuncesInputViewData> =
        store.viewData { ounces -> context(environment) { ounces.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // nothing to decide: the field has no outcomes, and its environment keeps the rule for its text
        val slice = store.slice(textFieldLens) { ounces, _: Nothing? -> Change(ounces) }
        textFieldFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.textField,
        )
    }

    fun submit() {
        store.update { ounces ->
            context(environment) { ounces.submit() }
        }
    }

    fun answerShown(token: Token) {
        store.update { ounces ->
            context(environment) { ounces.answerShown(token) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
            store: Store<OuncesInputState, OuncesInputOutcome>,
            environment: OuncesInputEnvironment,
        ): OuncesInputFeature
    }
}

private val textFieldLens = Lens<OuncesInputState, TextFieldState>(
    get = { ounces -> ounces.textField },
    set = { ounces, textField -> ounces.copy(textField = textField) },
)

// The transitions behind the actions. Only the feature instance calls them. They're outside the
// class so that they can't reach its lifetime or store: they can run more than once, so they must
// have no side effects

context(_: OuncesInputEnvironment)
fun OuncesInputState.submit(): Change<OuncesInputState, OuncesInputOutcome> {
    val state = this.copy(submission = newSubmission())
    return Change(state, outcome = OuncesInputOutcome.Submitted)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.answerShown(token: Token): Change<OuncesInputState, OuncesInputOutcome> {
    val state = this.copy(submission = this.submission.shown(token))
    return Change(state)
}
