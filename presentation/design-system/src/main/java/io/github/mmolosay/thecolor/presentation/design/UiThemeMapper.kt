package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme

/**
 * Maps [DomainUiTheme] of Domain layer to [UiTheme] of Domain layer and vice versa.
 */
object UiThemeMapper {

    fun DomainUiTheme.toPresentation(): UiTheme =
        when (this) {
            DomainUiTheme.Light -> UiTheme.Light
            DomainUiTheme.Dark -> UiTheme.Dark
        }
}