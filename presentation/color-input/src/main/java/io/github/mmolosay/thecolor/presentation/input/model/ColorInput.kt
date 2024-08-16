package io.github.mmolosay.thecolor.presentation.input.model

/**
 * Data, that was entered by user via UI of color input View.
 * Does not contain `null`s because any text field's text is at least an empty string.
 */
sealed interface ColorInput {

    data class Hex(
        val string: String,
    ) : ColorInput

    data class Rgb(
        val r: String,
        val g: String,
        val b: String,
    ) : ColorInput
}

val ColorInput.Hex.isInShortForm: Boolean
    get() = string.length == 3

val ColorInput.Hex.isInFullForm: Boolean
    get() = string.length == 6