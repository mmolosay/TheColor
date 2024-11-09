package io.github.mmolosay.thecolor.presentation.input.impl.field

/**
 * Platform-agnostic data provided by ViewModel to 'Text Field' View.
 *
 * @param onTextChange a callback to be invoked when displayed text is changed.
 * It should receive a result of [filterUserInput] or other text that is presentation-ready.
 *
 * @param filterUserInput filters text from user input and returns processed text to be displayed.
 */
data class TextFieldData(
    val text: Text,
    val onTextChange: (Text) -> Unit,
    val filterUserInput: (String) -> Text,
    val trailingButton: TrailingButton,
    val shouldSelectAllTextOnFocus: Boolean,
) {
    /**
     * Text that was filtered and is ready to be displayed in UI.
     * Can be obtained from [filterUserInput].
     *
     * The main point of having this `value class` is to avoid accidents with data types.
     * Function [filterUserInput] takes String from UI and returns filtered [Text].
     * At the same time, [onTextChange] takes [Text], not String.
     * This depicts that [onTextChange] should receive preemptively filtered text from [filterUserInput].
     */
    // I decided to not introduce 'Text.Empty' extension for the same reason why there's no String.Empty
    @JvmInline
    value class Text(val string: String)

    sealed interface TrailingButton {
        data object Hidden : TrailingButton
        data class Visible(val onClick: () -> Unit) : TrailingButton
    }
}