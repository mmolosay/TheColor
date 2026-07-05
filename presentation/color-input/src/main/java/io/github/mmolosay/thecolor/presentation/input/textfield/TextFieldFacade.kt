package io.github.mmolosay.thecolor.presentation.input.textfield

import androidx.compose.runtime.Immutable

@Immutable
data class TextFieldFacade(
    val data: TextFieldData,
    val execute: (TextFieldAction) -> Unit,
    val inputProcessor: TextFieldInputProcessor,
)