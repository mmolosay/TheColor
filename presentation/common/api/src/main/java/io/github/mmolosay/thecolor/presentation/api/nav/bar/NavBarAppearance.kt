package io.github.mmolosay.thecolor.presentation.api.nav.bar

import java.util.Optional

/**
 * Platform-agnostic model of navigation bar's appearance.
 *
 * @param color a color integer in `ARGB` format.
 * @param useLightTintForControls whether the controls should be light to contrast against dark [color].
 */
data class NavBarAppearance(
    val color: Optional<Int>,
    val useLightTintForControls: Optional<Boolean>,
) {

    data class WithTag(
        val appearance: NavBarAppearance,
        val tag: Any?,
    )
}

/**
 * A builder function for [NavBarAppearance] with values by default.
 */
fun navBarAppearance(
    color: Optional<Int> = Optional.empty(),
    useLightTintForControls: Optional<Boolean> = Optional.empty(),
) =
    NavBarAppearance(
        color = color,
        useLightTintForControls = useLightTintForControls,
    )

infix fun NavBarAppearance.withTag(tag: Any?): NavBarAppearance.WithTag =
    NavBarAppearance.WithTag(appearance = this, tag = tag)