package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.presentation.feature.input.newSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.shown
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class OuncesInputFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<OuncesInputState, OuncesInputOutcome>,
    @Assisted private val environment: OuncesInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
    settings: DemoSettings, // from dependency injection: only the subscription uses it
) {

    val viewData: StateFlow<OuncesInputViewData> =
        store.mapState { ounces -> context(environment) { ounces.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // the field has no outcomes, and its environment keeps the rule for its text
        val slice = store.slice(textFieldLens) { ounces, _: Nothing? ->
            context(environment) { onTextField(ounces) }
        }
        textFieldFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.textField,
        )
    }

    init {
        // for its whole lifetime: the setting has one writer, so each value applies as it arrives
        lifetime.launch {
            settings.decimalSeparator.filterNotNull().collect { separator ->
                store.update { ounces ->
                    context(environment) { ounces.receiveSeparator(separator) }
                }
            }
        }
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

// The reducer for its Text Field, which runs only when the field's own edit changes its text
context(_: OuncesInputEnvironment)
fun onTextField(ounces: OuncesInputState): Change<OuncesInputState, OuncesInputOutcome> {
    val state = ounces.withTextChanged()
    return Change(state)
}

// The transition behind the setting's value. The text holds a point either way, so a keystroke
// made on the text shown with the old separator still applies
context(_: OuncesInputEnvironment)
fun OuncesInputState.receiveSeparator(
    separator: DecimalSeparator,
): Change<OuncesInputState, OuncesInputOutcome> {
    val state = this.copy(separator = separator)
    return Change(state)
}

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
