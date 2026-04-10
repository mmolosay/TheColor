package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

// TODO: rename file?

/**
 * A command issued towards 'Color Details' feature to be handled by it.
 * 
 * Processed in [ColorDetailsViewModel]. Should only be used to communicate with 
 * 'Color Details' feature from other features and `ViewModel`s.
 */
sealed interface ColorDetailsCommand {

    /**
     * Sets the specified [color] as the "seed" color for the 'Color Details' feature
     * and fetches the color details for it.
     */
    data class SetSeedColor(
        val color: Color,
    ) : ColorDetailsCommand

    /**
     * Same as the [SetSeedColor], but provides the [details] of the "seed" color to use.
     */
    data class SetSeedDetails(
        val details: DomainColorDetails,
    ) : ColorDetailsCommand

    /**
     * Selects a color with the specified [ColorRole].
     * Requires the "seed" color to be set.
     */
    data class SelectColor(
        val colorRole: ColorRole,
    ) : ColorDetailsCommand
}