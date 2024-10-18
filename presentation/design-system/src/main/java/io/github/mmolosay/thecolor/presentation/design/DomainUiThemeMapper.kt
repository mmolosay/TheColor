package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme

/**
 * Maps [DomainUiTheme] of Domain layer to [ColorScheme] of Presentation layer and vice versa.
 */
object DomainUiThemeMapper {

    fun DomainUiTheme.toPresentation(): ColorScheme =
        when (this) {
            DomainUiTheme.Light -> ColorScheme.Light
            DomainUiTheme.Dark -> ColorScheme.Dark
        }
}