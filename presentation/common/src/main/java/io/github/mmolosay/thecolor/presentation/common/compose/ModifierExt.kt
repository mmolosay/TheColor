package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.ui.Modifier

/**
 * Adds specified [modifier] to the receiver [Modifier] chain if [condition] is `true`.
 * Else, returns the receiver [Modifier] without changes.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    modifier: Modifier.() -> Modifier,
): Modifier =
    if (condition) {
        this.modifier()
    } else {
        this
    }