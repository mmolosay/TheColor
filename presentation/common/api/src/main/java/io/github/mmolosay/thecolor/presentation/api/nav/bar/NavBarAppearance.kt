package io.github.mmolosay.thecolor.presentation.api.nav.bar

/**
 * Platform-agnostic model of navigation bar's appearance.
 * May not contain all the data due to having nullable fields.
 *
 * @param argbColor a color integer in `ARGB` format commonly used across Android SDK.
 * @param useLightTintForControls whether the controls should be light to contrast against dark [argbColor].
 */
data class NavBarAppearance(
    val argbColor: Int?,
    val useLightTintForControls: Boolean?,
)

/**
 * Checks whether all fields have not-null value.
 */
val NavBarAppearance.isComplete: Boolean
    get() = (argbColor != null) && (useLightTintForControls != null)

/**
 * A builder function for [NavBarAppearance] with `null` values by default.
 */
fun navBarAppearance(
    argbColor: Int? = null,
    useLightTintForControls: Boolean? = null,
): NavBarAppearance =
    NavBarAppearance(
        argbColor = argbColor,
        useLightTintForControls = useLightTintForControls,
    )

/**
 * Combines two [NavBarAppearance]s.
 * Fields that are missing from the receiver appearance will be taken from [other] appearance.
 *
 * If the receiver [isComplete], returns receiver.
 * If the receiver has a value in the field, it will be used in returned appearance.
 * If a field is missing from both appearances, it will stay `null` in returned appearance.
 */
infix fun NavBarAppearance.addFrom(other: NavBarAppearance): NavBarAppearance {
    if (this.isComplete) return this
    return navBarAppearance(
        argbColor = this.argbColor ?: other.argbColor,
        useLightTintForControls = this.useLightTintForControls ?: other.useLightTintForControls,
    )
}