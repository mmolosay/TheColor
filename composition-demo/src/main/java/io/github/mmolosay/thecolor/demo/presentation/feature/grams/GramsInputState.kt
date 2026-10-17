package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.text
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.withText

data class GramsInputState(
    val textField: TextFieldState,
    val submission: Submission?,
)

sealed interface GramsInputOutcome {
    data object Submitted : GramsInputOutcome // the parent reads the amount from the state
}

object GramsInputStateFactory {

    // from the parent's choice of amount
    context(environment: GramsInputEnvironment)
    fun create(amount: Amount?): GramsInputState {
        val textField = context(environment.textField) { TextFieldStateFactory.create(text = "") }
        val empty = GramsInputState(textField = textField, submission = null)
        return empty.withAmount(amount)
    }
}

// the fact: what the text stands for
context(environment: GramsInputEnvironment)
fun GramsInputState.amount(): Amount? {
    val text = context(environment.textField) { this.textField.text() }
    val value = text.toIntOrNull() ?: return null
    return Amount.Grams(value = value)
}

// the same fact, given
context(environment: GramsInputEnvironment)
fun GramsInputState.withAmount(amount: Amount?): GramsInputState {
    val text = run {
        if (amount == null) return@run ""
        val grams = with(environment.amountConverter) { amount.toGrams() }
        grams.value.toString()
    }
    val textField = context(environment.textField) { this.textField.withText(text) }
    return this.copy(textField = textField)
}

context(_: GramsInputEnvironment)
fun GramsInputState.withSubmissionAccepted(): GramsInputState {
    val submission = this.submission.answered(Submission.Answer.Accepted)
    return this.copy(submission = submission)
}

context(_: GramsInputEnvironment)
fun GramsInputState.withSubmissionRejected(): GramsInputState {
    val submission = this.submission.answered(Submission.Answer.Rejected)
    return this.copy(submission = submission)
}
