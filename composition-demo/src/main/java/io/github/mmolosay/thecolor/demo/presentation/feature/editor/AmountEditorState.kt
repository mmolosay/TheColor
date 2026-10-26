package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialState
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInputState
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInputState
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
        get = { state -> state.grams },
        set = { state, grams -> state.copy(grams = grams) },
    )

    val ounces = Lens<AmountEditorState, OuncesInputState>(
        get = { state -> state.ounces },
        set = { state, ounces -> state.copy(ounces = ounces) },
    )

    val cups = Lens<AmountEditorState, CupDialState>(
        get = { state -> state.cups },
        set = { state, cups -> state.copy(cups = cups) },
    )
}

sealed interface AmountEditorOutcome {
    // a whole amount, submitted in either input; the parent answers it with withSubmissionAccepted
    data class Submitted(val amount: Amount) : AmountEditorOutcome
}
