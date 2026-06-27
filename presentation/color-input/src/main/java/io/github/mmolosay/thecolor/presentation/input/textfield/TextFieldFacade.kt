package io.github.mmolosay.thecolor.presentation.input.textfield

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

@Immutable
interface TextFieldFacade {

    val text: WithSource<Text>
    fun setText(text: Text)

    val inputProcessor: TextFieldInputProcessor
    val shouldSelectAllTextOnFocus: Boolean
    val clearTextFeature: ClearTextFeature?

    interface ClearTextFeature {
        operator fun invoke()
    }
}