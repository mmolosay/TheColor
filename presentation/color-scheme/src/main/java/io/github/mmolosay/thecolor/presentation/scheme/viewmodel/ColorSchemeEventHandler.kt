package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

/**
 * Handles [ColorSchemeEvent]s that originate in 'Color Scheme' feature.
 *
 * Provided to the 'Color Scheme' feature from the outside (by the caller / parent).
 */
fun interface ColorSchemeEventHandler {
    operator fun invoke(event: ColorSchemeEvent)
}