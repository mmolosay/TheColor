package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.input.isPending
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.text
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.version
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
        val text = textFor(amount)
        val textField = context(environment.textField) { TextFieldStateFactory.create(text = text) }
        return GramsInputState(textField = textField, submission = null)
    }
}

// the fact: what the text stands for
context(environment: GramsInputEnvironment)
fun GramsInputState.amount(): Amount? {
    val text = context(environment.textField) { this.textField.text() }
    val value = text.toIntOrNull() ?: return null
    return Amount.Grams(value = value)
}

// no text at all, not even text that isn't an amount yet
context(environment: GramsInputEnvironment)
fun GramsInputState.isEmpty(): Boolean {
    val text = context(environment.textField) { this.textField.text() }
    return text.isEmpty()
}

// the same fact, given
context(environment: GramsInputEnvironment)
fun GramsInputState.withAmount(amount: Amount?): GramsInputState {
    val text = textFor(amount)
    val textField = context(environment.textField) { this.textField.withText(text) }
    if (textField == this.textField) return this
    return this.copy(textField = textField)
}

// the text that stands for an amount
context(environment: GramsInputEnvironment)
private fun textFor(amount: Amount?): String {
    if (amount == null) return ""
    val grams = with(environment.amountConverter) { amount.toGrams() }
    return grams.value.toString()
}

// for the parent
context(environment: GramsInputEnvironment)
fun GramsInputState.version(): Int =
    context(environment.textField) { this.textField.version() }

context(_: GramsInputEnvironment)
fun GramsInputState.hasPendingSubmission(): Boolean =
    this.submission.isPending(this.version())

context(_: GramsInputEnvironment)
fun GramsInputState.withSubmissionAccepted(): GramsInputState {
    val submission = this.submission.answered(Submission.Answer.Accepted, this.version())
    return this.copy(submission = submission)
}

context(_: GramsInputEnvironment)
fun GramsInputState.withSubmissionRejected(): GramsInputState {
    val submission = this.submission.answered(Submission.Answer.Rejected, this.version())
    return this.copy(submission = submission)
}

context(_: GramsInputEnvironment)
fun GramsInputState.toViewData(): GramsInputViewData =
    GramsInputViewData(
        isPending = this.hasPendingSubmission(),
        answerToShow = this.submission.answerToShow(),
    )
