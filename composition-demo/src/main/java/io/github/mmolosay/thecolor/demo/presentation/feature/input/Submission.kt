package io.github.mmolosay.thecolor.demo.presentation.feature.input

import io.github.mmolosay.thecolor.utils.Identity

// A submission and its answer: a new instance for each submission and each answer, so each answer
// is shown once
data class Submission(
    val answer: Answer?, // null while no parent has answered
    val version: Int, // of the text it was made on
    val identity: Identity = Identity(),
) {
    enum class Answer {
        Accepted,
        Rejected,
    }
}

// A submission is for the text it was made on, so it waits for an answer only while the text is at
// that version: any change to the text withdraws it, and an answer to it has nothing to apply to
fun Submission?.isPending(version: Int): Boolean =
    (this != null && this.answer == null && this.version == version)

// answers only a submission that awaits one, so an answer is never overwritten, and a withdrawn
// submission is never answered
fun Submission?.answered(answer: Submission.Answer, version: Int): Submission? {
    if (this == null || !this.isPending(version)) return this
    return Submission(answer = answer, version = this.version)
}

// what's left once the View has shown this answered submission: a newer submission stays
fun Submission?.shown(submission: Submission): Submission? {
    if (this != submission) return this
    return null
}

// an answer the View hasn't shown yet, with the submission it answers, which the View acknowledges
data class AnswerToShow(
    val submission: Submission,
    val answer: Submission.Answer,
)

fun Submission?.answerToShow(): AnswerToShow? {
    val answer = this?.answer ?: return null
    return AnswerToShow(submission = this, answer = answer)
}
