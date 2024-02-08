package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps [Color] of domain layer to its representation in data layer (DTO).
 */
@Singleton
class ColorMapper @Inject constructor() {

    /*
     * The same algorithm can be found in io.github.mmolosay.thecolor.input.ColorConverter.
     * However, these pieces of code should not be unified and reused. This is a false duplication.
     *
     * This method serves data layer.
     * Its algorithm depends on what data APIs expect as input.
     *
     * The other method serves presentation layer.
     * Its algorithm depends on design and how it was decided to present data.
     *
     * These methods will change at different time for different reasons. They serve different
     * purposes and should not be fused. This is not a duplication, just a coincidence.
     */
    /**
     * Maps given [Color] to its HEX representation, e.g. "0047AB".
     */
    fun Color.Hex.toHexString(): String =
        this.value
            .toString(radix = 16)
            .uppercase()
            .trimStart { it == '0' }
            .padStart(6, '0')
}