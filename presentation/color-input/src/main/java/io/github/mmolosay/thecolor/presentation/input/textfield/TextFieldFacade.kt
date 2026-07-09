package io.github.mmolosay.thecolor.presentation.input.textfield

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

@Immutable
data class TextFieldFacade(
    val execute: (TextFieldAction) -> Unit,
    val text: WithSource<Text>,
    val shouldSelectAllTextOnFocus: Boolean,
    val isClearTextFeatureEnabled: Boolean,
    val inputProcessor: TextFieldInputProcessor,
)