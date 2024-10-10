package io.github.mmolosay.thecolor.data.remote.mapper

import io.github.mmolosay.thecolor.data.remote.model.ColorSchemeDto
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import javax.inject.Inject

/**
 * Maps [ColorSchemeDto] model of Data layer to [ColorScheme] model of Domain layer.
 */
class ColorSchemeMapper @Inject constructor(
    private val colorDetailsMapper: ColorDetailsMapper,
) {

    fun ColorSchemeDto.toDomain(): ColorScheme =
        ColorScheme(
            swatchDetails = this.swatches.map {
                with(colorDetailsMapper) { it.toDomain() }
            },
        )
}