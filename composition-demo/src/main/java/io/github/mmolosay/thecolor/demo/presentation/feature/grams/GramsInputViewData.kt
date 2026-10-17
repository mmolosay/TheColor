package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission

// no text: its Text Field's View shows it, from the field's own data
data class GramsInputViewData(
    val answerToShow: AnswerToShow?, // an answer the View hasn't shown yet
) {
    data class AnswerToShow(
        val token: Token,
        val answer: Submission.Answer,
    )
}
