package io.github.mmolosay.thecolor.presentation.input.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences

class TextFieldCore @AssistedInject constructor(
    @Assisted private val formatter: TextFormatter,
) {

    fun initialState(text: String): TextFieldState =
        TextFieldState(
            text = formatter.format(text),
            version = 0,
            selectAllTextOnFocus = DefaultUserPreferences.SelectAllTextOnTextFieldFocus.enabled,
        )

    // region Getters

    fun TextFieldState.toViewData(): TextFieldViewData =
        TextFieldViewData(
            text = this.text,
            version = this.version,
            selectAllTextOnFocus = this.selectAllTextOnFocus,
        )

    // endregion

    // region Setters

    fun TextFieldState.withText(text: String): TextFieldState {
        val formatted = formatter.format(text)
        if (formatted == this.text) return this
        return this.copy(text = formatted, version = this.version + 1)
    }

    // endregion

    // region Transitions

    fun TextFieldState.clearText(): Change<TextFieldState, Nothing> {
        val state = this.withText("")
        return Change(state)
    }

    fun TextFieldState.enterText(
        text: String,
        expected: String,
    ): Change<TextFieldState, Nothing> {
        if (this.text != expected) return Change(this)
        val state = this.withText(text)
        return Change(state)
    }

    fun TextFieldState.applySelectAllTextOnFocus(enabled: Boolean): Change<TextFieldState, Nothing> {
        val state = this.copy(selectAllTextOnFocus = enabled)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(
            formatter: TextFormatter,
        ): TextFieldCore
    }
}

fun interface TextFormatter {
    fun format(text: String): String
}