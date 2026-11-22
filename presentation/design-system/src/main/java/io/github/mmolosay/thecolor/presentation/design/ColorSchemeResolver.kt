package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.presentation.design.Material3DynamicColorsAvailability.areDynamicColorsAvailable
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

/**
 * Defines a strategy of obtaining a [ColorScheme] according to provided parameters.
 *
 * Brightness is usually inferred from system UI mode (see [systemBrightness]).
 */
fun interface ColorSchemeResolver {
    fun resolve(brightness: Brightness): ColorScheme
}

/**
 * Creates a [ColorSchemeResolver] that picks the scheme [colorSchemeSet] holds for a brightness.
 *
 * The brightness is the only input left to [ColorSchemeResolver.resolve], so that the resolver can
 * be created where the system's UI mode is not known.
 *
 * @param useDynamicColorSchemes whether the set's 'Light' and 'Dark' schemes resolve to
 * their dynamic versions. On a device without dynamic colors it has no effect, so no dynamic scheme
 * is resolved there.
 */
fun ColorSchemeResolver(
    colorSchemeSet: DomainUiColorSchemeSet,
    useDynamicColorSchemes: Boolean,
): ColorSchemeResolver {
    val useDynamic = (useDynamicColorSchemes && areDynamicColorsAvailable())
    return ColorSchemeResolver { brightness ->
        val domainColorScheme = when (brightness) {
            Brightness.Light -> colorSchemeSet.light
            Brightness.Dark -> colorSchemeSet.dark
        }
        when (domainColorScheme) {
            DomainUiColorScheme.Light -> when (useDynamic) {
                true -> ColorScheme.DynamicLight
                false -> ColorScheme.Light
            }
            DomainUiColorScheme.Dark -> when (useDynamic) {
                true -> ColorScheme.DynamicDark
                false -> ColorScheme.Dark
            }
            DomainUiColorScheme.Jungle -> ColorScheme.Jungle
            DomainUiColorScheme.Midnight -> ColorScheme.Midnight
        }
    }
}

/**
 * A [ColorSchemeResolver] that chooses between [ColorScheme.Light] and [ColorScheme.Dark].
 */
object DayNightColorSchemeResolver : ColorSchemeResolver {

    override fun resolve(brightness: Brightness): ColorScheme =
        when (brightness) {
            Brightness.Light -> ColorScheme.Light
            Brightness.Dark -> ColorScheme.Dark
        }
}
