package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

// everything a field's View shows, so that the View reads nothing else
data class TextFieldViewData(
    val text: String,
    val version: Int, // not drawn: a text set back to an earlier one is still new to the collector
)