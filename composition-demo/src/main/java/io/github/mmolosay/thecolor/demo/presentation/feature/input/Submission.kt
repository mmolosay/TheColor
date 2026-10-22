package io.github.mmolosay.thecolor.demo.presentation.feature.input

import io.github.mmolosay.thecolor.composition.Token

// a submission and its answer; a new token for each, so each one is answered and shown once
data class Submission(
    val token: Token,
    val answer: Answer?, // null while no parent has answered
) {
    enum class Answer {
        Accepted,
        Rejected,
    }
}

fun newSubmission(): Submission =
    Submission(token = Token(), answer = null)

// answers only a submission that awaits one, so an answer is never overwritten
fun Submission?.answered(answer: Submission.Answer): Submission? {
    if (this == null || this.answer != null) return this
    return this.copy(answer = answer)
}

// what's left once the text it was made on changes: a submission still waiting is withdrawn, and an
// answer still to show stays
fun Submission?.withdrawn(): Submission? {
    if (this == null || this.answer != null) return this
    return null
}

fun Submission?.isPending(): Boolean =
    (this != null && this.answer == null)

// what's left once the View has shown the answer for this token: a newer submission stays
fun Submission?.shown(token: Token): Submission? {
    if (this == null || this.token !== token) return this
    return null
}

// an answer the View hasn't shown yet, with the token that tells it apart from a newer one
data class AnswerToShow(
    val token: Token,
    val answer: Submission.Answer,
)

fun Submission?.answerToShow(): AnswerToShow? {
    val answer = this?.answer ?: return null
    return AnswerToShow(token = this.token, answer = answer)
}
