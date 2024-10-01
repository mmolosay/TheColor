package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.home.ColorCenterSession.AllowedColors
import io.github.mmolosay.thecolor.presentation.home.ColorCenterSession.Seed
import javax.inject.Inject

/**
 * A data regarding current session of Color Center.
 * Despite first thought that one may have, this component (and other connected to it)
 * belongs to Home feature (and thus to `:home` module).
 *
 * Session is tied to a [seed], which is a main color which started this session.
 * Session starts when color is submitted (proceeded with) in Home feature.
 * Session ends when color is cleared / changed via Color Input.
 *
 * Any color can be checked whether it [doesBelongToSession].
 */
/* internal but Dagger */
class ColorCenterSession(
    val seed: Seed,
    val allowedColors: AllowedColors,
) {
    fun Color.doesBelongToSession(): Boolean {
        val allAllowedColors = allowedColors.set + seed.color
        return (this in allAllowedColors)
    }

    @JvmInline
    value class Seed(val color: Color)

    @JvmInline
    value class AllowedColors(val set: Set<Color>)
}

/**
 * Creates instances of [ColorCenterSession] in a progressive manner.
 * It is an implementation of a "Builder" design pattern.
 * The instance of a builder is reusable: dependencies will be disposed of once the `build()` is called,
 * allowing for a new round of the instance creation.
 */
/* internal but Dagger */
class ColorCenterSessionBuilder @Inject constructor() {

    var seed: Color? = null
    var allowedColors: Set<Color>? = null

    fun seed(color: Color) = apply {
        this.seed = color
    }

    fun allowedColors(colors: Set<Color>) = apply {
        this.allowedColors = colors
    }

    fun build(): ColorCenterSession {
        val seed = requireNotNull(seed)
        val allowedColors = requireNotNull(allowedColors)
        return ColorCenterSession(
            seed = Seed(color = seed),
            allowedColors = AllowedColors(set = allowedColors),
        ).also {
            clear()
        }
    }

    fun clear() {
        this.seed = null
        this.allowedColors = null
    }
}