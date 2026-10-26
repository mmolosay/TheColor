package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answerToShow
import io.github.mmolosay.thecolor.demo.presentation.feature.input.answered
import io.github.mmolosay.thecolor.demo.presentation.feature.input.isPending
import io.github.mmolosay.thecolor.demo.presentation.feature.input.shown
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter

class GramsInputCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    textFieldFactory: TextFieldCore.Factory,
) {

    // its Text Field's, with the rule for the text it allows, so that the field's own writes and
    // this feature's all keep it
    val textFieldCore: TextFieldCore = textFieldFactory.create(formatter = gramsTextFormatter)

    // from the parent's choice of amount
    fun initialState(amount: Amount?): GramsInputState {
        val textField = textFieldCore.initialState(text = textFor(amount))
        return GramsInputState(textField = textField, submission = null)
    }

    // region Getters

    // the fact: what the text stands for
    fun GramsInputState.amount(): Amount? {
        val text = with(textFieldCore) { textField.text() }
        val value = text.toIntOrNull() ?: return null
        return Amount.Grams(value = value)
    }

    // no text at all, not even text that isn't an amount yet
    fun GramsInputState.isEmpty(): Boolean {
        val text = with(textFieldCore) { textField.text() }
        return text.isEmpty()
    }

    // for the parent
    fun GramsInputState.version(): Int =
        with(textFieldCore) { textField.version() }

    fun GramsInputState.hasPendingSubmission(): Boolean =
        this.submission.isPending(this.version())

    fun GramsInputState.toViewData(): GramsInputViewData =
        GramsInputViewData(
            isPending = this.hasPendingSubmission(),
            answerToShow = this.submission.answerToShow(),
        )

    // endregion

    // region Setters

    // the same fact, given
    fun GramsInputState.withAmount(amount: Amount?): GramsInputState {
        val field = with(textFieldCore) { textField.withText(textFor(amount)) }
        if (field == this.textField) return this
        return this.copy(textField = field)
    }

    fun GramsInputState.withSubmissionAccepted(): GramsInputState {
        val submission = this.submission.answered(Submission.Answer.Accepted, this.version())
        return this.copy(submission = submission)
    }

    fun GramsInputState.withSubmissionRejected(): GramsInputState {
        val submission = this.submission.answered(Submission.Answer.Rejected, this.version())
        return this.copy(submission = submission)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    fun GramsInputState.answerShown(
        submission: Submission,
    ): Change<GramsInputState, GramsInputOutcome> {
        val state = this.copy(submission = this.submission.shown(submission))
        return Change(state)
    }

    // for the text as it is now: a change to it withdraws the submission
    fun GramsInputState.submit(): Change<GramsInputState, GramsInputOutcome> {
        val submission = Submission(answer = null, version = this.version())
        val state = this.copy(submission = submission)
        return Change(state, outcome = GramsInputOutcome.Submitted)
    }

    // endregion

    // the text that stands for an amount
    private fun textFor(amount: Amount?): String {
        if (amount == null) return ""
        val grams = with(amountConverter) { amount.toGrams() }
        return grams.value.toString()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(): GramsInputCore
    }
}

// keeps only the text it allows: digits, up to four, no leading zeros
private val gramsTextFormatter = TextFormatter { text ->
    text.filter { it in '0'..'9' }.trimStart('0').take(4)
}
