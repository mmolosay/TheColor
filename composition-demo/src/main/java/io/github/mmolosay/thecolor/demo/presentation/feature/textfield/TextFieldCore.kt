package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change

class TextFieldCore @AssistedInject constructor(
    @Assisted private val formatter: TextFormatter, // the parent's: which text is allowed
) {

    // the text formatted the same way as the text typed later
    fun initialState(text: String): TextFieldState {
        val formatted = formatter.format(text)
        return TextFieldState(text = formatted, version = 0)
    }

    // region Getters

    fun TextFieldState.text(): String =
        this.text

    fun TextFieldState.toViewData(): TextFieldViewData =
        TextFieldViewData(text = this.text)

    // for the parent
    fun TextFieldState.version(): Int =
        this.version

    // endregion

    // region Setters

    // The only way the text changes, so that its version moves with every change, whoever makes it.
    // Text that changes nothing leaves the state as it is, so that what was made on the text still
    // holds
    fun TextFieldState.withText(text: String): TextFieldState {
        val formatted = formatter.format(text)
        if (formatted == this.text) return this
        return this.copy(text = formatted, version = this.version + 1)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    fun TextFieldState.clearText(): Change<TextFieldState, Nothing> {
        val state = this.withText("")
        return Change(state)
    }

    fun TextFieldState.enterText(text: String, expected: String): Change<TextFieldState, Nothing> {
        if (this.text != expected) {
            return Change(this)
        }
        val state = this.withText(text)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(formatter: TextFormatter): TextFieldCore
    }
}

fun interface TextFormatter {
    fun format(text: String): String
}
