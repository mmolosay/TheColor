package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.domain.DemoSettings
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
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
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<OuncesInputState, OuncesInputOutcome>,
    @Assisted private val environment: OuncesInputEnvironment,
    textFieldFactory: TextFieldFeature.Factory,
    settings: DemoSettings, // from dependency injection: only the subscription uses it
) {

    val viewData: StateFlow<OuncesInputViewData> =
        store.mapState { ounces -> context(environment) { ounces.toViewData() } }

    // for its View, which shows the field's View
    val textField: TextFieldFeature = run {
        // nothing to decide: the field has no outcomes, its environment keeps the rule for its text,
        // and a submission made on older text is no longer pending
        val slice = store.slice(textFieldLens) { ounces -> Change(ounces) }
        textFieldFactory.create(
            scope = scope.child(),
            store = slice,
            environment = environment.textField,
        )
    }

    init {
        // while its scope is active: the setting has one writer, so each value applies as it arrives
        scope.launch {
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

    fun answerShown(submission: Submission) {
        store.update { ounces ->
            context(environment) { ounces.answerShown(submission) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<OuncesInputState, OuncesInputOutcome>,
            environment: OuncesInputEnvironment,
        ): OuncesInputFeature
    }
}

private val textFieldLens = Lens<OuncesInputState, TextFieldState>(
    get = { ounces -> ounces.textField },
    set = { ounces, textField -> ounces.copy(textField = textField) },
)

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
// class so that they can't reach its scope or store: they can run more than once, so they must
// have no side effects

// for the text as it is now: a change to it withdraws the submission
context(_: OuncesInputEnvironment)
fun OuncesInputState.submit(): Change<OuncesInputState, OuncesInputOutcome> {
    val submission = Submission(answer = null, version = this.version())
    val state = this.copy(submission = submission)
    return Change(state, outcome = OuncesInputOutcome.Submitted)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.answerShown(
    submission: Submission,
): Change<OuncesInputState, OuncesInputOutcome> {
    val state = this.copy(submission = this.submission.shown(submission))
    return Change(state)
}
