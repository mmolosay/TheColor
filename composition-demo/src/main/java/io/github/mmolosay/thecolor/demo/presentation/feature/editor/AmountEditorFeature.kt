package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialState
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.withSubmissionRejected
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.withSubmissionRejected
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.CoroutineScope

// No view data of its own: its View only lays out the inputs' Views
class AmountEditorFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted store: Store<AmountEditorState, AmountEditorOutcome>,
    @Assisted environment: AmountEditorEnvironment,
    gramsFactory: GramsInputFeature.Factory,
    ouncesFactory: OuncesInputFeature.Factory,
    cupsFactory: CupDialFeature.Factory,
) {

    // for its View, which shows the inputs' Views
    val grams: GramsInputFeature = run {
        val slice = store.slice(gramsLens) { editor, outcome: GramsInputOutcome? ->
            context(environment) { onGrams(editor, outcome) }
        }
        gramsFactory.create(
            scope = scope.child(),
            store = slice,
            environment = environment.grams,
        )
    }

    val ounces: OuncesInputFeature = run {
        val slice = store.slice(ouncesLens) { editor, outcome: OuncesInputOutcome? ->
            context(environment) { onOunces(editor, outcome) }
        }
        ouncesFactory.create(
            scope = scope.child(),
            store = slice,
            environment = environment.ounces,
        )
    }

    val cups: CupDialFeature = run {
        val slice = store.slice(cupsLens) { editor, _: Nothing? ->
            context(environment) { onCups(editor) }
        }
        cupsFactory.create(
            scope = scope.child(),
            store = slice,
            environment = environment.cups,
        )
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<AmountEditorState, AmountEditorOutcome>,
            environment: AmountEditorEnvironment,
        ): AmountEditorFeature
    }
}

private val gramsLens = Lens<AmountEditorState, GramsInputState>(
    get = { editor -> editor.grams },
    set = { editor, grams -> editor.copy(grams = grams) },
)

private val ouncesLens = Lens<AmountEditorState, OuncesInputState>(
    get = { editor -> editor.ounces },
    set = { editor, ounces -> editor.copy(ounces = ounces) },
)

private val cupsLens = Lens<AmountEditorState, CupDialState>(
    get = { editor -> editor.cups },
    set = { editor, cups -> editor.copy(cups = cups) },
)

// The reducers for its inputs. Only the feature instance runs them, inside an input's update. Each
// brings the other inputs in line with the edited one. An edit that leaves no whole amount, such as
// clearing the text, leaves the other input as it is. A submission that stands for no amount is
// rejected here; a whole amount is handed up, for the parent to answer

context(environment: AmountEditorEnvironment)
fun onGrams(
    editor: AmountEditorState,
    outcome: GramsInputOutcome?,
): Change<AmountEditorState, AmountEditorOutcome> {
    val amount = context(environment.grams) { editor.grams.amount() }
    val agreed = if (amount != null) editor.withAmount(amount) else editor
    return when (outcome) {
        null -> Change(agreed)
        GramsInputOutcome.Submitted -> {
            if (amount == null) {
                val grams = context(environment.grams) { agreed.grams.withSubmissionRejected() }
                return Change(agreed.copy(grams = grams))
            }
            Change(agreed, outcome = AmountEditorOutcome.Submitted(amount))
        }
    }
}

context(environment: AmountEditorEnvironment)
fun onOunces(
    editor: AmountEditorState,
    outcome: OuncesInputOutcome?,
): Change<AmountEditorState, AmountEditorOutcome> {
    val amount = context(environment.ounces) { editor.ounces.amount() }
    val agreed = if (amount != null) editor.withAmount(amount) else editor
    return when (outcome) {
        null -> Change(agreed)
        OuncesInputOutcome.Submitted -> {
            if (amount == null) {
                val ounces = context(environment.ounces) { agreed.ounces.withSubmissionRejected() }
                return Change(agreed.copy(ounces = ounces))
            }
            Change(agreed, outcome = AmountEditorOutcome.Submitted(amount))
        }
    }
}

// The dial has no submissions. Each sample that lands brings the other inputs in line with it, as a
// typed amount does
context(environment: AmountEditorEnvironment)
fun onCups(editor: AmountEditorState): Change<AmountEditorState, AmountEditorOutcome> {
    val amount = context(environment.cups) { editor.cups.amount() }
    val agreed = if (amount != null) editor.withAmount(amount) else editor
    return Change(agreed)
}
