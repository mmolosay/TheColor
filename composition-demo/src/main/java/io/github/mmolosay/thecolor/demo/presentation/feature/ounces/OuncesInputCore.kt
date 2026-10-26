package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.input.isPending
import io.github.mmolosay.thecolor.demo.presentation.feature.input.shown
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter
import java.math.BigDecimal

class OuncesInputCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    textFieldFactory: TextFieldCore.Factory,
) {

    // its Text Field's, with the rule for the text it allows, so that the field's own writes and
    // this feature's all keep it
    val textFieldCore: TextFieldCore = textFieldFactory.create(formatter = ouncesTextFormatter)

    // from the parent's choice of amount, with the point until the setting is read
    fun initialState(amount: Amount?): OuncesInputState {
        val textField = textFieldCore.initialState(text = textFor(amount))
        return OuncesInputState(
            textField = textField,
            submission = null,
            separator = DecimalSeparator.Point,
        )
    }

    // region Getters

    // the fact: what the text stands for; none while it isn't a whole amount yet, such as "1."
    fun OuncesInputState.amount(): Amount? {
        val text = with(textFieldCore) { textField.text() }
        if (text.isEmpty() || text.endsWith('.')) return null
        return Amount.Ounces(value = BigDecimal(text))
    }

    // no text at all, not even text that isn't an amount yet, such as "1."
    fun OuncesInputState.isEmpty(): Boolean {
        val text = with(textFieldCore) { textField.text() }
        return text.isEmpty()
    }

    // for the parent; the setting doesn't move it: it's how the text shows, not what the user
    // enters
    fun OuncesInputState.version(): Int =
        with(textFieldCore) { textField.version() }

    fun OuncesInputState.hasPendingSubmission(): Boolean =
        this.submission.isPending(this.version())

    fun OuncesInputState.toViewData(): OuncesInputViewData =
        OuncesInputViewData(
            decimalSeparator = when (this.separator) {
                DecimalSeparator.Point -> '.'
                DecimalSeparator.Comma -> ','
            },
            isPending = this.hasPendingSubmission(),
            answerToShow = this.submission.answerToShow(),
        )

    // endregion

    // region Setters

    // the same fact, given
    fun OuncesInputState.withAmount(amount: Amount?): OuncesInputState {
        val field = with(textFieldCore) { textField.withText(textFor(amount)) }
        if (field == this.textField) return this
        return this.copy(textField = field)
    }

    fun OuncesInputState.withSubmissionAccepted(): OuncesInputState {
        val submission = this.submission.answered(Submission.Answer.Accepted, this.version())
        return this.copy(submission = submission)
    }

    fun OuncesInputState.withSubmissionRejected(): OuncesInputState {
        val submission = this.submission.answered(Submission.Answer.Rejected, this.version())
        return this.copy(submission = submission)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    fun OuncesInputState.answerShown(
        submission: Submission,
    ): Change<OuncesInputState, OuncesInputOutcome> {
        val state = this.copy(submission = this.submission.shown(submission))
        return Change(state)
    }

    // for the text as it is now: a change to it withdraws the submission
    fun OuncesInputState.submit(): Change<OuncesInputState, OuncesInputOutcome> {
        val submission = Submission(answer = null, version = this.version())
        val state = this.copy(submission = submission)
        return Change(state, outcome = OuncesInputOutcome.Submitted)
    }

    // The text holds a point either way, so a keystroke made on the text shown with the old
    // separator still applies
    fun OuncesInputState.receiveSeparator(
        separator: DecimalSeparator,
    ): Change<OuncesInputState, OuncesInputOutcome> {
        val state = this.copy(separator = separator)
        return Change(state)
    }

    // endregion

    // the text that stands for an amount, with a point
    private fun textFor(amount: Amount?): String {
        if (amount == null) return ""
        val ounces = with(amountConverter) { amount.toOunces(OuncesDecimalPlaces) }
        return ounces.value.toPlainString()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(): OuncesInputCore
    }
}

// the digits after the point, both for what the user types and for an amount set from elsewhere
private const val OuncesDecimalPlaces = 2

private const val MaxWholeDigits = 3

// Keeps only the text it allows: digits and one point, up to three digits before it, and no leading
// zeros except a single one before the point. A comma becomes the point, so the text holds a point
// whichever separator the setting shows
private val ouncesTextFormatter = TextFormatter { text ->
    val kept = text.replace(',', '.').filter { it.isDigit() || it == '.' }
    val point = kept.indexOf('.')
    val whole = run {
        val digits = if (point < 0) kept else kept.substring(0, point)
        val trimmed = digits.trimStart('0').take(MaxWholeDigits)
        if (trimmed.isEmpty() && (digits.isNotEmpty() || point >= 0)) "0" else trimmed
    }
    if (point < 0) return@TextFormatter whole
    val fraction = kept.substring(point + 1).filter { it != '.' }.take(OuncesDecimalPlaces)
    "$whole.$fraction"
}
