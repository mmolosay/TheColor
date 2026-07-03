package io.github.mmolosay.thecolor.presentation.input.textfield

import androidx.compose.runtime.Immutable
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

/**
 * The View's interface to a feature: the values it presents and the actions it may invoke.
 *
 * To enable recomposition skipping in Compose, all implementations must:
 *  1. be immutable;
 *  2. provide value-based equality via [equals];
 *  3. back actions only by state that is itself stable (e.g. the owning ViewModel),
 *     never by transient per-instance data.
 *
 * The same requirements apply to nested member interfaces and classes.
 */
@Immutable
interface TextFieldFacade {

    val text: WithSource<Text>
    fun setText(text: Text)

    val inputProcessor: TextFieldInputProcessor
    val shouldSelectAllTextOnFocus: Boolean
    val clearTextFeature: ClearTextFeature?

    @Immutable
    interface ClearTextFeature {
        operator fun invoke()
    }
}