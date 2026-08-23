package io.github.mmolosay.thecolor.presentation.details.viewmodel

/**
 * Handles [ColorDetailsEvent]s that originate in 'Color Details' feature.
 *
 * Provided to the 'Color Details' feature from the outside (by the caller / parent).
 */
fun interface ColorDetailsEventHandler {
    operator fun invoke(event: ColorDetailsEvent)
}
