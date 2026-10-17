package io.github.mmolosay.thecolor.demo.textfield

/**
 * A field's source of truth: what its [TextFieldFeature], its View and its parent all go by.
 * Only the on-screen control's own buffer may briefly differ from it.
 *
 * It holds the text as last typed or set, which means nothing to the field itself: what the text
 * stands for, such as an amount, and which text is allowed are up to the field's parent. It holds
 * no cursor, which only the View needs, and no settings, such as a clear button, which depend on
 * where the field sits.
 *
 * Outside the feature, it must be created and used only through its operations, so that how it
 * holds its text can change without changing any parent.
 */
data class TextFieldState(
    val text: String,
)
