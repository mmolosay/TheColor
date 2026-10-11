package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import timber.log.Timber

fun TextFieldData.reduce(action: TextFieldAction): TextFieldData {
    return when (action) {
        is TextFieldAction.SetText -> {
            if (this.text.data != action.expected) {
                // TODO: remove; temporary log for the fast typing check
                Timber.tag("TextFieldRace").d(
                    "dropped '${action.text.string}': " +
                        "expected '${action.expected.string}', holds '${this.text.data.string}'",
                )
                return this // stale invocation
            }
            this.withText(action.text causedByUser true)
        }
        is TextFieldAction.ClearTextFeature.Invoke -> {
            if (this.isClearTextFeatureEnabled.not()) return this
            this.withText(Text("") causedByUser true)
        }
    }
}