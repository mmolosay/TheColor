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
 * Maps [DomainUiColorSchemeMode] of Domain layer to a corresponding [ColorScheme] of Presentation layer.
 */
fun DomainUiColorSchemeMode.toPresentation(
    isSystemInDarkMode: Boolean,
): ColorScheme =
    when (this) {
        is DomainUiColorSchemeMode.Single -> {
            this.theme.toPresentation()
        }
        is UserPreferences.UiColorSchemeMode.Dual -> {
            val theme = if (isSystemInDarkMode) this.dark else this.light
            theme.toPresentation()
        }
    }