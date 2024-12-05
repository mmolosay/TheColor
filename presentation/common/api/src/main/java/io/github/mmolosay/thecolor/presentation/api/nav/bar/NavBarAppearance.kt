package io.github.mmolosay.thecolor.presentation.api.nav.bar

/**
 * Platform-agnostic model of navigation bar's appearance.
 */
data class NavBarAppearance(
    val color: Element.Color?,
    val controlsTint: Element.ControlsTint?,
) {

    sealed interface Element {
        data class Color(val argb: Int) : Element
        data class ControlsTint(val useLightTintForControls: Boolean) : Element
    }
}

val NavBarAppearance.isComplete: Boolean
    get() = (color != null) && (controlsTint != null)

fun NavBarAppearance(
    argbColor: Int? = null,
    useLightTintForControls: Boolean? = null,
): NavBarAppearance =
    NavBarAppearance(
        color = argbColor?.let { NavBarAppearance.Element.Color(it) },
        controlsTint = useLightTintForControls?.let { NavBarAppearance.Element.ControlsTint(it) },
    )

/**
 * Combines two [NavBarAppearance]s.
 * Fields that are missing from the receiver appearance will be taken from [other] appearance.
 * If specific element is missing from both appearances, it will stay `null`.
 */
infix fun NavBarAppearance.addFrom(other: NavBarAppearance): NavBarAppearance {
    if (this.isComplete) return this
    return NavBarAppearance(
        color = this.color ?: other.color,
        controlsTint = this.controlsTint ?: other.controlsTint,
    )
}