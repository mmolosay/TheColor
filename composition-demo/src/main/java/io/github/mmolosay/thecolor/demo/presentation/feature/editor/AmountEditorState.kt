package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.isSameAs
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialState
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.version
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.withAmount
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.hasPendingSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.version
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.withAmount
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.withSubmissionAccepted
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.hasPendingSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.version
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.withAmount
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.withSubmissionAccepted
import io.github.mmolosay.thecolor.utils.Lens

// Inputs that agree on one amount: whichever the user types in or drags, the others stand for the
// same amount, in the same update
data class AmountEditorState(
    val grams: GramsInputState,
    val ounces: OuncesInputState,
    val cups: CupDialState,
)

object AmountEditorStateLenses {

    val grams = Lens<AmountEditorState, GramsInputState>(
        get = { editor -> editor.grams },
        set = { editor, grams -> editor.copy(grams = grams) },
    )

    val ounces = Lens<AmountEditorState, OuncesInputState>(
        get = { editor -> editor.ounces },
        set = { editor, ounces -> editor.copy(ounces = ounces) },
    )

    val cups = Lens<AmountEditorState, CupDialState>(
        get = { editor -> editor.cups },
        set = { editor, cups -> editor.copy(cups = cups) },
    )
}

sealed interface AmountEditorOutcome {
    // a whole amount, submitted in either input; the parent answers it with withSubmissionAccepted
    data class Submitted(val amount: Amount) : AmountEditorOutcome
}

object AmountEditorStateFactory {

    // from the parent's choice of amount
    context(environment: AmountEditorEnvironment)
    fun create(amount: Amount?): AmountEditorState {
        val grams = context(environment.grams) { GramsInputStateFactory.create(amount = null) }
        val ounces = context(environment.ounces) { OuncesInputStateFactory.create(amount = null) }
        val cups = context(environment.cups) { CupDialStateFactory.create(amount = null) }
        val empty = AmountEditorState(grams = grams, ounces = ounces, cups = cups)
        return empty.withAmount(amount)
    }
}

// the fact: the amount the inputs stand for; an input whose text isn't a whole amount yet, such as an
// empty one, leaves it to the others
context(environment: AmountEditorEnvironment)
fun AmountEditorState.amount(): Amount? {
    val grams = context(environment.grams) { this.grams.amount() }
    if (grams != null) return grams
    val ounces = context(environment.ounces) { this.ounces.amount() }
    if (ounces != null) return ounces
    return context(environment.cups) { this.cups.amount() }
}

// The agreement, and the same fact given. Each input is set to the amount, unless it already stands
// for it, so that the input the user is typing in keeps their text: "1.5" isn't rewritten as "1.50"
context(environment: AmountEditorEnvironment)
fun AmountEditorState.withAmount(amount: Amount?): AmountEditorState {
    val grams = run {
        val current = context(environment.grams) { this.grams.amount() }
        val isSame = context(environment.amountConverter) { amount isSameAs current }
        if (isSame) return@run this.grams
        context(environment.grams) { this.grams.withAmount(amount) }
    }
    val ounces = run {
        val current = context(environment.ounces) { this.ounces.amount() }
        val isSame = context(environment.amountConverter) { amount isSameAs current }
        if (isSame) return@run this.ounces
        context(environment.ounces) { this.ounces.withAmount(amount) }
    }
    val cups = run {
        val current = context(environment.cups) { this.cups.amount() }
        val isSame = context(environment.amountConverter) { amount isSameAs current }
        if (isSame) return@run this.cups
        context(environment.cups) { this.cups.withAmount(amount) }
    }
    return this.copy(grams = grams, ounces = ounces, cups = cups)
}

// for the parent; each input's version only grows, so their sum changes whenever any of them does
context(environment: AmountEditorEnvironment)
fun AmountEditorState.version(): Int {
    val grams = context(environment.grams) { this.grams.version() }
    val ounces = context(environment.ounces) { this.ounces.version() }
    val cups = context(environment.cups) { this.cups.version() }
    return (grams + ounces + cups)
}

// for the parent: whether either input waits for an answer
context(environment: AmountEditorEnvironment)
fun AmountEditorState.hasPendingSubmission(): Boolean {
    val grams = context(environment.grams) { this.grams.hasPendingSubmission() }
    val ounces = context(environment.ounces) { this.ounces.hasPendingSubmission() }
    return (grams || ounces)
}

// for the parent: answers whichever input waits for an answer
context(environment: AmountEditorEnvironment)
fun AmountEditorState.withSubmissionAccepted(): AmountEditorState {
    val grams = context(environment.grams) { this.grams.withSubmissionAccepted() }
    val ounces = context(environment.ounces) { this.ounces.withSubmissionAccepted() }
    return this.copy(grams = grams, ounces = ounces)
}
