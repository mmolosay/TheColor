package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.color.Color

/** A command issued towards 'Color Scheme' feature to be handled by it. */
sealed interface ColorSchemeCommand {

    /** Request to obtain data using specified parameters. */
    data class FetchData(
        val color: Color,
    ) : ColorSchemeCommand
}