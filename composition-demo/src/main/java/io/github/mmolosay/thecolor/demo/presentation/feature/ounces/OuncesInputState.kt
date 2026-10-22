package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.text
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.withText
import java.math.BigDecimal

data class OuncesInputState(
    val textField: TextFieldState,
    val submission: Submission?,
)

sealed interface OuncesInputOutcome {
    data object Submitted : OuncesInputOutcome // the parent reads the amount from the state
}

object OuncesInputStateFactory {

    // from the parent's choice of amount
    context(environment: OuncesInputEnvironment)
    fun create(amount: Amount?): OuncesInputState {
        val textField = context(environment.textField) { TextFieldStateFactory.create(text = "") }
        val empty = OuncesInputState(textField = textField, submission = null)
        return empty.withAmount(amount)
    }
}

// the fact: what the text stands for; none while it isn't a whole amount yet, such as "1."
context(environment: OuncesInputEnvironment)
fun OuncesInputState.amount(): Amount? {
    val text = context(environment.textField) { this.textField.text() }
    if (text.isEmpty() || text.endsWith('.')) return null
    return Amount.Ounces(value = BigDecimal(text))
}

// the same fact, given
context(environment: OuncesInputEnvironment)
fun OuncesInputState.withAmount(amount: Amount?): OuncesInputState {
    val text = run {
        if (amount == null) return@run ""
        val ounces = with(environment.amountConverter) { amount.toOunces(OuncesDecimalPlaces) }
        ounces.value.toPlainString()
    }
    val textField = context(environment.textField) { this.textField.withText(text) }
    return this.copy(textField = textField)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.withSubmissionAccepted(): OuncesInputState {
    val submission = this.submission.answered(Submission.Answer.Accepted)
    return this.copy(submission = submission)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.withSubmissionRejected(): OuncesInputState {
    val submission = this.submission.answered(Submission.Answer.Rejected)
    return this.copy(submission = submission)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.toViewData(): OuncesInputViewData =
    OuncesInputViewData(answerToShow = this.submission.answerToShow())
