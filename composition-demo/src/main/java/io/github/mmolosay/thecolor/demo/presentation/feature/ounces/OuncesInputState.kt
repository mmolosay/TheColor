package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.input.isPending
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldState
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.text
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.version
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.withText
import java.math.BigDecimal

data class OuncesInputState(
    val textField: TextFieldState,
    val submission: Submission?,
    val separator: DecimalSeparator, // the setting: the text holds a point, the View shows this
)

sealed interface OuncesInputOutcome {
    data object Submitted : OuncesInputOutcome // the parent reads the amount from the state
}

object OuncesInputStateFactory {

    // from the parent's choice of amount, with the point until the setting is read
    context(environment: OuncesInputEnvironment)
    fun create(amount: Amount?): OuncesInputState {
        val text = textFor(amount)
        val textField = context(environment.textField) { TextFieldStateFactory.create(text = text) }
        return OuncesInputState(
            textField = textField,
            submission = null,
            separator = DecimalSeparator.Point,
        )
    }
}

// the fact: what the text stands for; none while it isn't a whole amount yet, such as "1."
context(environment: OuncesInputEnvironment)
fun OuncesInputState.amount(): Amount? {
    val text = context(environment.textField) { this.textField.text() }
    if (text.isEmpty() || text.endsWith('.')) return null
    return Amount.Ounces(value = BigDecimal(text))
}

// no text at all, not even text that isn't an amount yet, such as "1."
context(environment: OuncesInputEnvironment)
fun OuncesInputState.isEmpty(): Boolean {
    val text = context(environment.textField) { this.textField.text() }
    return text.isEmpty()
}

// the same fact, given
context(environment: OuncesInputEnvironment)
fun OuncesInputState.withAmount(amount: Amount?): OuncesInputState {
    val text = textFor(amount)
    val textField = context(environment.textField) { this.textField.withText(text) }
    if (textField == this.textField) return this
    return this.copy(textField = textField)
}

// the text that stands for an amount, with a point
context(environment: OuncesInputEnvironment)
private fun textFor(amount: Amount?): String {
    if (amount == null) return ""
    val ounces = with(environment.amountConverter) { amount.toOunces(OuncesDecimalPlaces) }
    return ounces.value.toPlainString()
}

// for the parent; the setting doesn't move it: it's how the text shows, not what the user enters
context(environment: OuncesInputEnvironment)
fun OuncesInputState.version(): Int =
    context(environment.textField) { this.textField.version() }

context(_: OuncesInputEnvironment)
fun OuncesInputState.hasPendingSubmission(): Boolean =
    this.submission.isPending(this.version())

context(_: OuncesInputEnvironment)
fun OuncesInputState.withSubmissionAccepted(): OuncesInputState {
    val submission = this.submission.answered(Submission.Answer.Accepted, this.version())
    return this.copy(submission = submission)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.withSubmissionRejected(): OuncesInputState {
    val submission = this.submission.answered(Submission.Answer.Rejected, this.version())
    return this.copy(submission = submission)
}

context(_: OuncesInputEnvironment)
fun OuncesInputState.toViewData(): OuncesInputViewData =
    OuncesInputViewData(
        decimalSeparator = when (this.separator) {
            DecimalSeparator.Point -> '.'
            DecimalSeparator.Comma -> ','
        },
        isPending = this.hasPendingSubmission(),
        answerToShow = this.submission.answerToShow(),
    )
