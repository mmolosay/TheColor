/**
 * Operations over a field's state. Each takes the field's [TextFieldEnvironment] as a context
 * parameter, even when it doesn't use it yet, so that it can gain dependencies without changing its
 * callers.
 */

package io.github.mmolosay.thecolor.demo.textfield

context(_: TextFieldEnvironment)
fun newTextFieldState(text: String): TextFieldState {
    val empty = TextFieldState(text = "")
    return empty.withText(text)
}

context(_: TextFieldEnvironment)
fun TextFieldState.text(): String =
    this.text

context(environment: TextFieldEnvironment)
fun TextFieldState.withText(text: String): TextFieldState =
    this.copy(text = environment.formatter.format(text))
