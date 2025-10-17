package io.github.mmolosay.thecolor.presentation.home.viewmodel

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
internal fun ColorCenterSession.allColors(): Set<Color> =
    relatedColors + seed

/* internal but Dagger */
class DoesColorBelongToSessionUseCase @Inject constructor(
    private val colorComparator: ColorComparator,
) {
    // syntactic sugar
    infix fun Color?.doesBelongTo(session: ColorCenterSession): Boolean =
        invoke(color = this, session = session)

    operator fun invoke(color: Color?, session: ColorCenterSession): Boolean {
        if (color == null) return false // allowed colors do not contain null
        val allAllowedColors = session.allColors()
        return allAllowedColors.any { allowedColor ->
            with(colorComparator) { color isSameAs allowedColor }
        }
    }
}