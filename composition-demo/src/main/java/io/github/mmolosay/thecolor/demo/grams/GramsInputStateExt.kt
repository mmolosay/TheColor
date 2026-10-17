package io.github.mmolosay.thecolor.demo.grams

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.input.Submission
import io.github.mmolosay.thecolor.demo.input.answered
import io.github.mmolosay.thecolor.demo.textfield.newTextFieldState
import io.github.mmolosay.thecolor.demo.textfield.text
import io.github.mmolosay.thecolor.demo.textfield.withText

// from the parent's choice of amount
context(environment: GramsInputEnvironment)
fun newGramsInputState(amount: Amount?): GramsInputState {
    val textField = context(environment.textField) { newTextFieldState(text = "") }
    val empty = GramsInputState(textField = textField, submission = null)
    return empty.withAmount(amount)
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
