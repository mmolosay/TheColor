package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.isSameAs
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialCore
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputCore
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputCore
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputOutcome

class AmountEditorCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    gramsFactory: GramsInputCore.Factory,
    ouncesFactory: OuncesInputCore.Factory,
    cupsFactory: CupDialCore.Factory,
) {

    // its inputs', one each
    val gramsCore: GramsInputCore = gramsFactory.create()
    val ouncesCore: OuncesInputCore = ouncesFactory.create()
    val cupsCore: CupDialCore = cupsFactory.create()

    // from the parent's choice of amount
    fun initialState(amount: Amount?): AmountEditorState {
        val empty = AmountEditorState(
            grams = gramsCore.initialState(amount = null),
            ounces = ouncesCore.initialState(amount = null),
            cups = cupsCore.initialState(amount = null),
        )
        return empty.withAmount(amount)
    }

    // region Getters

    // the fact: the amount the inputs stand for; an input whose text isn't a whole amount yet, such
    // as an empty one, leaves it to the others
    fun AmountEditorState.amount(): Amount? =
        with(gramsCore) { grams.amount() }
            ?: with(ouncesCore) { ounces.amount() }
            ?: with(cupsCore) { cups.amount() }

    // for the parent: whether either input waits for an answer
    fun AmountEditorState.hasPendingSubmission(): Boolean =
        with(gramsCore) { grams.hasPendingSubmission() } ||
            with(ouncesCore) { ounces.hasPendingSubmission() }

    // for the parent; each input's version only grows, so their sum changes whenever any of them
    // does
    fun AmountEditorState.version(): Int =
        with(gramsCore) { grams.version() } +
            with(ouncesCore) { ounces.version() } +
            with(cupsCore) { cups.version() }

    // endregion

    // region Setters

    // The agreement after a typed edit. A whole amount is set on the other inputs. An input the
    // user emptied empties them too, since the amount is gone. An input whose text isn't a whole
    // amount yet, such as "1.", leaves them as they are while the user types
    private fun AmountEditorState.inLineWithInput(
        amount: Amount?,
        isEmpty: Boolean,
    ): AmountEditorState =
        when {
            amount != null -> this.withAmount(amount)
            isEmpty -> this.withAmount(null)
            else -> this
        }

    // The agreement, and the same fact given. Each input is set to the amount, unless it already
    // stands for it, so that the input the user is typing in keeps their text: "1.5" isn't
    // rewritten as "1.50"
    fun AmountEditorState.withAmount(amount: Amount?): AmountEditorState {
        val agreedGrams = with(gramsCore) {
            if (isSame(amount, grams.amount())) grams else grams.withAmount(amount)
        }
        val agreedOunces = with(ouncesCore) {
            if (isSame(amount, ounces.amount())) ounces else ounces.withAmount(amount)
        }
        val agreedCups = with(cupsCore) {
            if (isSame(amount, cups.amount())) cups else cups.withAmount(amount)
        }
        return this.copy(grams = agreedGrams, ounces = agreedOunces, cups = agreedCups)
    }

    // for the parent: answers whichever input waits for an answer
    fun AmountEditorState.withSubmissionAccepted(): AmountEditorState =
        this.copy(
            grams = with(gramsCore) { grams.withSubmissionAccepted() },
            ounces = with(ouncesCore) { ounces.withSubmissionAccepted() },
        )

    // endregion

    // region Transitions

    // For its Instance only, which runs each reducer inside its input's update. They can run more
    // than once, so they must have no side effects. Each brings the other inputs in line with the
    // edited one. A submission that stands for no amount is rejected here; a whole amount is handed
    // up, for the parent to answer

    fun AmountEditorState.onGrams(
        outcome: GramsInputOutcome?,
    ): Change<AmountEditorState, AmountEditorOutcome> {
        val amount = with(gramsCore) { grams.amount() }
        val isEmpty = with(gramsCore) { grams.isEmpty() }
        val agreed = this.inLineWithInput(amount, isEmpty)
        return when (outcome) {
            null -> Change(agreed)
            GramsInputOutcome.Submitted -> {
                if (amount == null) {
                    val rejected = with(gramsCore) { agreed.grams.withSubmissionRejected() }
                    return Change(agreed.copy(grams = rejected))
                }
                Change(agreed, outcome = AmountEditorOutcome.Submitted(amount))
            }
        }
    }

    fun AmountEditorState.onOunces(
        outcome: OuncesInputOutcome?,
    ): Change<AmountEditorState, AmountEditorOutcome> {
        val amount = with(ouncesCore) { ounces.amount() }
        val isEmpty = with(ouncesCore) { ounces.isEmpty() }
        val agreed = this.inLineWithInput(amount, isEmpty)
        return when (outcome) {
            null -> Change(agreed)
            OuncesInputOutcome.Submitted -> {
                if (amount == null) {
                    val rejected = with(ouncesCore) { agreed.ounces.withSubmissionRejected() }
                    return Change(agreed.copy(ounces = rejected))
                }
                Change(agreed, outcome = AmountEditorOutcome.Submitted(amount))
            }
        }
    }

    // The dial has no submissions. Each sample that lands brings the other inputs in line with it,
    // as a typed amount does
    fun AmountEditorState.onCups(): Change<AmountEditorState, AmountEditorOutcome> {
        val amount = with(cupsCore) { cups.amount() }
        val agreed = if (amount != null) this.withAmount(amount) else this
        return Change(agreed)
    }

    // endregion

    // in any form
    private fun isSame(amount: Amount?, other: Amount?): Boolean =
        context(amountConverter) { amount isSameAs other }

    @AssistedFactory
    fun interface Factory {
        fun create(): AmountEditorCore
    }
}
