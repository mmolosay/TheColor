package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import javax.inject.Inject

class TextFieldDataFactory @Inject constructor() {

    fun create(
        text: WithSource<TextFieldData.Text> = TextFieldData.Text("") causedByUser false,
        isClearTextFeatureEnabled: Boolean,
    ): TextFieldData =
        TextFieldData(
            text = text,
            shouldSelectAllTextOnFocus = DefaultUserPreferences.SelectAllTextOnTextFieldFocus.enabled,
            isClearTextFeatureEnabled = isClearTextFeatureEnabled,
        )
}