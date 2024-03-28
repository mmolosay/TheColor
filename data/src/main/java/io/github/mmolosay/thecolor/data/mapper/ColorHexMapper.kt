package io.github.mmolosay.thecolor.data.mapper

import io.github.mmolosay.thecolor.domain.model.Color
import javax.inject.Inject

/**
 * Maps color hex string from DTO (e.g. "0047AB") to [Color.Hex].
 */
class ColorHexMapper @Inject constructor() {

    fun String.toColorHex(): Color.Hex =
        this
            .trimStart('#')
            .toInt(radix = 16)
            .let { Color.Hex(value = it) }
}