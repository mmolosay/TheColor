package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.input.isPending
import io.github.mmolosay.thecolor.demo.presentation.feature.input.withdrawn
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
    if (textField == this.textField) return this
    return this.copy(textField = textField).withTextChanged()
}

// A submission is for the text it was made on, so once the text changes, a submission still
// waiting for an answer is withdrawn. Every path that changes the text calls it: the parent's
// withAmount, and the reducer for the field's own edits
context(_: GramsInputEnvironment)
fun GramsInputState.withTextChanged(): GramsInputState =
    this.copy(submission = this.submission.withdrawn())

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

context(_: GramsInputEnvironment)
fun GramsInputState.toViewData(): GramsInputViewData =
    GramsInputViewData(
        isPending = this.submission.isPending(),
        answerToShow = this.submission.answerToShow(),
    )
