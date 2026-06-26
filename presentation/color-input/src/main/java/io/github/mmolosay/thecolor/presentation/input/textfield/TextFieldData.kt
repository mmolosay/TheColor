package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.model.WithSource

/**
 * Platform-agnostic data provided by ViewModel to 'Text Field' View.
 *
 * @param text a current text in this 'Text Field'.
 * Change it using [TextFieldAction.SetText].
 *
 * @param isClearTextFeatureEnabled depicts whether a 'clear text' feature is enabled in this 'Text Field'
 * Invoke it using [TextFieldAction.ClearText].
 */
data class TextFieldData(
    val text: WithSource<Text>,
    val inputProcessor: TextFieldInputProcessor,
    val shouldSelectAllTextOnFocus: Boolean,
    val isClearTextFeatureEnabled: Boolean,
) {
    /**
     * Text that was processed (filtered, validated, etc.) and is View-ready.
     *
     * The main point of having this `value class` is to avoid accidents with data types:
     * raw input is [String], processed value is [Text].
     *
     * Use [TextFieldInputProcessor] to produce valid instances of [Text].
     */
    // there's no 'Text.Empty' extension for the same reason why there's no 'String.Empty'
    @JvmInline
    value class Text(val string: String)
}