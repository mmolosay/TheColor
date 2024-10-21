package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorComparator
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
 * Any color can be checked whether it belongs to a session using [DoesColorBelongToSessionUseCase].
 *
 * Not a `data` class. Two instances may contain same colors but in different color spaces.
 * The auto-generated [equals] method of `data` class will use [Color.equals], which doesn't check
 * for structural equality. For this, see [ColorComparator].
 */
/* internal but Dagger */
class ColorCenterSession(
    val seed: Color,
    val relatedColors: Set<Color>,
)

/**
 * Returns all colors that are related to this session.
 */
fun ColorCenterSession.allColors(): Set<Color> =
    relatedColors + seed

class DoesColorBelongToSessionUseCase @Inject constructor(
    private val colorComparator: ColorComparator,
) {
    // syntactic sugar
    infix fun Color.doesBelongTo(session: ColorCenterSession): Boolean =
        invoke(color = this, session = session)

    operator fun invoke(color: Color, session: ColorCenterSession): Boolean {
        val allAllowedColors = session.allColors()
        return allAllowedColors.any { allowedColor ->
            with(colorComparator) { color isSameAs allowedColor }
        }
    }
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
    var relatedColors: Set<Color>? = null

    fun seed(color: Color) = apply {
        this.seed = color
    }

    fun relatedColors(colors: Set<Color>) = apply {
        this.relatedColors = colors
    }

    fun build(): ColorCenterSession {
        val seed = requireNotNull(seed)
        val allowedColors = requireNotNull(relatedColors)
        return ColorCenterSession(
            seed = seed,
            relatedColors = allowedColors,
        ).also {
            clear()
        }
    }

    fun clear() {
        this.seed = null
        this.relatedColors = null
    }
}