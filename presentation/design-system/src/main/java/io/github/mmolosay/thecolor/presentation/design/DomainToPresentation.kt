package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeMode as DomainUiColorSchemeMode

/*
 * Contains functions that transform Domain layer models into their equivalents in Presentation layer.
 */

/**
 * Maps [DomainUiColorScheme] of Domain layer to [ColorScheme] of Presentation layer and vice versa.
 */
fun DomainUiColorScheme.toPresentation(): ColorScheme =
    when (this) {
        DomainUiColorScheme.Light -> ColorScheme.Light
        DomainUiColorScheme.Dark -> ColorScheme.Dark
    }

/**
 * Maps [DomainUiColorSchemeMode] of Domain layer to a [ColorSchemeResolver] of Presentation layer.
 *
 * ### Reasoning behind having [ColorSchemeResolver] as returned type instead of [ColorScheme]
 * In order for this method to return a ready-to-use presentational [ColorScheme], it needs
 * to accept some `isSystemInDarkMode: Boolean` as a parameter.
 * It will be used to resolve [DomainUiColorSchemeMode.Dual].
 * At the same time, we want to call this function from ViewModel, where Domain models are usually
 * mapped to their Presentation layer counterparts.
 *
 * The value of `isSystemInDarkMode` parameter originates in UI or Android-aware component.
 * It's not a good approach to retrieve this value in ViewModel, because then ViewModel becomes
 * platform-aware and thus hard(er) to test.
 *
 * To avoid getting `isSystemInDarkMode` inside ViewModel, and still perform Domain to Presentation
 * mapping inside of the said ViewModel, we return [ColorSchemeResolver] from this function.
 * It suits our needs ideally: it's a Presentation layer model and is as ready as it's possible for
 * UI to use it, while still keeping ViewModel platform-agnostic.
 *
 * This way, the only thing for UI to do will be to supply 'brightness',
 * which falls well within View's scope of responsibility.
 */
fun DomainUiColorSchemeMode.toPresentation(): ColorSchemeResolver =
    ColorSchemeResolver { brightness ->
        val domainColorScheme = when (this) {
            is DomainUiColorSchemeMode.Single -> {
                this.scheme
            }
            is UserPreferences.UiColorSchemeMode.Dual -> {
                when (brightness) {
                    Brightness.Light -> this.light
                    Brightness.Dark -> this.dark
                }
            }
        }
        domainColorScheme.toPresentation()
    }