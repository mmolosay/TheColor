package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import javax.inject.Inject

/**
 * The dependencies of a field's operations and transitions, which take it as a context parameter.
 *
 * A function that needs a new dependency gets it through here, so that its callers don't change.
 *
 * Each field has one, which its parent holds in its own environment and gives to the
 * [TextFieldFeature] it creates, so a parent can give one of its fields another.
 */
class TextFieldEnvironment @Inject constructor(
    val formatter: TextFormatter, // used for example
)

fun interface TextFormatter {
    fun format(text: String): String
}
