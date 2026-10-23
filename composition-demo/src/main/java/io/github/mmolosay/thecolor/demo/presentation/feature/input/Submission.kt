package io.github.mmolosay.thecolor.demo.presentation.feature.input

// A submission and its answer, compared by reference: a new instance for each submission and each
// answer, so each answer is shown once
class Submission(
    val answer: Answer?, // null while no parent has answered
) {
    enum class Answer {
        Accepted,
        Rejected,
    }
}

// answers only a submission that awaits one, so an answer is never overwritten
fun Submission?.answered(answer: Submission.Answer): Submission? {
    if (this == null || this.answer != null) return this
    return Submission(answer = answer)
}

// what's left once the text it was made on changes: a submission still waiting is withdrawn, and an
// answer still to show stays
fun Submission?.withdrawn(): Submission? {
    if (this == null || this.answer != null) return this
    return null
}

fun Submission?.isPending(): Boolean =
    (this != null && this.answer == null)

// what's left once the View has shown this answered submission: a newer submission stays
fun Submission?.shown(submission: Submission): Submission? {
    if (this !== submission) return this
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
