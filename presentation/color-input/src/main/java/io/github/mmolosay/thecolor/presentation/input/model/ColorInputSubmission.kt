package io.github.mmolosay.thecolor.presentation.input.model

import io.github.mmolosay.thecolor.utils.Identity

sealed interface ColorInputSubmission {

    data class Pending(
        val text: String,
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

fun ColorInputSubmission?.isPending(text: String): Boolean =
    (this is ColorInputSubmission.Pending && this.text == text)

fun ColorInputSubmission?.answered(
    answer: ColorInputSubmission.Answer,
    text: String,
): ColorInputSubmission? {
    if (!this.isPending(text)) return this
    return ColorInputSubmission.Answered(answer = answer)
}

// TODO: should probably be inlined in the place of use
fun ColorInputSubmission?.acknowledged(
    submission: ColorInputSubmission.Answered,
): ColorInputSubmission? {
    if (this != submission) return this
    return null
}
