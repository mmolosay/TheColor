package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

/*
 * Contains functions that transform Domain layer models into their equivalents in Presentation layer.
 */

/**
 * Maps [DomainUiTheme] of Domain layer to [ColorScheme] of Presentation layer and vice versa.
 */
fun DomainUiTheme.toPresentation(): ColorScheme =
    when (this) {
        DomainUiTheme.Light -> ColorScheme.Light
        DomainUiTheme.Dark -> ColorScheme.Dark
    }

/**
 * Processes [DomainUiThemeMode] of Domain layer into a corresponding [ColorScheme] of Presentation layer.
 */
fun DomainUiThemeMode.toPresentation(
    isSystemInDarkMode: Boolean,
): ColorScheme =
    when (this) {
        is UserPreferences.UiThemeMode.Single -> {
            this.theme.toPresentation()
        }
        is UserPreferences.UiThemeMode.Dual -> {
            val theme = if (isSystemInDarkMode) this.dark else this.light
            theme.toPresentation()
        }
    }