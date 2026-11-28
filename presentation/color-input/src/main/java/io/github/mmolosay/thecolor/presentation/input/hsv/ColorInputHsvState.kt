package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.utils.Identity

sealed interface ColorInputHsvState {

    val color: Color.Hsv?
    val version: Int

    data class Settled(
        override val color: Color.Hsv?,
        override val version: Int,
    ) : ColorInputHsvState

    data class Picked(
        val picked: Color.Hsv,
        override val color: Color.Hsv?,
        val promotion: Promotion,
        override val version: Int,
    ) : ColorInputHsvState {

        data class Promotion(
            val identity: Identity = Identity(),
        )
    }
}