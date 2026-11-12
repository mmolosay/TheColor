package io.github.mmolosay.thecolor.presentation.input.model

import io.github.mmolosay.thecolor.utils.Identity

sealed interface ColorInputSubmission {

    data class Pending(
        val input: ColorInput,
        val identity: Identity = Identity(),
    ) : ColorInputSubmission

    data class Answered(
        val answer: Answer,
        val identity: Identity = Identity(),
    ) : ColorInputSubmission

    enum class Answer {
        Accepted,
        Rejected,
    }
}

fun ColorInputSubmission?.isPending(
    expectedInput: ColorInput,
): Boolean =
    (this is ColorInputSubmission.Pending && this.input == expectedInput)

fun ColorInputSubmission?.answered(
    answer: ColorInputSubmission.Answer,
    expectedInput: ColorInput,
): ColorInputSubmission? {
    if (!this.isPending(expectedInput)) return this
    return ColorInputSubmission.Answered(answer = answer)
}