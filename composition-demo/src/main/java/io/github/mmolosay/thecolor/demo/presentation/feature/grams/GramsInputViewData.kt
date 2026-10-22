package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import io.github.mmolosay.thecolor.demo.presentation.feature.input.AnswerToShow

// no text: its Text Field's View shows it, from the field's own data
data class GramsInputViewData(
    val answerToShow: AnswerToShow?, // an answer the View hasn't shown yet
)
