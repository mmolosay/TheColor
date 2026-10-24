package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

/**
 * A field's source of truth: what its [TextFieldFeature], its View and its parent all go by.
 * Only the on-screen text field's internal buffer may briefly differ from it.
 *
 * It holds the text as last typed or set, which means nothing to the field itself: what the text
 * stands for, such as an amount, and which text is allowed are up to the field's parent. It holds
 * no cursor, which only the View needs, and no settings, such as a clear button, which depend on
 * where the field sits.
 *
 * Outside the feature, it must be created only with [TextFieldStateFactory] and used only through
 * its operations, so that how it holds its text can change without changing any parent.
 */
data class TextFieldState(
    val text: String,
    val version: Int,
)

/**
 * Creates a field's state with the environment its parent gives the field, so that the text it
 * starts with is formatted the same way as the text typed later.
 *
 * It holds nothing: every dependency comes from the environment.
 */
object TextFieldStateFactory {

    context(_: TextFieldEnvironment)
    fun create(text: String): TextFieldState {
        val empty = TextFieldState(text = "", version = 0)
        return empty.withText(text)
    }
}

// Operations over a field's state. Each takes the field's environment as a context parameter, even
// when it doesn't use it yet, so that it can gain dependencies without changing its callers

context(_: TextFieldEnvironment)
fun TextFieldState.text(): String =
    this.text

// The only way the text changes, so that its version moves with every change, whoever makes it.
// Text that changes nothing leaves the state as it is, so that what was made on the text still holds
context(environment: TextFieldEnvironment)
fun TextFieldState.withText(text: String): TextFieldState {
    val formatted = environment.formatter.format(text)
    if (formatted == this.text) return this
    return this.copy(text = formatted, version = this.version + 1)
}

// for the parent
context(_: TextFieldEnvironment)
fun TextFieldState.version(): Int =
    this.version

context(_: TextFieldEnvironment)
fun TextFieldState.toViewData(): TextFieldViewData =
    TextFieldViewData(text = this.text)
