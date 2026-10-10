package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

fun TextFieldData.reduce(action: TextFieldAction): TextFieldData {
    return when (action) {
        is TextFieldAction.SetText -> {
            if (this.text.data != action.expected) return this // stale invocation
            this.withText(action.text causedByUser true)
        }
        is TextFieldAction.ClearTextFeature.Invoke -> {
            if (this.isClearTextFeatureEnabled.not()) return this
            this.withText(Text("") causedByUser true)
        }
    }
}