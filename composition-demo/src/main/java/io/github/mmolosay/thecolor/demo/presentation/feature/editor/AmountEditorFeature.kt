package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
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
    @Assisted lifetime: CoroutineScope,
    @Assisted store: Store<AmountEditorState, AmountEditorOutcome>,
    @Assisted environment: AmountEditorEnvironment,
    gramsFactory: GramsInputFeature.Factory,
    ouncesFactory: OuncesInputFeature.Factory,
) {

    // for its View, which shows the inputs' Views
    val grams: GramsInputFeature = run {
        val slice = store.slice<GramsInputState, GramsInputOutcome>(gramsLens) { editor, outcome ->
            context(environment) { onGrams(editor, outcome) }
        }
        gramsFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.grams,
        )
    }

    val ounces: OuncesInputFeature = run {
        val slice = store.slice<OuncesInputState, OuncesInputOutcome>(ouncesLens) { editor, outcome ->
            context(environment) { onOunces(editor, outcome) }
        }
        ouncesFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.ounces,
        )
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
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

// The reducers for its inputs. Only the feature instance runs them, inside an input's update. Each
// brings the other input in line with the edited one. An edit that leaves no whole amount, such as
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
