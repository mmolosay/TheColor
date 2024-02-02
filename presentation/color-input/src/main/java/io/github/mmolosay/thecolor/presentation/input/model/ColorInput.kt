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

/**
 * Imagine user enters "1" into hex color input View.
 * From data validation perspective, it is a valid, finished color: 0x1 == 0x000001 or RGB(0, 0, 1).
 * But from user perspective it is not a completed color. They do not consider "invisible" leading zeros.
 * User intends to enter "1______", and in their mind this "1" is not in the end, but in the beginning of future color.
 */
fun ColorInput.isCompleteFromUserPerspective(): Boolean =
    when (this) {
        is ColorInput.Hex -> this.isCompleteFromUserPerspective()
        is ColorInput.Rgb -> this.isCompleteFromUserPerspective()
    }

fun ColorInput.Hex.isCompleteFromUserPerspective(): Boolean =
    isInShortForm || isInFullForm

fun ColorInput.Rgb.isCompleteFromUserPerspective(): Boolean =
    r.isNotEmpty() && g.isNotEmpty() && b.isNotEmpty()