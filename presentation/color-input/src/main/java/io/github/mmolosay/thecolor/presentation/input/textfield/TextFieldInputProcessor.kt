package io.github.mmolosay.thecolor.presentation.input.textfield

fun interface TextFieldInputProcessor {
    operator fun invoke(input: String): TextFieldData.Text
}