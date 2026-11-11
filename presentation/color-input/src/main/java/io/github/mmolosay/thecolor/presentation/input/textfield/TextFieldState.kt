package io.github.mmolosay.thecolor.presentation.input.textfield

data class TextFieldState(
    val text: String,
    val version: Int,
    val selectAllTextOnFocus: Boolean,
)